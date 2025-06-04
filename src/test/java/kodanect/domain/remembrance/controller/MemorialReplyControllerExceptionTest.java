package kodanect.domain.remembrance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.exception.config.GlobalExcepHndlr;
import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.dto.MemorialReplyDeleteRequest;
import kodanect.domain.remembrance.dto.MemorialReplyUpdateRequest;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.service.MemorialReplyService;
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

import javax.ws.rs.core.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemorialReplyController.class)
@Import(GlobalExcepHndlr.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class MemorialReplyControllerExceptionTest {

    private static final Integer donateSeq = 1;
    private static final Integer replySeq = 1;
    private static final String BAD_REQUEST_MESSAGE = "잘못된 요청입니다.";
    private static final String NOT_FOUND_MESSAGE = "요청한 리소스를 찾을 수 없습니다.";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MemorialReplyService memorialReplyService;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    MessageSourceAccessor messageSourceAccessor;

    /*
    *
    * 댓글 더보기
    *
    * */

    @Test
    @DisplayName("댓글 더보기 : 존재하지 않는 게시글을 요청한 경우 - 404")
    public void moreMemorialNotFoundException() throws Exception {

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
    public void moreInvalidDonateSeqException() throws Exception {

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
    public void createMissingReplyContentException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replyContents("")
                        .replyPassword("1234")
                        .replyWriter("홍길동")
                        .build();

        doThrow(new MissingReplyContentException())
                .when(memorialReplyService)
                .createReply(anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", donateSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 작성자가 입력되지 않은 경우 - 400")
    public void createMissingReplyWriterException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .replyWriter("")
                        .build();

        doThrow(new MissingReplyWriterException())
                .when(memorialReplyService)
                .createReply(anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", donateSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 비밀번호가 입력되지 않은 경우 - 400")
    public void createMissingReplyPasswordException() throws Exception {

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replyContents("내용")
                        .replyPassword("")
                        .replyWriter("홍길동")
                        .build();

        doThrow(new MissingReplyPasswordException())
                .when(memorialReplyService)
                .createReply(anyInt(), any());

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", donateSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    public void createInvalidDonateSeqException() throws Exception {

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

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", donateSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 생성 : 존재하지 않는 게시글을 요청한 경우 - 404")
    public void createMemorialNotFoundException() throws Exception {

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

        mockMvc.perform(post("/remembrance/{donateSeq}/replies", donateSeq)
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
    public void updateReplyPostMismatchException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(replySeq)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .build();

        doThrow(new ReplyPostMismatchException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 요청된 댓글 ID가 URL 또는 본문과 일치하지 않는 경우 - 400")
    public void updateReplyIdMismatchException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(0)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .build();

        doThrow(new ReplyIdMismatchException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 댓글 비밀번호가 입력되지 않은 경우 - 400")
    public void updateMissingReplyPasswordException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(replySeq)
                        .replyContents("내용")
                        .replyPassword("")
                        .build();

        doThrow(new MissingReplyPasswordException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 댓글 비밀번호가 일치하지 않는 경우 - 403")
    public void updateReplyPasswordMismatchException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(replySeq)
                        .replyContents("내용")
                        .replyPassword("123")
                        .build();

        doThrow(new ReplyPasswordMismatchException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("댓글 수정 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    public void updateInvalidDonateSeqException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(replySeq)
                        .replyContents("")
                        .replyPassword("1234")
                        .build();

        doThrow(new InvalidDonateSeqException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", 0, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 내용이 입력되지 않은 경우 - 400")
    public void updateMissingReplyContentException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(replySeq)
                        .replyContents("")
                        .replyPassword("1234")
                        .build();

        doThrow(new MissingReplyContentException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 존재하지 않는 댓글을 요청한 경우 - 404")
    public void updateMemorialReplyNotFoundException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(0)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .build();

        doThrow(new MemorialReplyNotFoundException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 존재하지 않는 게시글을 요청한 경우 - 404")
    public void updateMemorialNotFoundException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(replySeq)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .build();

        doThrow(new MemorialNotFoundException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", 0, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 유효하지 않은 댓글 번호를 요청한 경우 - 400")
    public void updateInvalidReplySeqException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(0)
                        .replyContents("")
                        .replyPassword("1234")
                        .build();

        doThrow(new InvalidReplySeqException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 수정 : 이미 삭제된 댓글을 다시 삭제하거나 수정하려는 경우 - 409")
    public void updateReplyAlreadyDeleteException() throws Exception {

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(replySeq)
                        .replyContents("내용")
                        .replyPassword("1234")
                        .build();

        doThrow(new ReplyAlreadyDeleteException())
                .when(memorialReplyService)
                .updateReply(anyInt(), anyInt(), any());

        mockMvc.perform(put("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, replySeq)
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
    public void deleteReplyPostMismatchException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(replySeq)
                        .replyPassword("1234")
                        .build();

        doThrow(new ReplyPostMismatchException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", 0, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 요청된 댓글 ID가 URL 또는 본문과 일치하지 않는 경우 - 400")
    public void deleteReplyIdMismatchException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(0)
                        .replyPassword("1234")
                        .build();

        doThrow(new ReplyIdMismatchException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 비밀번호가 입력되지 않은 경우 - 400")
    public void deleteMissingReplyPasswordException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(replySeq)
                        .replyPassword("")
                        .build();

        doThrow(new MissingReplyPasswordException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 댓글 비밀번호가 일치하지 않는 경우 - 403")
    public void deleteReplyPasswordMismatchException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(replySeq)
                        .replyPassword("일치하지 않는 비밀번호")
                        .build();

        doThrow(new ReplyPasswordMismatchException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("댓글 삭제 : 존재하지 않는 댓글을 요청한 경우 - 404")
    public void deleteMemorialReplyNotFoundException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(0)
                        .replyPassword("1234")
                        .build();

        doThrow(new MemorialReplyNotFoundException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 존재하지 않는 게시글을 요청한 경우 - 404")
    public void deleteMemorialNotFoundException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(replySeq)
                        .replyPassword("1234")
                        .build();

        doThrow(new MemorialNotFoundException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", 0, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 유효하지 않은 댓글 번호를 요청한 경우 - 400")
    public void deleteInvalidReplySeqException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(0)
                        .replyPassword("1234")
                        .build();

        doThrow(new InvalidReplySeqException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", donateSeq, 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 유효하지 않은 게시글 번호를 요청한 경우 - 400")
    public void deleteInvalidDonateSeqException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(replySeq)
                        .replyPassword("1234")
                        .build();

        doThrow(new InvalidDonateSeqException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", 0, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(BAD_REQUEST_MESSAGE));
    }

    @Test
    @DisplayName("댓글 삭제 : 이미 삭제된 댓글을 다시 삭제하거나 수정하려는 경우 - 409")
    public void deleteReplyAlreadyDeleteException() throws Exception {

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(0)
                        .replySeq(replySeq)
                        .replyPassword("1234")
                        .build();

        doThrow(new ReplyAlreadyDeleteException())
                .when(memorialReplyService)
                .deleteReply(anyInt(), anyInt(), any());

        mockMvc.perform(delete("/remembrance/{donateSeq}/replies/{replySeq}", 0, replySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("해당 항목은 이미 삭제되었습니다."));
    }

}
