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

import java.util.NoSuchElementException;

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
                                                                    @RequestBody RecipientCommentEntity commentEntityRequest) {
        logger.info("POST /recipientLetters/{}/comments called with comment: {}", letterSeq, commentEntityRequest);
        try {
            // 댓글을 작성할 게시물 정보를 RecipientCommentVO에 설정
            // RecipientEntity 객체를 생성하여 letterSeq만 설정하고 RecipientCommentEntity의 letter 필드에 주입
            RecipientEntity parentLetter = RecipientEntity.builder().letterSeq(letterSeq).build();
            commentEntityRequest.setLetter(parentLetter);

            RecipientCommentResponseDto savedComment = recipientCommentService.insertComment(commentEntityRequest);
            logger.info("Comment successfully written with commentSeq: {}", savedComment.getCommentSeq());
            return new ResponseEntity<>(savedComment, HttpStatus.CREATED); // 201 Created
        } catch (IllegalArgumentException e) {
            logger.warn("Bad request for comment write: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Bad Request (예: 게시물 없음)
        } catch (NoSuchElementException e) { // 서비스에서 NoSuchElementException을 던질 수 있음
            logger.warn("Not Found for comment write: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        } catch (Exception e) {
            logger.error("Error writing comment for letterSeq {}: {}", letterSeq, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }

    // 댓글 수정
    @PutMapping("/{letterSeq}/comments/{commentSeq}")
    public ResponseEntity<RecipientCommentResponseDto> updateComment(@PathVariable("letterSeq") int letterSeq,
                                                            @PathVariable("commentSeq") int commentSeq,
                                                            @RequestBody RecipientCommentEntity commentEntityRequest) {
        logger.info("PUT /recipientLetters/{}/comments/{} called with commentVO: {}", letterSeq, commentSeq, commentEntityRequest);
        try {
            // URL의 commentSeq를 PathVariable에서 받아 Entity에 설정 (RequestBody에 없을 경우 대비)
            commentEntityRequest.setCommentSeq(commentSeq);

            // 비밀번호는 RequestBody에서 직접 받으므로 별도 파라미터로 넘기기
            String inputPassword = commentEntityRequest.getCommentPasscode();

            if (commentEntityRequest.getContents() == null || commentEntityRequest.getCommentPasscode() == null) {
                logger.warn("Missing required fields for comment update: contents or commentPasscode");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Bad Request
            }

            RecipientCommentResponseDto updatedComment = recipientCommentService.updateComment(commentEntityRequest, inputPassword);
            logger.info("Comment successfully updated for commentSeq: {}", updatedComment.getCommentSeq());
            return new ResponseEntity<>(updatedComment, HttpStatus.OK); // 200 OK
        } catch (IllegalArgumentException e) {
            logger.warn("Error updating comment {}: {}", commentSeq, e.getMessage());
            // 비밀번호 불일치: 403 Forbidden, 댓글 없음: 404 Not Found
            if (e.getMessage().contains("비밀번호가 일치하지 않습니다")) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
            } else if (e.getMessage().contains("댓글을 찾을 수 없거나 이미 삭제되었습니다")) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Bad Request
        } catch (Exception e) {
            logger.error("Error updating commentSeq {}: {}", commentSeq, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }

    // 댓글 삭제
    @DeleteMapping("/{letterSeq}/comments/{commentSeq}")
    public ResponseEntity<Void> deleteComment(@PathVariable("letterSeq") int letterSeq,
                                              @PathVariable("commentSeq") int commentSeq,
                                              @RequestBody RecipientCommentEntity commentEntityRequest) { // Entity를 요청으로 받음 (비밀번호 추출용)
        logger.info("DELETE /recipientLetters/{}/comments/{} called with commentEntityRequest: {}", letterSeq, commentSeq, commentEntityRequest);
        try {
            String commentPasscode = commentEntityRequest.getCommentPasscode();

            if (commentPasscode == null) {
                logger.warn("Missing required field for comment delete: commentPasscode");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Bad Request
            }

            recipientCommentService.deleteComment(commentSeq, commentPasscode);
            logger.info("Comment successfully deleted (logically) for commentSeq: {}", commentSeq);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        } catch (IllegalArgumentException e) {
            logger.warn("Error deleting comment {}: {}", commentSeq, e.getMessage());
            if (e.getMessage().contains("비밀번호가 일치하지 않습니다")) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 403 Forbidden
            } else if (e.getMessage().contains("댓글을 찾을 수 없거나 이미 삭제되었습니다")) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Bad Request
        } catch (Exception e) {
            logger.error("Error deleting commentSeq {}: {}", commentSeq, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }

}
