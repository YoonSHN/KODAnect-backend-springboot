package kodanect.domain.heaven.controller;

import kodanect.common.response.ApiResponse;
import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.domain.heaven.dto.request.HeavenCommentCreateRequest;
import kodanect.domain.heaven.dto.request.HeavenCommentUpdateRequest;
import kodanect.domain.heaven.dto.request.HeavenCommentVerifyRequest;
import kodanect.domain.heaven.dto.response.HeavenCommentResponse;
import kodanect.domain.heaven.service.HeavenCommentService;
import kodanect.domain.remembrance.dto.common.BlankGroup;
import kodanect.domain.remembrance.dto.common.PatternGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/heavenLetters/{letterSeq}/comments")
@Validated({BlankGroup.class, PatternGroup.class})
@RequiredArgsConstructor
public class HeavenCommentController {

    private final HeavenCommentService heavenCommentService;
    private final MessageSourceAccessor messageSourceAccessor;

    /* 댓글 더보기 */
    @GetMapping
    public ResponseEntity<ApiResponse<CursorCommentPaginationResponse<HeavenCommentResponse, Integer>>> getMoreCommentList(
            @PathVariable Integer letterSeq,
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "3") int size
    ) {
        CursorCommentPaginationResponse<HeavenCommentResponse, Integer> commentList = heavenCommentService.getMoreCommentList(letterSeq, cursor, size);

        String message = messageSourceAccessor.getMessage("board.comment.read.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, commentList));
    }

    /* 댓글 등록 */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createHeavenComment(
            @PathVariable Integer letterSeq,
            @RequestBody @Valid HeavenCommentCreateRequest heavenCommentCreateRequest
    ) {
        heavenCommentService.createHeavenComment(letterSeq, heavenCommentCreateRequest);

        String message = messageSourceAccessor.getMessage("board.comment.create.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, message));
    }

    /* 댓글 수정 인증 */
    @PostMapping("/{commentSeq}/verifyPwd")
    public ResponseEntity<ApiResponse<Void>> verifyHeavenCommentPasscode(
            @PathVariable Integer letterSeq,
            @PathVariable Integer commentSeq,
            @RequestBody @Valid HeavenCommentVerifyRequest heavenCommentVerifyRequest
    ) {
        heavenCommentService.verifyHeavenCommentPasscode(letterSeq, commentSeq, heavenCommentVerifyRequest);

        String message = messageSourceAccessor.getMessage("board.comment.verify.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message));
    }

    /* 댓글 수정 */
    @PutMapping("/{commentSeq}")
    public ResponseEntity<ApiResponse<Void>> updateHeavenComment(
            @PathVariable Integer letterSeq,
            @PathVariable Integer commentSeq,
            @RequestBody @Valid HeavenCommentUpdateRequest heavenCommentUpdateRequest
    ) {
        heavenCommentService.updateHeavenComment(letterSeq, commentSeq, heavenCommentUpdateRequest);

        String message = messageSourceAccessor.getMessage("board.comment.update.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message));
    }

    /* 댓글 삭제 */
    @DeleteMapping("/{commentSeq}")
    public ResponseEntity<ApiResponse<Void>> deleteHeavenComment(
            @PathVariable Integer letterSeq,
            @PathVariable Integer commentSeq,
            @RequestBody @Valid HeavenCommentVerifyRequest heavenCommentVerifyRequest
    ) {
        heavenCommentService.deleteHeavenComment(letterSeq, commentSeq, heavenCommentVerifyRequest);

        String message = messageSourceAccessor.getMessage("board.comment.delete.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message));
    }
}
