package kodanect.domain.remembrance.controller;

import kodanect.common.exception.config.GlobalExcepHndlr;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.service.MemorialService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemorialController.class)
@Import(GlobalExcepHndlr.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class MemorialControllerExceptionTest {

    private static final Integer invalidDonateSeq = -1;
    private static final String BAD_REQUEST_MESSAGE = "잘못된 요청입니다.";
    private static final String NOT_FOUND_MESSAGE = "요청한 리소스를 찾을 수 없습니다.";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private MemorialService memorialService;

    @MockBean
    private MessageSourceAccessor messageSourceAccessor;

    /*
    *
    * 추모관 게시글 리스트 조회
    *
    * */

    @Test
    @DisplayName("추모관 게시글 리스트 조회 : 유효하지 않은 페이징 범위를 요청한 경우 - 400")
    public void getMemorialListInvalidPaginationRangeException() throws Exception {

        Integer invalidCursor = -1;
        int invalidSize = -1;

        when(memorialService.getMemorialList(invalidCursor, invalidSize))
                .thenThrow(new InvalidPaginationRangeException());

        mockMvc.perform(get("/remembrance")
                        .param("cursor", String.valueOf(invalidCursor))
                        .param("size", String.valueOf(invalidSize)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    /*
    *
    * 추모관 게시글 상세 조회
    *
    * */

    @Test
    @DisplayName("추모관 게시글 상세 조회 : 존재하지 않는 게시글을 요청한 경우 - 404")
    public void getMemorialByDonateSeqMemorialNotFoundException() throws Exception {

        when(memorialService.getMemorialByDonateSeq(invalidDonateSeq))
                .thenThrow(new MemorialNotFoundException());

        mockMvc.perform(get("/remembrance/{donateSeq}", invalidDonateSeq))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("추모관 게시글 상세 조회 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    public void getMemorialByDonateSeqInvalidDonateSeqException() throws Exception {

        when(memorialService.getMemorialByDonateSeq(invalidDonateSeq))
                .thenThrow(new InvalidDonateSeqException());

        mockMvc.perform(get("/remembrance/{donateSeq}", invalidDonateSeq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    /*
    *
    * 추모관 게시글 검색 조회
    *
    * */

    @Test
    @DisplayName("추모관 게시글 리스트 검색 조회 : 유효하지 않은 페이징 범위를 요청한 경우 - 400")
    public void getSearchMemorialListInvalidPaginationRangeException() throws Exception {

        when(memorialService.getSearchMemorialList(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new InvalidPaginationRangeException());

        mockMvc.perform(get("/remembrance/search")
                        .param("startDate", "2020-01-01")
                        .param("endDate", "2020-12-31")
                        .param("searchWord", "")
                        .param("cursor", "-1")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("추모관 게시글 리스트 검색 조회 : 날짜 검색 파라미터가 누락된 경우 - 400")
    public void getSearchMemorialListMissingSearchDateParameterException() throws Exception {

        when(memorialService.getSearchMemorialList(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new MissingSearchDateParameterException());

        mockMvc.perform(get("/remembrance/search")
                        .param("startDate", "invalid")
                        .param("endDate", "invalid")
                        .param("searchWord", "")
                        .param("cursor", "1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("추모관 게시글 리스트 검색 조회 : 유효하지 않은 날짜 형식을 요청한 경우 - 400")
    public void getSearchMemorialListInvalidSearchDateFormatException() throws Exception {

        when(memorialService.getSearchMemorialList(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new InvalidSearchDateFormatException());

        mockMvc.perform(get("/remembrance/search")
                        .param("startDate", "invalid")
                        .param("endDate", "invalid")
                        .param("searchWord", "")
                        .param("cursor", "1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("추모관 게시글 리스트 검색 조회 : 유효하지 않은 날짜 범위를 요청한 경우 - 400")
    public void getSearchMemorialListInvalidSearchDateRangeException() throws Exception {

        when(memorialService.getSearchMemorialList(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new InvalidSearchDateRangeException());

        mockMvc.perform(get("/remembrance/search")
                        .param("startDate", "2020-01-01")
                        .param("endDate", "2019-01-01")
                        .param("searchWord", "")
                        .param("cursor", "1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    /*
    *
    * 추모관 이모지 카운팅
    *
    * */

    @Test
    @DisplayName("추모관 이모지 카운팅 : 유효하지 않은 이모지를 요청한 경우 - 400")
    public void updateMemorialLikeCountInvalidEmotionTypeException() throws Exception {

        Integer validDonateSeq = 1;
        String invalidEmotion = "angry";

        doThrow(new InvalidEmotionTypeException())
                .when(memorialService)
                .emotionCountUpdate(validDonateSeq, invalidEmotion);

        mockMvc.perform(patch("/remembrance/{donateSeq}/{emotion}", validDonateSeq, invalidEmotion))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("추모관 이모지 카운팅 : 존재하지 않는 게시글을 요청한 경우 - 404")
    public void updateMemorialLikeCountMemorialNotFoundException() throws Exception {

        String invalidEmotion = "angry";

        doThrow(new MemorialNotFoundException())
                .when(memorialService)
                .emotionCountUpdate(invalidDonateSeq, invalidEmotion);

        mockMvc.perform(patch("/remembrance/{donateSeq}/{emotion}", invalidDonateSeq, invalidEmotion))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("추모관 이모지 카운팅 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    public void updateMemorialLikeCountInvalidDonateSeqException() throws Exception {

        String invalidEmotion = "angry";

        doThrow(new InvalidDonateSeqException())
                .when(memorialService)
                .emotionCountUpdate(invalidDonateSeq, invalidEmotion);

        mockMvc.perform(patch("/remembrance/{donateSeq}/{emotion}", invalidDonateSeq, invalidEmotion))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }
}
