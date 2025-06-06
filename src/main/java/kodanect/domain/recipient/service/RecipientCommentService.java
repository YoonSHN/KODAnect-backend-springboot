package kodanect.domain.recipient.service;

import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.domain.recipient.dto.RecipientCommentRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;

public interface RecipientCommentService {
    // 댓글 작성
    RecipientCommentResponseDto insertComment(int letterSeq, RecipientCommentRequestDto requestDto);

    // 댓글 수정
    RecipientCommentResponseDto updateComment(int commentSeq, String newContents, String newWriter, String inputPasscode);

    // 댓글 삭제
    void deleteComment(Integer letterSeq, Integer commentSeq, String inputPasscode);

    CursorReplyPaginationResponse<RecipientCommentResponseDto, Integer> selectPaginatedCommentsForRecipient(int letterSeq, Integer lastCommentId, int size);
}
