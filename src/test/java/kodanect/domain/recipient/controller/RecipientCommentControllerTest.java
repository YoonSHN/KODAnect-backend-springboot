package kodanect.domain.recipient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.domain.recipient.dto.CommentDeleteRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.service.RecipientCommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(RecipientCommentController.class)
class RecipientCommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipientCommentService recipientCommentService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MessageSourceAccessor messageSourceAccessor() {
            MessageSource messageSource = new ResourceBundleMessageSource();
            ((ResourceBundleMessageSource) messageSource).setBasename("messages"); // 필요시 messages.properties 지정
            return new MessageSourceAccessor(messageSource);
        }
    }

    @Test
    @DisplayName("댓글 목록 조회 성공 테스트")
    void testGetPaginatedComments() throws Exception {
        RecipientCommentResponseDto comment = new RecipientCommentResponseDto();
        comment.setCommentSeq(1);
        comment.setCommentContents("테스트 댓글");
        // 필요한 필드 셋팅 추가

        CursorReplyPaginationResponse<RecipientCommentResponseDto, Integer> pageResponse =
                CursorReplyPaginationResponse.<RecipientCommentResponseDto, Integer>builder()
                        .content(List.of(comment))
                        .replyNextCursor(1)
                        .replyHasNext(false)
                        .build();

        given(recipientCommentService.selectPaginatedCommentsForRecipient(1, null, 3))
                .willReturn(pageResponse);

        mockMvc.perform(get("/recipientLetters/1/comments")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].commentSeq").value(1))
                .andExpect(jsonPath("$.data.content[0].commentContents").value("테스트 댓글"));
    }

    @Test
    @DisplayName("댓글 작성 성공 테스트")
    void testWriteComment() throws Exception {
        RecipientCommentRequestDto requestDto = new RecipientCommentRequestDto();
        requestDto.setCommentContents("새 댓글");
        requestDto.setCommentWriter("작성자");
        requestDto.setCommentPasscode("asdf1234");

        RecipientCommentResponseDto responseDto = new RecipientCommentResponseDto();
        responseDto.setCommentSeq(1);
        responseDto.setCommentContents("새 댓글");
        responseDto.setCommentWriter("작성자");

        given(recipientCommentService.insertComment(1, requestDto)).willReturn(responseDto);

        mockMvc.perform(post("/recipientLetters/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.commentSeq").value(1))
                .andExpect(jsonPath("$.data.commentContents").value("새 댓글"));
    }

    @Test
    @DisplayName("댓글 수정 성공 테스트")
    void testUpdateComment() throws Exception {
        RecipientCommentRequestDto requestDto = new RecipientCommentRequestDto();
        requestDto.setCommentContents("수정된 댓글");
        requestDto.setCommentWriter("작성자");
        requestDto.setCommentPasscode("asdf1234");

        RecipientCommentResponseDto responseDto = new RecipientCommentResponseDto();
        responseDto.setCommentSeq(1);
        responseDto.setCommentContents("수정된 댓글");
        responseDto.setCommentWriter("작성자");

        given(recipientCommentService.updateComment(
                1,
                "수정된 댓글",
                "작성자",
                "asdf1234")).willReturn(responseDto);

        mockMvc.perform(put("/recipientLetters/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("댓글 삭제 성공 테스트")
    void testDeleteComment() throws Exception {
        CommentDeleteRequestDto requestDto = new CommentDeleteRequestDto();
        requestDto.setCommentPasscode("asdf1234");

        doNothing().when(recipientCommentService).deleteComment(1, 1, "asdf1234");

        mockMvc.perform(delete("/recipientLetters/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글이 성공적으로 삭제되었습니다."));
    }
  
}