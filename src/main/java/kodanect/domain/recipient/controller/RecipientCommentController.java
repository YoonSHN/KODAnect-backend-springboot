package kodanect.domain.recipient.controller;

import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.service.RecipientCommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/recipientLetters")
public class RecipientCommentController {

    private static final Logger logger = LoggerFactory.getLogger(RecipientCommentController.class);

    private final RecipientCommentService recipientCommentService;

    public RecipientCommentController(RecipientCommentService recipientCommentService) {
        this.recipientCommentService = recipientCommentService;
    }

    //  댓글 작성
    @PostMapping("/{letterSeq}/comments")
    public ResponseEntity<RecipientCommentResponseDto> writeComment(@PathVariable("letterSeq") int letterSeq,
                                                                    @Valid @RequestBody RecipientCommentEntity commentEntityRequest) {
        logger.info("POST /recipientLetters/{}/comments called with comment: {}", letterSeq, commentEntityRequest);

        // 댓글을 작성할 게시물
        // RecipientEntity 객체를 생성하여 letterSeq만 설정하고 RecipientCommentEntity의 letter 필드에 주입
        RecipientEntity parentLetter = RecipientEntity.builder().letterSeq(letterSeq).build();
        commentEntityRequest.setLetter(parentLetter);

        // 서비스 메서드는 이제 throws Exception 선언이 없으므로 try-catch에서 일반 Exception은 제거
        RecipientCommentResponseDto savedComment = recipientCommentService.insertComment(commentEntityRequest);
        logger.info("Comment successfully written with commentSeq: {}", savedComment.getCommentSeq());
        return new ResponseEntity<>(savedComment, HttpStatus.CREATED); // 201 Created
    }

    // 댓글 수정
    @PutMapping("/{letterSeq}/comments/{commentSeq}")
    public ResponseEntity<RecipientCommentResponseDto> updateComment(@PathVariable("letterSeq") int letterSeq,
                                                                     @PathVariable("commentSeq") int commentSeq,
                                                                     @Valid @RequestBody RecipientCommentEntity commentEntityRequest) {
        logger.info("PUT /recipientLetters/{}/comments/{} called with commentEntity: {}", letterSeq, commentSeq, commentEntityRequest);
        commentEntityRequest.setCommentSeq(commentSeq); // URL의 commentSeq를 PathVariable에서 받아 Entity에 설정

        // 비밀번호는 RequestBody에서 직접 받으므로 별도 파라미터로 넘기기
        // (commentEntityRequest 내에 이미 있으므로 별도 추출 필요 없음, 서비스에서 사용)
        String inputPassword = commentEntityRequest.getCommentPasscode();

        // Bean Validation(@Valid)이 여기서 먼저 처리되므로, 수동으로 null/empty 체크는 불필요
        // 단, 특정 필드만 업데이트 되는 부분이라면 (patch) 수동 체크가 필요할 수도 있음.
        // 여기서는 PUT이므로 전체 필드를 교체한다고 가정.

        RecipientCommentResponseDto updatedComment = recipientCommentService.updateComment(commentEntityRequest, inputPassword);
        logger.info("Comment successfully updated for commentSeq: {}", updatedComment.getCommentSeq());
        return new ResponseEntity<>(updatedComment, HttpStatus.OK); // 200 OK
    }


    // 댓글 삭제
    @DeleteMapping("/{letterSeq}/comments/{commentSeq}")
    public ResponseEntity<Void> deleteComment(@PathVariable("letterSeq") int letterSeq,
                                              @PathVariable("commentSeq") int commentSeq,
                                              @Valid @RequestBody RecipientCommentEntity commentEntityRequest) { // Entity를 요청으로 받음 (비밀번호 추출용)
        logger.info("DELETE /recipientLetters/{}/comments/{} called with commentEntityRequest: {}", letterSeq, commentSeq, commentEntityRequest);
        String commentPasscode = commentEntityRequest.getCommentPasscode();

        // Bean Validation(@Valid)이 여기서 먼저 처리되므로, 수동으로 null 체크는 불필요
        // if (commentPasscode == null) { ... }

        recipientCommentService.deleteComment(commentSeq, commentPasscode);
        logger.info("Comment successfully deleted (logically) for commentSeq: {}", commentSeq);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
    }

}
