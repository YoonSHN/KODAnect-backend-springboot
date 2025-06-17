package kodanect.domain.recipient.controller;

import kodanect.common.exception.config.SecureLogger;
import kodanect.common.response.ApiResponse;
import kodanect.domain.recipient.dto.RecipientCommentAuthRequestDto;
import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.domain.recipient.dto.CommentDeleteRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.dto.RecipientCommentUpdateRequestDto;
import kodanect.domain.recipient.service.RecipientCommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/recipientLetters")
public class RecipientCommentController {

    private static final SecureLogger logger = SecureLogger.getLogger(RecipientCommentController.class);

    private final RecipientCommentService recipientCommentService;

    public RecipientCommentController(RecipientCommentService recipientCommentService) {
        this.recipientCommentService = recipientCommentService;
    }

    /** ## 특정 게시물의 "더보기" 댓글 조회 API (커서 기반 페이징 적용)

     **요청:** `GET /recipientLetters/{letterSeq}/comments`
     **파라미터:** `letterSeq` (Path Variable), `lastCommentId`, `size`
     **응답:** `ApiResponse<CursorReplyPaginationResponse<RecipientCommentResponseDto, Long>>`
     */
    @GetMapping("/{letterSeq}/comments")
    public ResponseEntity<ApiResponse<CursorCommentPaginationResponse<RecipientCommentResponseDto, Integer>>> getPaginatedCommentsForRecipient(
            @PathVariable("letterSeq") Integer letterSeq,
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "3") int size) {  // **댓글 한 번에 가져올 개수 (기본값 3)**
        logger.info("페이징된 댓글 조회 요청: letterSeq={}, lastCommentId={}, size={}", letterSeq, cursor, size);
        CursorCommentPaginationResponse<RecipientCommentResponseDto, Integer> responseData =
                recipientCommentService.selectPaginatedCommentsForRecipient(letterSeq, cursor, size);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "댓글 목록 조회 성공", responseData));
    }

    //  댓글 작성
    @PostMapping("/{letterSeq}/comments")
    public ResponseEntity<ApiResponse<RecipientCommentResponseDto>> writeComment(@PathVariable("letterSeq") Integer letterSeq,
                                                                                 @Valid @RequestBody RecipientCommentRequestDto requestDto) {

        RecipientCommentResponseDto createdComment = recipientCommentService.insertComment(
                letterSeq, // 게시물 번호를 직접 서비스로 전달
                requestDto // DTO 객체 전달
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "댓글이 성공적으로 등록되었습니다.", createdComment));
    }

    // 댓글 인증 API (비밀번호 확인)
    @PostMapping("/{letterSeq}/comments/{commentSeq}/verifyPwd")
    public ResponseEntity<ApiResponse<Void>> verifyPwdComment(@PathVariable("letterSeq") Integer letterSeq,
                                                              @PathVariable("commentSeq") Integer commentSeq,
                                                              @Valid @RequestBody RecipientCommentAuthRequestDto authRequestDto) {
        // 서비스에서 비밀번호 검증 수행. 실패 시 예외 발생 (컨트롤러에서 예외 처리)
        recipientCommentService.authenticateComment(commentSeq, authRequestDto.getCommentPasscode());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.NO_CONTENT, "댓글 인증에 성공했습니다.", null));
    }

    // 댓글 수정 API (인증 후 호출)
    @PutMapping("/{letterSeq}/comments/{commentSeq}")
    public ResponseEntity<ApiResponse<RecipientCommentResponseDto>> updateComment(@PathVariable("letterSeq") Integer letterSeq,
                                                                                  @PathVariable("commentSeq") Integer commentSeq,
                                                                                  @Valid @RequestBody RecipientCommentUpdateRequestDto requestDto) {
        // 비밀번호 인증은 이미 authenticate 엔드포인트를 통해 완료되었다고 가정
        RecipientCommentResponseDto updatedComment = recipientCommentService.updateComment(
                commentSeq, // 댓글 시퀀스
                requestDto.getContents(), // 업데이트할 내용
                requestDto.getCommentWriter() // 업데이트할 작성자
        );
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "댓글이 성공적으로 수정되었습니다.", updatedComment));
    }

    // 댓글 삭제
    @DeleteMapping("/{letterSeq}/comments/{commentSeq}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable("letterSeq") Integer letterSeq,
                                                           @PathVariable("commentSeq") Integer commentSeq,
                                                           @Valid @RequestBody CommentDeleteRequestDto requestDto) {
        logger.info("댓글 삭제 요청: letterSeq={}, commentSeq={}", letterSeq, commentSeq);

        // 서비스 계층으로 전달
        recipientCommentService.deleteComment(
                letterSeq,
                commentSeq,
                requestDto.getCommentPasscode()
        );

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "댓글이 성공적으로 삭제되었습니다."));
    }
}