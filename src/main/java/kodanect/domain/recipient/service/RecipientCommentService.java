package kodanect.domain.recipient.service;

import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RecipientCommentService {
    // 특정 게시물의 댓글 조회
    @Transactional(readOnly = true)
    List<RecipientCommentResponseDto> selectRecipientCommentByLetterSeq(int letterSeq) throws Exception;

    // 댓글 작성
    @Transactional
    RecipientCommentResponseDto insertComment(RecipientCommentEntity commentEntityRequest) throws Exception;

    // 댓글 수정
    @Transactional
    RecipientCommentResponseDto updateComment(RecipientCommentEntity commentEntityRequest, String inputPassword) throws Exception;

    // 댓글 삭제
    @Transactional
    void deleteComment(int commentSeq, String inputPassword) throws Exception;

    // 댓글 비밀번호 확인
    @Transactional(readOnly = true)
    boolean verifyCommentPassword(int commentSeq, String inputPassword) throws Exception;
}
