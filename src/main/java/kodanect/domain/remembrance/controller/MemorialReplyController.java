package kodanect.domain.remembrance.controller;

import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.dto.MemorialReplyDeleteRequest;
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

    @PutMapping("/{replySeq}")
    public ResponseEntity<ApiResponse<String>> updateMemorialReply(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @PathVariable @Min(value = 1, message = REPLY_INVALID) Integer replySeq,
            @RequestBody @Valid MemorialReplyUpdateRequest memorialReplyUpdateRequest)
            throws  ReplyPasswordMismatchException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    ReplyAlreadyDeleteException
    {
        /* 게시글 댓글 수정 */

        String successMessage = messageSourceAccessor.getMessage("board.reply.update.success", new Object[] {});
        memorialReplyService.updateReply(donateSeq, replySeq, memorialReplyUpdateRequest);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }

    @DeleteMapping("/{replySeq}")
    public ResponseEntity<ApiResponse<String>> deleteMemorialReply(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @PathVariable @Min(value = 1, message = REPLY_INVALID) Integer replySeq,
            @RequestBody @Valid MemorialReplyDeleteRequest memorialReplyDeleteRequest)
            throws  ReplyPasswordMismatchException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    ReplyAlreadyDeleteException
    {
        /* 게시글 댓글 삭제 - 소프트 삭제 */

        String successMessage = messageSourceAccessor.getMessage("board.reply.delete.success", new Object[] {});
        memorialReplyService.deleteReply(donateSeq, replySeq, memorialReplyDeleteRequest);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }
}
