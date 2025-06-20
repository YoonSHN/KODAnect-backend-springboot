package kodanect.domain.recipient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.domain.recipient.dto.CommentDeleteRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.service.RecipientCommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import java.time.LocalDateTime;
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
        comment.setContents("테스트 댓글"); // DTO 필드는 contents 입니다.
        comment.setWriteTime(LocalDateTime.now());
        comment.setCommentWriter("테스트 작성자"); // 댓글 작성자

        CursorCommentPaginationResponse<RecipientCommentResponseDto, Integer> pageResponse =
                CursorCommentPaginationResponse.<RecipientCommentResponseDto, Integer>builder()
                        .content(List.of(comment))
                        .commentNextCursor(1)
                        .commentHasNext(false)
                        .build();

        // 서비스 목킹: letterSeq, lastCommentId(null), size=3
        given(recipientCommentService.selectPaginatedCommentsForRecipient(1, null, 3))
                .willReturn(pageResponse);

        mockMvc.perform(get("/recipientLetters/1/comments")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].commentSeq").value(1))
                // 여기를 commentContents -> contents 로 수정합니다.
                .andExpect(jsonPath("$.data.content[0].contents").value("테스트 댓글"))
                .andExpect(jsonPath("$.data.content[0].writeTime").exists())
                .andExpect(jsonPath("$.data.commentNextCursor").value(1)) // commentNextCursor 검증 추가
                .andExpect(jsonPath("$.data.commentHasNext").value(false)); // commentHasNext 검증 추가
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