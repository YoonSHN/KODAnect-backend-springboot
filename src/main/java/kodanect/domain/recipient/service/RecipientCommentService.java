package kodanect.domain.recipient.service;

import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.domain.recipient.dto.RecipientCommentRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;

public interface RecipientCommentService {
    // 댓글 작성
    RecipientCommentResponseDto insertComment(Integer letterSeq, RecipientCommentRequestDto requestDto);

    // 댓글 수정
    RecipientCommentResponseDto updateComment(Integer commentSeq, String newContents, String newWriter, String inputPasscode);

    // 댓글 삭제
    void deleteComment(Integer letterSeq, Integer commentSeq, String inputPasscode);

    // 특정 개시물의 페이징된 댓글 조회
    CursorReplyPaginationResponse<RecipientCommentResponseDto, Integer> selectPaginatedCommentsForRecipient(Integer letterSeq, Integer lastCommentId, Integer size);
}
