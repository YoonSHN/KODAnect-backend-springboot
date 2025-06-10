package kodanect.domain.remembrance.controller;

import kodanect.common.exception.config.MemorialExceptionHandler;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.service.MemorialService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemorialController.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class MemorialControllerExceptionTest {

    @TestConfiguration
    static class TestMessageSourceConfig {

        @Bean
        public MessageSource messageSource() {
            ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
            messageSource.setBasenames("egovframework/message/message-common"); // _ko 빼고!
            messageSource.setDefaultEncoding("UTF-8");
            messageSource.setFallbackToSystemLocale(false); // 시스템 로케일 무시
            return messageSource;
        }

        @Bean
        public MessageSourceAccessor messageSourceAccessor(MessageSource messageSource) {
            return new MessageSourceAccessor(messageSource, Locale.KOREA);
        }

        @Bean
        public MemorialExceptionHandler memorialExceptionHandler(MessageSourceAccessor accessor) {
            return new MemorialExceptionHandler(accessor);
        }
    }

    private static final Integer DONATE_SEQUENCE = 1;
    private static final Integer INVALID_DONATE_SEQUENCE = -1;
    private static final Integer MAX_DONATE_SEQUENCE = Integer.MAX_VALUE;
    private static final Integer CURSOR = 1;
    private static final int SIZE = 20;
    private static final Integer INVALID_CURSOR = -1;
    private static final int INVALID_SIZE = -1;
    private static final String EMPTY = "";
    private static final String START_DATE = "2020-01-01";
    private static final String END_DATE = "2020-12-31";
    private static final String INVALID_EMOTION = "angry";
    private static final int NOT_FOUND = 404;
    private static final int BAD_REQUEST = 400;

    private static final String INVALID_PAGINATION_MESSAGE =
            "요청한 페이지 범위가 잘못되었습니다. (cursor: -1, size: -1)";
    private static final String MEMORIAL_NOT_FOUND_MESSAGE =
            "해당 추모글을 찾을 수 없습니다. (donateSeq: 2,147,483,647)";
    private static final String DONATE_INVALID_MESSAGE =
            "해당 추모글을 찾을 수 없습니다.";
    private static final String SEARCH_DATE_INVALID_MESSAGE =
            "날짜 형식이 잘못되었습니다. (startDate: invalid, endDate: invalid)";
    private static final String SEARCH_DATE_RANGE_INVALID_MESSAGE =
            "검색 시작일은 종료일보다 이전이어야 합니다. (startDate: 2020-12-31, endDate: 2020-01-01)";
    private static final String INVALID_EMOTION_MESSAGE =
            "지원하지 않는 감정 표현입니다. (emotion: angry)";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private MemorialService memorialService;

    @Autowired
    private MessageSourceAccessor messageSourceAccessor;

    /*
    *
    * 추모관 게시글 리스트 조회
    *
    * */

    @Test
    @DisplayName("추모관 게시글 리스트 조회 : 유효하지 않은 페이징 범위를 요청한 경우 - 400")
    void getMemorialListInvalidPaginationRangeException() throws Exception {

        mockMvc.perform(get("/remembrance")
                        .param("cursor", String.valueOf(INVALID_CURSOR))
                        .param("size", String.valueOf(INVALID_SIZE)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(INVALID_PAGINATION_MESSAGE));
    }

    /*
    *
    * 추모관 게시글 상세 조회
    *
    * */

    @Test
    @DisplayName("추모관 게시글 상세 조회 : 존재하지 않는 게시글을 요청한 경우 - 404")
    void getMemorialByDonateSeqMemorialNotFoundException() throws Exception {

        when(memorialService.getMemorialByDonateSeq(MAX_DONATE_SEQUENCE))
                .thenThrow(new MemorialNotFoundException(MAX_DONATE_SEQUENCE));

        mockMvc.perform(get("/remembrance/{donateSeq}", MAX_DONATE_SEQUENCE))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(NOT_FOUND))
                .andExpect(jsonPath("$.message").value(MEMORIAL_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("추모관 게시글 상세 조회 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    void getMemorialByDonateSeqInvalidDonateSeqException() throws Exception {

        mockMvc.perform(get("/remembrance/{donateSeq}", INVALID_DONATE_SEQUENCE))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(DONATE_INVALID_MESSAGE));
    }

    /*
    *
    * 추모관 게시글 검색 조회
    *
    * */

    @Test
    @DisplayName("추모관 게시글 리스트 검색 조회 : 유효하지 않은 페이징 범위를 요청한 경우 - 400")
    void getSearchMemorialListInvalidPaginationRangeException() throws Exception {

        mockMvc.perform(get("/remembrance/search")
                        .param("startDate", START_DATE)
                        .param("endDate", END_DATE)
                        .param("searchWord", EMPTY)
                        .param("cursor", String.valueOf(INVALID_CURSOR))
                        .param("size", String.valueOf(INVALID_SIZE)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(INVALID_PAGINATION_MESSAGE));
    }

    @Test
    @DisplayName("추모관 게시글 리스트 검색 조회 : 날짜 검색 파라미터가 누락된 경우 - 400")
    void getSearchMemorialListInvalidSearchDateException() throws Exception {

        mockMvc.perform(get("/remembrance/search")
                        .param("startDate", "invalid")
                        .param("endDate", "invalid")
                        .param("searchWord", EMPTY)
                        .param("cursor", String.valueOf(CURSOR))
                        .param("size", String.valueOf(SIZE)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(SEARCH_DATE_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("추모관 게시글 리스트 검색 조회 : 유효하지 않은 날짜 범위를 요청한 경우 - 400")
    void getSearchMemorialListInvalidSearchDateRangeException() throws Exception {

        mockMvc.perform(get("/remembrance/search")
                        .param("startDate", END_DATE)
                        .param("endDate", START_DATE)
                        .param("searchWord", EMPTY)
                        .param("cursor", String.valueOf(CURSOR))
                        .param("size", String.valueOf(SIZE)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(SEARCH_DATE_RANGE_INVALID_MESSAGE));
    }

    /*
    *
    * 추모관 이모지 카운팅
    *
    * */

    @Test
    @DisplayName("추모관 이모지 카운팅 : 유효하지 않은 이모지를 요청한 경우 - 400")
    void updateMemorialLikeCountInvalidEmotionTypeException() throws Exception {

        doThrow(new InvalidEmotionTypeException(INVALID_EMOTION))
                .when(memorialService)
                .emotionCountUpdate(DONATE_SEQUENCE, INVALID_EMOTION);

        mockMvc.perform(patch("/remembrance/{donateSeq}/{emotion}",
                        DONATE_SEQUENCE, INVALID_EMOTION))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(INVALID_EMOTION_MESSAGE));
    }

    @Test
    @DisplayName("추모관 이모지 카운팅 : 존재하지 않는 게시글을 요청한 경우 - 404")
    void updateMemorialLikeCountMemorialNotFoundException() throws Exception {

        doThrow(new MemorialNotFoundException(MAX_DONATE_SEQUENCE))
                .when(memorialService)
                .emotionCountUpdate(MAX_DONATE_SEQUENCE, INVALID_EMOTION);

        mockMvc.perform(patch("/remembrance/{donateSeq}/{emotion}",
                        MAX_DONATE_SEQUENCE, INVALID_EMOTION))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(NOT_FOUND))
                .andExpect(jsonPath("$.message").value(MEMORIAL_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("추모관 이모지 카운팅 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    void updateMemorialLikeCountInvalidDonateSeqException() throws Exception {

        mockMvc.perform(patch("/remembrance/{donateSeq}/{emotion}",
                        INVALID_DONATE_SEQUENCE, INVALID_EMOTION))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(DONATE_INVALID_MESSAGE));
    }
}
