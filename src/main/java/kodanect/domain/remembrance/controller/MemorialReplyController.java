package kodanect.domain.remembrance.controller;

import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.dto.MemorialReplyPasswordRequest;
import kodanect.domain.remembrance.dto.MemorialReplyResponse;
import kodanect.domain.remembrance.dto.MemorialReplyUpdateRequest;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.service.MemorialReplyService;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import kodanect.common.response.ApiResponse;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static kodanect.common.exception.config.MessageKeys.DONATE_INVALID;
import static kodanect.common.exception.config.MessageKeys.REPLY_INVALID;
import static kodanect.common.validation.PaginationValidator.validatePagination;

/**
 *
 * 기증자 추모관 댓글 관련 컨트롤러
 *
 * */
@RestController
@Validated
@RequestMapping("/remembrance/{donateSeq}/replies")
public class MemorialReplyController {

    private final MemorialReplyService memorialReplyService;
    private final MessageSourceAccessor messageSourceAccessor;

    public MemorialReplyController(MemorialReplyService memorialReplyService, MessageSourceAccessor messageSourceAccessor){
        this.memorialReplyService = memorialReplyService;
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
    public ResponseEntity<ApiResponse<CursorReplyPaginationResponse<MemorialReplyResponse, Integer>>> getMoreReplies(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @RequestParam Integer cursor, @RequestParam(defaultValue = "3") int size)
            throws  InvalidPaginationRangeException,
                    MemorialNotFoundException
    {
        /* 댓글 더보기 */

        /* 페이징 요청 검증 */
        validatePagination(cursor, size);

        String successMessage = messageSourceAccessor.getMessage("board.reply.read.success", new Object[] {});
        CursorReplyPaginationResponse<MemorialReplyResponse, Integer> memorialReplyResponses = memorialReplyService.getMoreReplyList(donateSeq, cursor, size);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage, memorialReplyResponses));
    }

    /**
     *
     * 기증자 추모관 댓글 생성 메서드
     *
     * @param donateSeq 댓글 생성할 게시글 번호
     * @param memorialReplyCreateRequest 댓글 생성 요청 dto
     *
     * */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createMemorialReply(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @RequestBody @Valid MemorialReplyCreateRequest memorialReplyCreateRequest)
            throws  MemorialNotFoundException
    {
        /* 게시글 댓글 작성 */

        String successMessage = messageSourceAccessor.getMessage("board.reply.create.success", new Object[] {});
        memorialReplyService.createReply(donateSeq, memorialReplyCreateRequest);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }

    /**
     *
     * 기증자 추모관 댓글 수정 메서드
     *
     * @param donateSeq 댓글 수정할 상세 게시글 번호
     * @param replySeq 수정할 댓글 번호
     * @param memorialReplyUpdateRequest 댓글 수정 요청 dto
     *
     * */
    @PutMapping("/{replySeq}")
    public ResponseEntity<ApiResponse<String>> updateMemorialReply(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @PathVariable @Min(value = 1, message = REPLY_INVALID) Integer replySeq,
            @RequestBody @Valid MemorialReplyUpdateRequest memorialReplyUpdateRequest)
            throws  MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    ReplyAlreadyDeleteException
    {
        /* 게시글 댓글 수정 */

        String successMessage = messageSourceAccessor.getMessage("board.reply.update.success", new Object[] {});
        memorialReplyService.updateReply(donateSeq, replySeq, memorialReplyUpdateRequest);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }

    /**
     *
     * 기증자 추모관 댓글 삭제 메서드
     *
     * @param donateSeq 댓글 삭제할 상세 게시글 번호
     * @param replySeq 삭제할 댓글 번호
     * @param request 입력한 비밀번호
     *
     * */
    @DeleteMapping("/{replySeq}")
    public ResponseEntity<ApiResponse<String>> deleteMemorialReply(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @PathVariable @Min(value = 1, message = REPLY_INVALID) Integer replySeq,
            @RequestBody @Valid MemorialReplyPasswordRequest request)
            throws  ReplyPasswordMismatchException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    ReplyAlreadyDeleteException
    {
        /* 게시글 댓글 삭제 - 소프트 삭제 */

        String successMessage = messageSourceAccessor.getMessage("board.reply.delete.success", new Object[] {});
        memorialReplyService.deleteReply(donateSeq, replySeq, request.getReplyPassword());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }

    /**
     *
     * 기증자 추모관 댓글 비밀번호 인증 메서드
     *
     * @param donateSeq 비밀번호 인증할 상세 게시글 번호
     * @param replySeq 비밀번호 인증할 댓글 번호
     * @param request 입력한 비밀번호
     * */
    @PostMapping("/{replySeq}")
    public ResponseEntity<ApiResponse<String>> verifyReplyPassword(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @PathVariable @Min(value = 1, message = REPLY_INVALID) Integer replySeq,
            @RequestBody @Valid MemorialReplyPasswordRequest request)
            throws  ReplyPasswordMismatchException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    ReplyAlreadyDeleteException

    {
        String successMessage = messageSourceAccessor.getMessage("board.reply.verify.success", new Object[] {});
        memorialReplyService.verifyReplyPassword(donateSeq, replySeq, request.getReplyPassword());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }
}
