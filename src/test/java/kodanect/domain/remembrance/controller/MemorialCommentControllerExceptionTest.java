package kodanect.domain.remembrance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.exception.config.GlobalExcepHndlr;
import kodanect.common.exception.config.MemorialExceptionHandler;
import kodanect.domain.remembrance.dto.MemorialCommentCreateRequest;
import kodanect.domain.remembrance.dto.MemorialCommentPasswordRequest;
import kodanect.domain.remembrance.dto.MemorialCommentUpdateRequest;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.service.MemorialCommentService;
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

@WebMvcTest(controllers = MemorialCommentController.class)
@Import(GlobalExcepHndlr.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class MemorialCommentControllerExceptionTest {

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
    private static final Integer COMMENT_SEQUENCE = 1;
    private static final Integer INVALID_COMMENT_SEQUENCE = -1;
    private static final Integer MAX_COMMENT_SEQUENCE = Integer.MAX_VALUE;
    private static final Integer CURSOR = 1;
    private static final Integer INVALID_CURSOR = -1;
    private static final int SIZE = 1;
    private static final int INVALID_SIZE = -1;
    private static final String EMPTY = "";
    private static final String CONTENTS = "내용";
    private static final String COMMENT_PASSWORD = "1234asdf";
    private static final String INVALID_COMMENT_PASSWORD = "1234!asdf";
    private static final String COMMENT_WRITER = "홍길동";
    private static final String INVALID_COMMENT_WRITER = "zi존홍! 길동";
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
    private static final String COMMENT_NOT_FOUND_MESSAGE =
            "해당 댓글을 찾을 수 없습니다. (commentSeq: 2,147,483,647)";
    private static final String COMMENT_INVALID_MESSAGE =
            "해당 댓글을 찾을 수 없습니다.";
    private static final String COMMENT_CONTENTS_EMPTY_MESSAGE =
            "댓글 내용을 입력해 주세요.";
    private static final String COMMENT_PASSWORD_INVALID_MESSAGE =
            "비밀번호는 영문과 숫자를 포함한 8~16자여야 합니다.";
    private static final String COMMENT_PASSWORD_EMPTY_MESSAGE =
            "비밀번호를 입력해 주세요.";
    private static final String COMMENT_PASSWORD_MISMATCH_MESSAGE =
            "댓글 비밀번호가 일치하지 않습니다. (commentSeq: 1)";
    private static final String COMMENT_WRITER_INVALID_MESSAGE =
            "작성자 닉네임 형식이 올바르지 않습니다.";
    private static final String COMMENT_WRITER_EMPTY_MESSAGE =
            "작성자 닉네임을 입력해 주세요.";
    private static final String COMMENT_ALREADY_DELETED_MESSAGE =
            "이미 삭제된 댓글입니다. (commentSeq: 1)";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MemorialCommentService memorialCommentService;

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
                .when(memorialCommentService)
                .getMoreCommentList(MAX_DONATE_SEQUENCE, CURSOR, SIZE);

        mockMvc.perform(get("/remembrance/{donateSeq}/comment", MAX_DONATE_SEQUENCE)
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
    void moreDonateSeqInvalidException() throws Exception {

        mockMvc.perform(get("/remembrance/{donateSeq}/comment", INVALID_DONATE_SEQUENCE)
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

        mockMvc.perform(get("/remembrance/{donateSeq}/comment", DONATE_SEQUENCE)
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
    void createMissingCommentContentException() throws Exception {

        MemorialCommentCreateRequest request =
                MemorialCommentCreateRequest
                        .builder()
                        .contents(EMPTY)
                        .commentPasscode(COMMENT_PASSWORD)
                        .commentWriter(COMMENT_WRITER)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/comment", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_CONTENTS_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 작성자가 입력되지 않은 경우 - 400")
    void createMissingCommentWriterException() throws Exception {

        MemorialCommentCreateRequest request =
                MemorialCommentCreateRequest
                        .builder()
                        .contents(CONTENTS)
                        .commentPasscode(COMMENT_PASSWORD)
                        .commentWriter(EMPTY)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/comment", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_WRITER_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 작성자 입력 형식이 올바르지 않은 경우 - 400")
    void createInvalidCommentWriterException() throws Exception {

        MemorialCommentCreateRequest request =
                MemorialCommentCreateRequest
                        .builder()
                        .contents(CONTENTS)
                        .commentPasscode(COMMENT_PASSWORD)
                        .commentWriter(INVALID_COMMENT_WRITER)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/comment", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_WRITER_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 비밀번호가 입력되지 않은 경우 - 400")
    void createMissingCommentPasscodeException() throws Exception {

        MemorialCommentCreateRequest request =
                MemorialCommentCreateRequest
                        .builder()
                        .contents(CONTENTS)
                        .commentPasscode(EMPTY)
                        .commentWriter(COMMENT_WRITER)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/comment", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_PASSWORD_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 비밀번호 입력 형식이 올바르지 않은 경우 - 400")
    void createInvalidCommentPasscodeException() throws Exception {

        MemorialCommentCreateRequest request =
                MemorialCommentCreateRequest
                        .builder()
                        .contents(CONTENTS)
                        .commentPasscode("1124a")
                        .commentWriter(COMMENT_WRITER)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/comment", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_PASSWORD_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    void createInvalidDonateSeqException() throws Exception {

        MemorialCommentCreateRequest request =
                MemorialCommentCreateRequest
                        .builder()
                        .contents(CONTENTS)
                        .commentPasscode(COMMENT_PASSWORD)
                        .commentWriter(COMMENT_WRITER)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/comment", INVALID_DONATE_SEQUENCE)
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

        MemorialCommentCreateRequest request =
                MemorialCommentCreateRequest
                        .builder()
                        .contents(CONTENTS)
                        .commentPasscode(COMMENT_PASSWORD)
                        .commentWriter(COMMENT_WRITER)
                        .build();

        doThrow(new MemorialNotFoundException(MAX_DONATE_SEQUENCE))
                .when(memorialCommentService)
                .createComment(anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/comment", MAX_DONATE_SEQUENCE)
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
     * 댓글 인증
     *
     * */

    @Test
    @DisplayName("댓글 인증 : 비밀번호가 입력되지 않은 경우 - 400")
    void varifyCommentPasscodeMissingException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(EMPTY)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_PASSWORD_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 인증 : 비밀번호가 일치하지 않는 경우 - 403")
    void varifyCommentPasswordMismatchException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(COMMENT_PASSWORD)
                        .build();

        doThrow(new CommentPasswordMismatchException(COMMENT_SEQUENCE))
                .when(memorialCommentService)
                .varifyComment(anyInt(), anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(FORBIDDEN))
                .andExpect(jsonPath("$.message").value(COMMENT_PASSWORD_MISMATCH_MESSAGE));
    }

    @Test
    @DisplayName("댓글 인증 : 비밀번호 형식이 올바르지 않은 경우 - 400")
    void updateCommentPasscodeInvalidException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(INVALID_COMMENT_PASSWORD)
                        .build();

        doThrow(new CommentPasswordMismatchException(COMMENT_SEQUENCE))
                .when(memorialCommentService)
                .varifyComment(anyInt(), anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_PASSWORD_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 인증 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    void varifyDonateSeqInvalidException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(INVALID_COMMENT_PASSWORD)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/comment/{commentSeq}",
                        INVALID_DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_PASSWORD_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 인증 : 존재하지 않은 게시글 번호를 요청한 경우 - 404")
    void varifyDonateSeqNotFoundException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(COMMENT_PASSWORD)
                        .build();

        doThrow(new MemorialNotFoundException(MAX_DONATE_SEQUENCE))
                .when(memorialCommentService)
                .varifyComment(anyInt(), anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/comment/{commentSeq}",
                        MAX_DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(NOT_FOUND))
                .andExpect(jsonPath("$.message").value(MEMORIAL_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 인증 : 유효하지 않은 댓글 번호를 요청한 경우 - 400")
    void varifyCommentSeqInvalidException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(COMMENT_PASSWORD)
                        .build();

        mockMvc.perform(post("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, INVALID_COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 인증 : 존재하지 않는 댓글을 요청한 경우 - 404")
    void varifyCommentNotFoundException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(COMMENT_PASSWORD)
                        .build();

        doThrow(new MemorialCommentNotFoundException(MAX_DONATE_SEQUENCE))
                .when(memorialCommentService)
                .varifyComment(anyInt(), anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, MAX_COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(NOT_FOUND))
                .andExpect(jsonPath("$.message").value(COMMENT_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 인증 : 이미 삭제된 댓글을 다시 삭제하거나 수정하려는 경우 - 409")
    void varifyCommentAlreadyDeleteException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(COMMENT_PASSWORD)
                        .build();

        doThrow(new CommentAlreadyDeleteException(COMMENT_SEQUENCE))
                .when(memorialCommentService)
                .varifyComment(anyInt(), anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/comment/{commentSeq}", DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CONFLICT))
                .andExpect(jsonPath("$.message").value(COMMENT_ALREADY_DELETED_MESSAGE));
    }

    /*
     *
     * 댓글 수정
     *
     * */

    @Test
    @DisplayName("댓글 수정 : 내용이 입력되지 않은 경우 - 400")
    void updateCommentContentMissingException() throws Exception {

        MemorialCommentUpdateRequest request =
                MemorialCommentUpdateRequest
                        .builder()
                        .commentWriter(COMMENT_WRITER)
                        .contents(EMPTY)
                        .build();

        mockMvc.perform(put("/remembrance/{donateSeq}/comment/{commentSeq}", DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_CONTENTS_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 작성자 닉네임이 입력되지 않은 경우 - 400")
    void updateCommentWriterMissingException() throws Exception {

        MemorialCommentUpdateRequest request =
                MemorialCommentUpdateRequest
                        .builder()
                        .commentWriter(EMPTY)
                        .contents(CONTENTS)
                        .build();

        mockMvc.perform(put("/remembrance/{donateSeq}/comment/{commentSeq}", DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_WRITER_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 작성자 닉네임이 올바르지 않은 경우 - 400")
    void updateCommentWriterInvalidException() throws Exception {

        MemorialCommentUpdateRequest request =
                MemorialCommentUpdateRequest
                        .builder()
                        .commentWriter(INVALID_COMMENT_WRITER)
                        .contents(CONTENTS)
                        .build();

        mockMvc.perform(put("/remembrance/{donateSeq}/comment/{commentSeq}", DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_WRITER_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    void updateDonateSeqInvalidException() throws Exception {

        MemorialCommentUpdateRequest request =
                MemorialCommentUpdateRequest
                        .builder()
                        .commentWriter(COMMENT_WRITER)
                        .contents(CONTENTS)
                        .build();

        mockMvc.perform(put("/remembrance/{donateSeq}/comment/{commentSeq}",
                        INVALID_DONATE_SEQUENCE, COMMENT_SEQUENCE)
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
    void updateDonateSeqNotFoundException() throws Exception {

        MemorialCommentUpdateRequest request =
                MemorialCommentUpdateRequest
                        .builder()
                        .commentWriter(COMMENT_WRITER)
                        .contents(CONTENTS)
                        .build();

        doThrow(new MemorialNotFoundException(MAX_DONATE_SEQUENCE))
                .when(memorialCommentService)
                .updateComment(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/comment/{commentSeq}",
                        MAX_DONATE_SEQUENCE, COMMENT_SEQUENCE)
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
    void updateCommentSeqInvalidException() throws Exception {

        MemorialCommentUpdateRequest request =
                MemorialCommentUpdateRequest
                        .builder()
                        .commentWriter(COMMENT_WRITER)
                        .contents(CONTENTS)
                        .build();

        mockMvc.perform(put("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, INVALID_COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 존재하지 않는 댓글을 요청한 경우 - 404")
    void updateCommentNotFoundException() throws Exception {

        MemorialCommentUpdateRequest request =
                MemorialCommentUpdateRequest
                        .builder()
                        .commentWriter(COMMENT_WRITER)
                        .contents(CONTENTS)
                        .build();

        doThrow(new MemorialCommentNotFoundException(MAX_DONATE_SEQUENCE))
                .when(memorialCommentService)
                .updateComment(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, MAX_COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(NOT_FOUND))
                .andExpect(jsonPath("$.message").value(COMMENT_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 이미 삭제된 댓글을 다시 삭제하거나 수정하려는 경우 - 409")
    void updateCommentAlreadyDeleteException() throws Exception {

        MemorialCommentUpdateRequest request =
                MemorialCommentUpdateRequest
                        .builder()
                        .commentWriter(COMMENT_WRITER)
                        .contents(CONTENTS)
                        .build();

        doThrow(new CommentAlreadyDeleteException(COMMENT_SEQUENCE))
                .when(memorialCommentService)
                .updateComment(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/comment/{commentSeq}", DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CONFLICT))
                .andExpect(jsonPath("$.message").value(COMMENT_ALREADY_DELETED_MESSAGE));
    }


    /*
     *
     * 댓글 삭제
     *
     * */

    @Test
    @DisplayName("댓글 삭제 : 비밀번호가 입력되지 않은 경우 - 400")
    void deleteMissingCommentPasscodeException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(EMPTY)
                        .build();

        mockMvc.perform(delete("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_PASSWORD_EMPTY_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 비밀번호 형식이 올바르지 않은 경우 - 400")
    void deleteInvalidCommentPasscodeException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(INVALID_COMMENT_PASSWORD)
                        .build();

        mockMvc.perform(delete("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_PASSWORD_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 댓글 비밀번호가 일치하지 않는 경우 - 403")
    void deleteCommentPasscodeMismatchException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(COMMENT_PASSWORD)
                        .build();

        doThrow(new CommentPasswordMismatchException(COMMENT_SEQUENCE))
                .when(memorialCommentService)
                .deleteComment(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(FORBIDDEN))
                .andExpect(jsonPath("$.message").value(COMMENT_PASSWORD_MISMATCH_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 존재하지 않는 댓글을 요청한 경우 - 404")
    void deleteMemorialCommentNotFoundException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(COMMENT_PASSWORD)
                        .build();

        doThrow(new MemorialCommentNotFoundException(MAX_COMMENT_SEQUENCE))
                .when(memorialCommentService)
                .deleteComment(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, MAX_COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(NOT_FOUND))
                .andExpect(jsonPath("$.message").value(COMMENT_NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 유효하지 않은 댓글 번호를 요청한 경우 - 400")
    void deleteInvalidCommentSeqException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(COMMENT_PASSWORD)
                        .build();

        mockMvc.perform(delete("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, INVALID_COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST))
                .andExpect(jsonPath("$.message").value(COMMENT_INVALID_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 존재하지 않는 게시글을 요청한 경우 - 404")
    void deleteMemorialNotFoundException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(COMMENT_PASSWORD)
                        .build();

        doThrow(new MemorialNotFoundException(MAX_DONATE_SEQUENCE))
                .when(memorialCommentService)
                .deleteComment(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/comment/{commentSeq}",
                        MAX_DONATE_SEQUENCE, COMMENT_SEQUENCE)
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

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(COMMENT_PASSWORD)
                        .build();

        mockMvc.perform(delete("/remembrance/{donateSeq}/comment/{commentSeq}",
                        INVALID_DONATE_SEQUENCE, COMMENT_SEQUENCE)
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
    void deleteCommentAlreadyDeleteException() throws Exception {

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode(COMMENT_PASSWORD)
                        .build();

        doThrow(new CommentAlreadyDeleteException(COMMENT_SEQUENCE))
                .when(memorialCommentService)
                .deleteComment(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/comment/{commentSeq}",
                        DONATE_SEQUENCE, COMMENT_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(CONFLICT))
                .andExpect(jsonPath("$.message").value(COMMENT_ALREADY_DELETED_MESSAGE));
    }

}
