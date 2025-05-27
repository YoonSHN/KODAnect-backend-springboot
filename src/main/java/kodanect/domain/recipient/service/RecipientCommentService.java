package kodanect.domain.recipient.service;

import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.entity.RecipientCommentEntity;

import java.util.List;

public interface RecipientCommentService {
    // 특정 게시물의 댓글 조회
    List<RecipientCommentResponseDto> selectRecipientCommentByLetterSeq(int letterSeq);

    // 댓글 작성
    RecipientCommentResponseDto insertComment(RecipientCommentEntity commentEntityRequest);

    // 댓글 수정
    RecipientCommentResponseDto updateComment(RecipientCommentEntity commentEntityRequest, String inputPassword);

    // 댓글 삭제
    void deleteComment(int commentSeq, String inputPassword);

    // 댓글 비밀번호 확인
    boolean verifyCommentPassword(int commentSeq, String inputPassword);
}
