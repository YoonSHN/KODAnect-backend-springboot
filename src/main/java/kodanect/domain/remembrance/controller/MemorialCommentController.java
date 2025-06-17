package kodanect.domain.remembrance.controller;

import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.domain.remembrance.dto.MemorialCommentCreateRequest;
import kodanect.domain.remembrance.dto.MemorialCommentPasswordRequest;
import kodanect.domain.remembrance.dto.MemorialCommentResponse;
import kodanect.domain.remembrance.dto.MemorialCommentUpdateRequest;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.service.MemorialCommentService;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import kodanect.common.response.ApiResponse;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static kodanect.common.exception.config.MessageKeys.DONATE_INVALID;
import static kodanect.common.exception.config.MessageKeys.COMMENT_INVALID;
import static kodanect.common.validation.PaginationValidator.validatePagination;

/**
 *
 * 기증자 추모관 댓글 관련 컨트롤러
 *
 * */
@RestController
@Validated
@RequestMapping("/remembrance/{donateSeq}/comment")
public class MemorialCommentController {

    private final MemorialCommentService memorialCommentService;
    private final MessageSourceAccessor messageSourceAccessor;

    public MemorialCommentController(MemorialCommentService memorialCommentService, MessageSourceAccessor messageSourceAccessor){
        this.memorialCommentService = memorialCommentService;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    /**
     *
     * 기증자 추모관 댓글 리스트 더 보기 요청 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param cursor 조회할 페이지 번호
     * @param size 조회할 페이지 사이즈
     *
     * */
    @GetMapping
    public ResponseEntity<ApiResponse<CursorCommentPaginationResponse<MemorialCommentResponse, Integer>>> getMoreReplies(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @RequestParam Integer cursor, @RequestParam(defaultValue = "3") int size)
            throws InvalidPaginationException,
                    MemorialNotFoundException
    {
        /* 댓글 더보기 */

        /* 페이징 요청 검증 */
        validatePagination(cursor, size);

        String successMessage = messageSourceAccessor.getMessage("board.comment.read.success", new Object[] {});
        CursorCommentPaginationResponse<MemorialCommentResponse, Integer> memorialReplyResponses = memorialCommentService.getMoreCommentList(donateSeq, cursor, size);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage, memorialReplyResponses));
    }

    /**
     *
     * 기증자 추모관 댓글 생성 메서드
     *
     * @param donateSeq 댓글 생성할 게시글 번호
     * @param memorialCommentCreateRequest 댓글 생성 요청 dto
     *
     * */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createMemorialReply(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @RequestBody @Valid MemorialCommentCreateRequest memorialCommentCreateRequest)
            throws  MemorialNotFoundException
    {
        /* 게시글 댓글 작성 */

        String successMessage = messageSourceAccessor.getMessage("board.comment.create.success", new Object[] {});
        memorialCommentService.createComment(donateSeq, memorialCommentCreateRequest);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, successMessage));
    }

    /**
     *
     * 기증자 추모관 댓글 수정 메서드
     *
     * @param donateSeq 댓글 수정할 상세 게시글 번호
     * @param commentSeq 수정할 댓글 번호
     * @param memorialCommentUpdateRequest 댓글 수정 요청 dto
     *
     * */
    @PutMapping("/{commentSeq}")
    public ResponseEntity<ApiResponse<String>> updateMemorialComment(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @PathVariable @Min(value = 1, message = COMMENT_INVALID) Integer commentSeq,
            @RequestBody @Valid MemorialCommentUpdateRequest memorialCommentUpdateRequest)
            throws  MemorialCommentNotFoundException,
                    MemorialNotFoundException,
                    CommentAlreadyDeleteException
    {
        /* 게시글 댓글 수정 */

        String successMessage = messageSourceAccessor.getMessage("board.comment.update.success", new Object[] {});
        memorialCommentService.updateComment(donateSeq, commentSeq, memorialCommentUpdateRequest);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }

    /**
     *
     * 기증자 추모관 댓글 삭제 메서드
     *
     * @param donateSeq 댓글 삭제할 상세 게시글 번호
     * @param commentSeq 삭제할 댓글 번호
     * @param memorialCommentPasswordRequest 댓글 삭제 요청 dto
     *
     * */
    @DeleteMapping("/{commentSeq}")
    public ResponseEntity<ApiResponse<String>> deleteMemorialComment(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @PathVariable @Min(value = 1, message = COMMENT_INVALID) Integer commentSeq,
            @RequestBody @Valid MemorialCommentPasswordRequest memorialCommentPasswordRequest)
            throws  CommentPasswordMismatchException,
                    MemorialCommentNotFoundException,
                    MemorialNotFoundException,
                    CommentAlreadyDeleteException
    {
        /* 게시글 댓글 삭제 - 소프트 삭제 */

        String successMessage = messageSourceAccessor.getMessage("board.comment.delete.success", new Object[] {});
        memorialCommentService.deleteComment(donateSeq, commentSeq, memorialCommentPasswordRequest);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }

    /**
     *
     * 기증자 추모관 댓글 비밀번호 검증 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param commentSeq 댓글 번호
     * @param memorialCommentPasswordRequest 비밀번호 검증 dto
     *
     * */
    @PostMapping("/{commentSeq}")
    public ResponseEntity<ApiResponse<String>> varifyMemorialComment(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @PathVariable @Min(value = 1, message = COMMENT_INVALID) Integer commentSeq,
            @RequestBody @Valid MemorialCommentPasswordRequest memorialCommentPasswordRequest)
            throws  CommentPasswordMismatchException,
                    MemorialCommentNotFoundException,
                    MemorialNotFoundException,
                    CommentAlreadyDeleteException
    {
        /* 댓글 비밀번호 검증 */

        String successMessage = messageSourceAccessor.getMessage("board.comment.varify.success", new Object[] {});
        memorialCommentService.varifyComment(donateSeq, commentSeq, memorialCommentPasswordRequest);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }
}
