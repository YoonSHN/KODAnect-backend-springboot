package kodanect.domain.remembrance.controller;

import kodanect.domain.remembrance.dto.MemorialReplyDto;
import kodanect.domain.remembrance.service.MemorialReplyService;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import kodanect.common.response.ApiResponse;

import java.util.Locale;

@RestController
@RequestMapping("/remembrance/{donateSeq}/replies")
public class MemorialReplyController {

    /* 상수 */
    private final int SUCCESS_RESPONSE_CODE = 200;

    private final MemorialReplyService memorialReplyService;
    private final MessageSource messageSource;

    public MemorialReplyController(MemorialReplyService memorialReplyService, MessageSource messageSource){
        this.memorialReplyService = memorialReplyService;
        this.messageSource = messageSource;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createMemorialReply(
            @PathVariable Integer donateSeq,
            @RequestBody MemorialReplyDto memorialReplyDto) throws Exception{
        /* 게시글 댓글 작성 */

        String successMessage = messageSource.getMessage("board.reply.create.success", null, Locale.getDefault());
        memorialReplyService.createReply(donateSeq, memorialReplyDto);
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_RESPONSE_CODE, successMessage));
    }

    @PutMapping("/{replySeq}")
    public ResponseEntity<ApiResponse<String>> updateMemorialReply(
            @PathVariable Integer donateSeq,
            @PathVariable Integer replySeq,
            @RequestBody MemorialReplyDto memorialReplyDto) throws Exception {
        /* 게시글 댓글 수정 */

        String successMessage = messageSource.getMessage("board.reply.update.success", null, Locale.getDefault());
        memorialReplyService.updateReply(donateSeq, replySeq, memorialReplyDto);
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_RESPONSE_CODE, successMessage));
    }

    @DeleteMapping("/{replySeq}")
    public ResponseEntity<ApiResponse<String>> deleteMemorialReply(
            @PathVariable Integer donateSeq,
            @PathVariable Integer replySeq,
            @RequestBody MemorialReplyDto memorialReplyDto) throws Exception {
        /* 게시글 댓글 삭제 - 소프트 삭제 */

        String successMessage = messageSource.getMessage("board.reply.delete.success", null, Locale.getDefault());
        memorialReplyService.deleteReply(donateSeq, replySeq, memorialReplyDto);
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_RESPONSE_CODE, successMessage));
    }
}
