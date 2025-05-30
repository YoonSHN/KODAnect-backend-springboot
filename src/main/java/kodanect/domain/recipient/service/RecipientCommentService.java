package kodanect.domain.recipient.service;

import kodanect.domain.recipient.dto.RecipientCommentRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;

import java.util.List;

public interface RecipientCommentService {
    // 특정 게시물의 댓글 조회
    List<RecipientCommentResponseDto> selectRecipientCommentByLetterSeq(int letterSeq);

    // 댓글 작성
    // int letterSeq 파라미터 추가
    RecipientCommentResponseDto insertComment(int letterSeq, RecipientCommentRequestDto requestDto, String captchaToken);

    // 댓글 수정
    RecipientCommentResponseDto updateComment(int commentSeq, String newContents, String newWriter, String inputPasscode, String captchaToken);

    // 댓글 삭제
    void deleteComment(Integer letterSeq, Integer commentSeq, String inputPasscode, String captchaToken);
}
