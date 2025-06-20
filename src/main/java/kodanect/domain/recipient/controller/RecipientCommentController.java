package kodanect.domain.recipient.controller;

import kodanect.common.exception.config.SecureLogger;
import kodanect.common.response.ApiResponse;
import kodanect.domain.recipient.dto.RecipientCommentAuthRequestDto;
import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.domain.recipient.dto.CommentDeleteRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.dto.RecipientCommentUpdateRequestDto;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
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

        recipientCommentService.insertComment(
                letterSeq, // 게시물 번호를 직접 서비스로 전달
                requestDto // DTO 객체 전달
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "댓글이 성공적으로 등록되었습니다.", null));
    }

    /** ## 댓글 인증 API (비밀번호 확인)
     *
     * **요청:** `POST /recipientLetters/{letterSeq}/comments/{commentSeq}/verifyPwd`
     * **파라미터:** `letterSeq` (Path Variable), `commentSeq` (Path Variable), `authRequestDto` (비밀번호)
     * **응답:** `ApiResponse<Boolean>` (성공 시)
     */
    @PostMapping("/{letterSeq}/comments/{commentSeq}/verifyPwd")
    public ResponseEntity<ApiResponse<Boolean>> verifyPwdComment(@PathVariable("letterSeq") Integer letterSeq,
                                                           @PathVariable("commentSeq") Integer commentSeq,
                                                           @Valid @RequestBody RecipientCommentAuthRequestDto authRequestDto) {
        try {
            logger.info("댓글 비밀번호 확인 요청: letterSeq={}, commentSeq={}", letterSeq, commentSeq);

            // 서비스 계층에서 비밀번호 검증 수행. 실패 시 RecipientInvalidPasscodeException 발생
            recipientCommentService.authenticateComment(commentSeq, authRequestDto.getCommentPasscode()); // authRequestDto 추가 전달

            // 성공 시, data 필드에 Boolean 타입 (true) 반환
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "댓글 인증에 성공했습니다.", null));

        } catch (RecipientInvalidPasscodeException e) {
            logger.warn("댓글 비밀번호 확인 실패: commentSeq={}, error={}", commentSeq, e.getMessage());
            // 비밀번호 확인 실패 시, data 필드에 아무것도 반환하지 않음
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED, e.getMessage()));
        } catch (Exception e) { // 기타 예상치 못한 예외 처리
            logger.error("댓글 비밀번호 확인 중 오류 발생: letterSeq={}, commentSeq={}, error={}", letterSeq, commentSeq, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 인증 중 오류가 발생했습니다."));
        }
    }

    /** ## 댓글 수정 API (인증 후 호출)
     *
     * **요청:** `PUT /recipientLetters/{letterSeq}/comments/{commentSeq}`
     * **파라미터:** `letterSeq` (Path Variable), `commentSeq` (Path Variable), `requestDto` (댓글 수정 내용)
     * **응답:** `ApiResponse<RecipientCommentResponseDto>`
     */
    @PutMapping("/{letterSeq}/comments/{commentSeq}")
    public ResponseEntity<ApiResponse<Object>> updateComment(@PathVariable("letterSeq") Integer letterSeq,
                                                        @PathVariable("commentSeq") Integer commentSeq,
                                                        @Valid @RequestBody RecipientCommentUpdateRequestDto requestDto)
    {
        logger.info("댓글 수정 요청 시작: commentSeq={}", commentSeq);

        try {
            recipientCommentService.updateComment(commentSeq, requestDto);
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "댓글이 성공적으로 수정되었습니다.", null));

        } catch (Exception e) {
            // 기타 예외 처리 (예: 댓글을 찾을 수 없는 경우)
            logger.error("댓글 수정 중 오류 발생: commentSeq={}, error={}", commentSeq, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 수정 중 오류가 발생했습니다."));
        }
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