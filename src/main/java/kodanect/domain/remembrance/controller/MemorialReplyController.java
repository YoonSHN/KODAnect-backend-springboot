package kodanect.domain.remembrance.controller;

import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.dto.MemorialReplyDeleteRequest;
import kodanect.domain.remembrance.dto.MemorialReplyUpdateRequest;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.service.MemorialReplyService;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import kodanect.common.response.ApiResponse;

@RestController
@RequestMapping("/remembrance/{donateSeq}/replies")
public class MemorialReplyController {

    private final MemorialReplyService memorialReplyService;
    private final MessageSourceAccessor messageSourceAccessor;

    public MemorialReplyController(MemorialReplyService memorialReplyService, MessageSourceAccessor messageSourceAccessor){
        this.memorialReplyService = memorialReplyService;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createMemorialReply(
            @PathVariable Integer donateSeq,
            @RequestBody MemorialReplyCreateRequest memorialReplyCreateRequest)
            throws  MissingReplyContentException,
                    MissingReplyWriterException,
                    MissingReplyPasswordException,
                    InvalidDonateSeqException,
                    MemorialNotFoundException
    {
        /* 게시글 댓글 작성 */

        String successMessage = messageSourceAccessor.getMessage("board.reply.create.success", new Object[] {});
        memorialReplyService.createReply(donateSeq, memorialReplyCreateRequest);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }

    @PutMapping("/{replySeq}")
    public ResponseEntity<ApiResponse<String>> updateMemorialReply(
            @PathVariable Integer donateSeq,
            @PathVariable Integer replySeq,
            @RequestBody MemorialReplyUpdateRequest memorialReplyUpdateRequest)
            throws  InvalidDonateSeqException,
                    MissingReplyContentException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    InvalidReplySeqException,
                    ReplyAlreadyDeleteException
    {
        /* 게시글 댓글 수정 */

        String successMessage = messageSourceAccessor.getMessage("board.reply.update.success", new Object[] {});
        memorialReplyService.updateReply(donateSeq, replySeq, memorialReplyUpdateRequest);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }

    @DeleteMapping("/{replySeq}")
    public ResponseEntity<ApiResponse<String>> deleteMemorialReply(
            @PathVariable Integer donateSeq,
            @PathVariable Integer replySeq,
            @RequestBody MemorialReplyDeleteRequest memorialReplyDeleteRequest)
            throws  ReplyPostMismatchException,
                    ReplyIdMismatchException,
                    MissingReplyPasswordException,
                    ReplyPasswordMismatchException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    InvalidReplySeqException,
                    InvalidDonateSeqException,
                    ReplyAlreadyDeleteException
    {
        /* 게시글 댓글 삭제 - 소프트 삭제 */

        String successMessage = messageSourceAccessor.getMessage("board.reply.delete.success", new Object[] {});
        memorialReplyService.deleteReply(donateSeq, replySeq, memorialReplyDeleteRequest);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }
}
