package kodanect.domain.remembrance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.exception.config.GlobalExcepHndlr;
import kodanect.common.exception.config.MemorialExceptionHandler;
import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.dto.MemorialReplyPasswordRequest;
import kodanect.domain.remembrance.dto.MemorialReplyUpdateRequest;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.service.MemorialReplyService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.web.servlet.MockMvc;

import javax.ws.rs.core.MediaType;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemorialReplyController.class)
@Import(GlobalExcepHndlr.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class MemorialReplyControllerExceptionTest {

    @TestConfiguration
    static class TestMessageSourceConfig {

        @Bean
        public MessageSource messageSource() {
            ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
            messageSource.setBasenames("egovframework/message/message-common");
            messageSource.setDefaultEncoding("UTF-8");
            messageSource.setFallbackToSystemLocale(true);
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

    private static final Integer REPLY_SEQUENCE = 1;
    private static final Integer INVALID_REPLY_SEQUENCE = -1;
    private static final Integer MAX_REPLY_SEQUENCE = Integer.MAX_VALUE;

    private static final Integer CURSOR = 1;
    private static final Integer INVALID_CURSOR = -1;
    private static final int SIZE = 1;
    private static final int INVALID_SIZE = -1;

    private static final String EMPTY = "";
    private static final String CONTENTS = "내용";
    private static final String REPLY_WRITER = "홍 길동";
    private static final String INVALID_REPLY_WRITER = "zi존홍! 길동";
    private static final String REPLY_PASSWORD = "1234asdf";
    private static final String INVALID_REPLY_PASSWORD = "zi!asd1212315555";

    private static final int NOT_FOUND = 404;
    private static final int BAD_REQUEST = 400;
    private static final int CONFLICT = 409;
    private static final int FORBIDDEN = 403;

    private static final String INVALID_PAGINATION_MESSAGE =
            "요청한 페이지 범위가 잘못되었습니다. (cursor: -1, size: -1)";
    private static final String MEMORIAL_NOT_FOUND_MESSAGE =
            "해당 추모글을 찾을 수 없습니다. (donateSeq: 2,147,483,647)";
    private static final String DONATE_INVALID_MESSAGE =
            "해당 추모글을 찾을 수 없습니다.";
    private static final String REPLY_NOT_FOUND_MESSAGE =
            "해당 댓글을 찾을 수 없습니다. (replySeq: 2,147,483,647)";
    private static final String REPLY_INVALID_MESSAGE =
            "해당 댓글을 찾을 수 없습니다.";
    private static final String REPLY_CONTENTS_EMPTY_MESSAGE =
            "댓글 내용을 입력해 주세요.";
    private static final String REPLY_PASSWORD_INVALID_MESSAGE =
            "비밀번호는 영문과 숫자를 포함한 8~16자여야 합니다.";
    private static final String REPLY_PASSWORD_EMPTY_MESSAGE =
            "비밀번호를 입력해 주세요.";
    private static final String REPLY_WRITER_EMPTY_MESSAGE =
            "작성자 닉네임을 입력해 주세요.";
    private static final String REPLY_WRITER_INVALID_MESSAGE =
            "작성자 닉네임 형식이 올바르지 않습니다.";
    private static final String REPLY_PASSWORD_MISMATCH_MESSAGE =
            "댓글 비밀번호가 일치하지 않습니다. (replySeq: 1)";
    private static final String REPLY_ALREADY_DELETED_MESSAGE =
            "이미 삭제된 댓글입니다. (replySeq: 1)";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MemorialReplyService memorialReplyService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MessageSourceAccessor messageSourceAccessor;

    /*
     *
     * 댓글 더보기
     *
     * */

    @Test
    @DisplayName("댓글 더보기 : 존재하지 않는 게시글을 요청한 경우 - 404")
    void moreMemorialNotFoundException() throws Exception {

        doThrow(new MemorialNotFoundException(MAX_DONATE_SEQUENCE))
                .when(memorialReplyService)
                .getMoreReplyList(MAX_DONATE_SEQUENCE, CURSOR, SIZE);

        mockMvc.perform(get("/remembrance/{donateSeq}/replies", MAX_DONATE_SEQUENCE)
                        .param("cursor", String.valueOf(CURSOR))
                        .param("size", String.valueOf(SIZE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(NOT_FOUND))
                .andExpect(jsonPath("$.message").value(MEMORIAL_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 더보기 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    void moreInvalidDonateSeqException() throws Exception {

        mockMvc.perform(get("/remembrance/{donateSeq}/replies", INVALID_DONATE_SEQUENCE)
                        .param("cursor", String.valueOf(CURSOR))
                        .param("size", String.valueOf(SIZE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(DONATE_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 더보기 : 유효하지 않은 페이징 번호를 요청한 경우 - 400")
    void moreInvalidPaginationException() throws Exception {

        mockMvc.perform(get("/remembrance/{donateSeq}/replies", DONATE_SEQUENCE)
                        .param("cursor", String.valueOf(INVALID_CURSOR))
                        .param("size", String.valueOf(INVALID_SIZE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(INVALID_PAGINATION_MESSAGE));
    }

    /*
     *
     * 댓글 생성
     *
     * */

    @Test
    @DisplayName("댓글 생성 : 내용이 입력되지 않은 경우 - 400")
    void createMissingReplyContentException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .replyContents(EMPTY)
                        .replyPassword(REPLY_PASSWORD)
                        .replyWriter(REPLY_WRITER)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_CONTENTS_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 작성자가 입력되지 않은 경우 - 400")
    void createMissingReplyWriterException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .replyContents(CONTENTS)
                        .replyPassword(REPLY_PASSWORD)
                        .replyWriter(EMPTY)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_WRITER_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 작성자 입력 형식이 올바르지 않은 경우 - 400")
    void createInvalidReplyWriterException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .replyContents(CONTENTS)
                        .replyPassword(REPLY_PASSWORD)
                        .replyWriter(INVALID_REPLY_WRITER)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_WRITER_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 비밀번호가 입력되지 않은 경우 - 400")
    void createMissingReplyPasswordException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .replyContents(CONTENTS)
                        .replyPassword(EMPTY)
                        .replyWriter(REPLY_WRITER)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_PASSWORD_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 비밀번호 입력 형식이 올바르지 않은 경우 - 400")
    void createInvalidReplyPasswordException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .replyContents(CONTENTS)
                        .replyPassword(INVALID_REPLY_PASSWORD)
                        .replyWriter(REPLY_WRITER)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_PASSWORD_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    void createInvalidDonateSeqException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .replyContents(CONTENTS)
                        .replyPassword(REPLY_PASSWORD)
                        .replyWriter(REPLY_WRITER)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", INVALID_DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(DONATE_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 존재하지 않은 게시글 번호를 요청한 경우 - 400")
    void createNotFoundDonateSeqException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .replyContents(CONTENTS)
                        .replyPassword(REPLY_PASSWORD)
                        .replyWriter(REPLY_WRITER)
                        .build();

        doThrow(new MemorialNotFoundException(MAX_DONATE_SEQUENCE))
                .when(memorialReplyService)
                .createReply(anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", MAX_DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(NOT_FOUND))
                .andExpect(jsonPath("$.message").value(MEMORIAL_NOT_FOUND_MESSAGE));
    }


    /*
     *
     * 비밀번호 인증
     *
     * */

    @Test
    @DisplayName("비밀번호 인증 : 비밀번호가 입력되지 않은 경우 - 400")
    void varifyMissingReplyPasswordException() throws Exception {

        MemorialReplyPasswordRequest request =
                MemorialReplyPasswordRequest
                        .builder()
                        .replyPassword(EMPTY)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_PASSWORD_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("비밀번호 인증 : 비밀번호 형식이 맞지 않은 경우 - 400")
    void varifyInvalidReplyPasswordException() throws Exception {

        MemorialReplyPasswordRequest request =
                MemorialReplyPasswordRequest
                        .builder()
                        .replyPassword(INVALID_REPLY_PASSWORD)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_PASSWORD_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("비밀번호 인증 : 비밀번호가 일치하지 않는 경우 - 403")
    void varifyReplyPasswordMismatchException() throws Exception {

        MemorialReplyPasswordRequest request =
                MemorialReplyPasswordRequest
                        .builder()
                        .replyPassword(REPLY_PASSWORD)
                        .build();

        doThrow(new ReplyPasswordMismatchException(REPLY_SEQUENCE))
                .when(memorialReplyService)
                .verifyReplyPassword(anyInt(), anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(FORBIDDEN))
                .andExpect(jsonPath("$.message").value(REPLY_PASSWORD_MISMATCH_MESSAGE));
    }

    /*
     *
     * 댓글 수정
     *
     * */

    @Test
    @DisplayName("댓글 수정 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    void updateInvalidDonateSeqException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .replyWriter(REPLY_WRITER)
                        .replyContents(CONTENTS)
                        .build();

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}",
                        INVALID_DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(DONATE_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 존재하지 않은 게시글 번호를 요청한 경우 - 404")
    void updateNotFoundDonateSeqException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .replyWriter(REPLY_WRITER)
                        .replyContents(CONTENTS)
                        .build();

        doThrow(new MemorialNotFoundException(MAX_DONATE_SEQUENCE))
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}",
                        MAX_DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(NOT_FOUND))
                .andExpect(jsonPath("$.message").value(MEMORIAL_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 유효하지 않은 댓글 번호를 요청한 경우 - 400")
    void updateInvalidReplySeqException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .replyWriter(REPLY_WRITER)
                        .replyContents(CONTENTS)
                        .build();

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, INVALID_REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 존재하지 않는 댓글을 요청한 경우 - 404")
    void updateMemorialReplyNotFoundException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .replyWriter(REPLY_WRITER)
                        .replyContents(CONTENTS)
                        .build();

        doThrow(new MemorialReplyNotFoundException(MAX_DONATE_SEQUENCE))
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, MAX_REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(NOT_FOUND))
                .andExpect(jsonPath("$.message").value(REPLY_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 이미 삭제된 댓글을 다시 삭제하거나 수정하려는 경우 - 409")
    void updateReplyAlreadyDeleteException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .replyWriter(REPLY_WRITER)
                        .replyContents(CONTENTS)
                        .build();

        doThrow(new ReplyAlreadyDeleteException(REPLY_SEQUENCE))
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CONFLICT))
                .andExpect(jsonPath("$.message").value(REPLY_ALREADY_DELETED_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 내용이 입력되지 않은 경우 - 400")
    void updateMissingReplyContentException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .replyWriter(REPLY_WRITER)
                        .replyContents(EMPTY)
                        .build();

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_CONTENTS_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 작성자 이름이 입력되지 않은 경우 - 400")
    void updateMissingReplyWriterException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .replyWriter(EMPTY)
                        .replyContents(CONTENTS)
                        .build();

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_WRITER_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 작성자 이름 형식이 올바르지 않은 경우 - 400")
    void updateInvalidReplyWriterException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .replyWriter(INVALID_REPLY_WRITER)
                        .replyContents(CONTENTS)
                        .build();

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_WRITER_INVALID_MESSAGE));
    }


    /*
     *
     * 댓글 삭제
     *
     * */

    @Test
    @DisplayName("댓글 삭제 : 비밀번호가 입력되지 않은 경우 - 400")
    void deleteMissingReplyPasswordException() throws Exception {

        MemorialReplyPasswordRequest request =
                MemorialReplyPasswordRequest
                        .builder()
                        .replyPassword(EMPTY)
                        .build();

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_PASSWORD_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 비밀번호가 입력 형식이 올바르지 않은 경우 - 400")
    void deleteInvalidReplyPasswordException() throws Exception {

        MemorialReplyPasswordRequest request =
                MemorialReplyPasswordRequest
                        .builder()
                        .replyPassword(INVALID_REPLY_PASSWORD)
                        .build();

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_PASSWORD_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 댓글 비밀번호가 일치하지 않는 경우 - 403")
    void deleteReplyPasswordMismatchException() throws Exception {

        MemorialReplyPasswordRequest request =
                MemorialReplyPasswordRequest
                        .builder()
                        .replyPassword(REPLY_PASSWORD)
                        .build();

        doThrow(new ReplyPasswordMismatchException(REPLY_SEQUENCE))
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(FORBIDDEN))
                .andExpect(jsonPath("$.message").value(REPLY_PASSWORD_MISMATCH_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 존재하지 않는 댓글을 요청한 경우 - 404")
    void deleteMemorialReplyNotFoundException() throws Exception {

        MemorialReplyPasswordRequest request =
                MemorialReplyPasswordRequest
                        .builder()
                        .replyPassword(REPLY_PASSWORD)
                        .build();

        doThrow(new MemorialReplyNotFoundException(MAX_REPLY_SEQUENCE))
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, MAX_REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(NOT_FOUND))
                .andExpect(jsonPath("$.message").value(REPLY_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 유효하지 않은 댓글 번호를 요청한 경우 - 400")
    void deleteInvalidReplySeqException() throws Exception {

        MemorialReplyPasswordRequest request =
                MemorialReplyPasswordRequest
                        .builder()
                        .replyPassword(REPLY_PASSWORD)
                        .build();

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, INVALID_REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(REPLY_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 존재하지 않는 게시글을 요청한 경우 - 404")
    void deleteMemorialNotFoundException() throws Exception {

        MemorialReplyPasswordRequest request =
                MemorialReplyPasswordRequest
                        .builder()
                        .replyPassword(REPLY_PASSWORD)
                        .build();

        doThrow(new MemorialNotFoundException(MAX_DONATE_SEQUENCE))
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}",
                        MAX_DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(NOT_FOUND))
                .andExpect(jsonPath("$.message").value(MEMORIAL_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    void deleteInvalidDonateSeqException() throws Exception {

        MemorialReplyPasswordRequest request =
                MemorialReplyPasswordRequest
                        .builder()
                        .replyPassword(REPLY_PASSWORD)
                        .build();

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}",
                        INVALID_DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(DONATE_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 이미 삭제된 댓글을 다시 삭제하거나 수정하려는 경우 - 409")
    void deleteReplyAlreadyDeleteException() throws Exception {

        MemorialReplyPasswordRequest request =
                MemorialReplyPasswordRequest
                        .builder()
                        .replyPassword(REPLY_PASSWORD)
                        .build();

        doThrow(new ReplyAlreadyDeleteException(REPLY_SEQUENCE))
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}",
                        DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CONFLICT))
                .andExpect(jsonPath("$.message").value(REPLY_ALREADY_DELETED_MESSAGE));
    }

}
