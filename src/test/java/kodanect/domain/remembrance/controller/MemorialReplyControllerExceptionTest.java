package kodanect.domain.remembrance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.exception.config.GlobalExcepHndlr;
import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.dto.MemorialReplyDeleteRequest;
import kodanect.domain.remembrance.dto.MemorialReplyUpdateRequest;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.service.MemorialReplyService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.test.web.servlet.MockMvc;

import javax.ws.rs.core.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemorialReplyController.class)
@Import(GlobalExcepHndlr.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class MemorialReplyControllerExceptionTest {

    private static final Integer DONATE_SEQUENCE = 1;
    private static final Integer REPLY_SEQUENCE = 1;
    private static final String BAD_REQUEST_MESSAGE = "잘못된 요청입니다.";
    private static final String NOT_FOUND_MESSAGE = "요청한 자원을 찾을 수 없습니다.";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MemorialReplyService memorialReplyService;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    MessageSourceAccessor messageSourceAccessor;

    @BeforeEach
    void setUp() {
        when(messageSourceAccessor.getMessage("error.notfound", "요청한 자원을 찾을 수 없습니다."))
                .thenReturn("요청한 자원을 찾을 수 없습니다.");

        when(messageSourceAccessor.getMessage("error.internal", "서버 내부 오류가 발생했습니다."))
                .thenReturn("서버 내부 오류가 발생했습니다.");

        when(messageSourceAccessor.getMessage("reply.password.mismatch"))
                .thenReturn("비밀번호가 일치하지 않습니다.");

        when(messageSourceAccessor.getMessage("error.notfound"))
                .thenReturn("요청한 자원을 찾을 수 없습니다.");
    }

    /*
    *
    * 댓글 더보기
    *
    * */

    @Test
    @DisplayName("댓글 더보기 : 존재하지 않는 게시글을 요청한 경우 - 404")
    void moreMemorialNotFoundException() throws Exception {

        Integer cursor = 1;
        int size = 3;

        doThrow(new MemorialNotFoundException())
                .when(memorialReplyService)
                .getMoreReplyList(0, cursor, size);

        mockMvc.perform(get("/remembrance/{donateSeq}/replies", 0)
                        .param("cursor", String.valueOf(cursor))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 더보기 : 유효하지 않은 게시글 번호를 요청한 경우 - 404")
    void moreInvalidDonateSeqException() throws Exception {

        Integer cursor = 1;
        int size = 3;

        doThrow(new InvalidDonateSeqException())
                .when(memorialReplyService)
                .getMoreReplyList(0, cursor, size);

        mockMvc.perform(get("/remembrance/{donateSeq}/replies", 0)
                        .param("cursor", String.valueOf(cursor))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
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
                        .donateSeq(DONATE_SEQUENCE)
                        .replyContents("")
                        .replyPassword("1234")
                        .replyWriter("홍길동")
                        .build();

        doThrow(new MissingReplyContentException())
                .when(memorialReplyService)
                .createReply(anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 작성자가 입력되지 않은 경우 - 400")
    void createMissingReplyWriterException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .replyWriter("")
                        .build();

        doThrow(new MissingReplyWriterException())
                .when(memorialReplyService)
                .createReply(anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 비밀번호가 입력되지 않은 경우 - 400")
    void createMissingReplyPasswordException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replyContents("내용")
                        .replyPassword("")
                        .replyWriter("홍길동")
                        .build();

        doThrow(new MissingReplyPasswordException())
                .when(memorialReplyService)
                .createReply(anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    void createInvalidDonateSeqException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .donateSeq(0)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .replyWriter("홍길동")
                        .build();

        doThrow(new InvalidDonateSeqException())
                .when(memorialReplyService)
                .createReply(anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 존재하지 않는 게시글을 요청한 경우 - 404")
    void createMemorialNotFoundException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .donateSeq(0)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .replyWriter("홍길동")
                        .build();

        doThrow(new MemorialNotFoundException())
                .when(memorialReplyService)
                .createReply(anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", DONATE_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    /*
    *
    * 댓글 수정
    *
    * */

    @Test
    @DisplayName("댓글 수정 : 댓글이 해당 게시글에 속하지 않는 경우 (게시글 ID 불일치) - 400")
    void updateReplyPostMismatchException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(REPLY_SEQUENCE)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .build();

        doThrow(new ReplyPostMismatchException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 요청된 댓글 ID가 URL 또는 본문과 일치하지 않는 경우 - 400")
    void updateReplyIdMismatchException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replySeq(0)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .build();

        doThrow(new ReplyIdMismatchException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 댓글 비밀번호가 입력되지 않은 경우 - 400")
    void updateMissingReplyPasswordException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replySeq(REPLY_SEQUENCE)
                        .replyContents("내용")
                        .replyPassword("")
                        .build();

        doThrow(new MissingReplyPasswordException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 댓글 비밀번호가 일치하지 않는 경우 - 403")
    void updateReplyPasswordMismatchException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replySeq(REPLY_SEQUENCE)
                        .replyContents("내용")
                        .replyPassword("123")
                        .build();

        doThrow(new ReplyPasswordMismatchException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("댓글 수정 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    void updateInvalidDonateSeqException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(REPLY_SEQUENCE)
                        .replyContents("")
                        .replyPassword("1234")
                        .build();

        doThrow(new InvalidDonateSeqException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", 0, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 내용이 입력되지 않은 경우 - 400")
    void updateMissingReplyContentException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replySeq(REPLY_SEQUENCE)
                        .replyContents("")
                        .replyPassword("1234")
                        .build();

        doThrow(new MissingReplyContentException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 존재하지 않는 댓글을 요청한 경우 - 404")
    void updateMemorialReplyNotFoundException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replySeq(0)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .build();

        doThrow(new MemorialReplyNotFoundException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 존재하지 않는 게시글을 요청한 경우 - 404")
    void updateMemorialNotFoundException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(REPLY_SEQUENCE)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .build();

        doThrow(new MemorialNotFoundException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", 0, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 유효하지 않은 댓글 번호를 요청한 경우 - 400")
    void updateInvalidReplySeqException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replySeq(0)
                        .replyContents("")
                        .replyPassword("1234")
                        .build();

        doThrow(new InvalidReplySeqException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 이미 삭제된 댓글을 다시 삭제하거나 수정하려는 경우 - 409")
    void updateReplyAlreadyDeleteException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replySeq(REPLY_SEQUENCE)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .build();

        doThrow(new ReplyAlreadyDeleteException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("해당 항목은 이미 삭제되었습니다."));
    }


    /*
    *
    * 댓글 삭제
    *
    * */

    @Test
    @DisplayName("댓글 삭제 : 댓글이 해당 게시글에 속하지 않는 경우 (게시글 ID 불일치) - 400")
    void deleteReplyPostMismatchException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(REPLY_SEQUENCE)
                        .replyPassword("1234")
                        .build();

        doThrow(new ReplyPostMismatchException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", 0, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 요청된 댓글 ID가 URL 또는 본문과 일치하지 않는 경우 - 400")
    void deleteReplyIdMismatchException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replySeq(0)
                        .replyPassword("1234")
                        .build();

        doThrow(new ReplyIdMismatchException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 비밀번호가 입력되지 않은 경우 - 400")
    void deleteMissingReplyPasswordException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replySeq(REPLY_SEQUENCE)
                        .replyPassword("")
                        .build();

        doThrow(new MissingReplyPasswordException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 댓글 비밀번호가 일치하지 않는 경우 - 403")
    void deleteReplyPasswordMismatchException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replySeq(REPLY_SEQUENCE)
                        .replyPassword("일치하지 않는 비밀번호")
                        .build();

        doThrow(new ReplyPasswordMismatchException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("댓글 삭제 : 존재하지 않는 댓글을 요청한 경우 - 404")
    void deleteMemorialReplyNotFoundException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replySeq(0)
                        .replyPassword("1234")
                        .build();

        doThrow(new MemorialReplyNotFoundException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 존재하지 않는 게시글을 요청한 경우 - 404")
    void deleteMemorialNotFoundException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(REPLY_SEQUENCE)
                        .replyPassword("1234")
                        .build();

        doThrow(new MemorialNotFoundException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", 0, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 유효하지 않은 댓글 번호를 요청한 경우 - 400")
    void deleteInvalidReplySeqException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(DONATE_SEQUENCE)
                        .replySeq(0)
                        .replyPassword("1234")
                        .build();

        doThrow(new InvalidReplySeqException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", DONATE_SEQUENCE, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    void deleteInvalidDonateSeqException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(REPLY_SEQUENCE)
                        .replyPassword("1234")
                        .build();

        doThrow(new InvalidDonateSeqException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", 0, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 이미 삭제된 댓글을 다시 삭제하거나 수정하려는 경우 - 409")
    void deleteReplyAlreadyDeleteException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(REPLY_SEQUENCE)
                        .replyPassword("1234")
                        .build();

        doThrow(new ReplyAlreadyDeleteException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", 0, REPLY_SEQUENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("해당 항목은 이미 삭제되었습니다."));
    }

}
