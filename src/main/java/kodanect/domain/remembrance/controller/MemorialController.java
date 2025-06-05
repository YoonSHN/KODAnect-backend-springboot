package kodanect.domain.remembrance.controller;

import kodanect.common.response.ApiResponse;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.remembrance.dto.MemorialDetailResponse;
import kodanect.domain.remembrance.dto.MemorialResponse;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.service.MemorialService;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/remembrance")
public class MemorialController {

    private final MemorialService memorialService;
    private final MessageSourceAccessor messageSourceAccessor;

    public MemorialController(MemorialService memorialService, MessageSourceAccessor messageSourceAccessor){
        this.memorialService = memorialService;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CursorPaginationResponse<MemorialResponse>>> getMemorialList(
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "20") int size)
            throws  InvalidPaginationRangeException
    {
        /* 게시글 리스트 조회 */

        String successMessage = messageSourceAccessor.getMessage("board.read.success", new Object[] {});
        CursorPaginationResponse<MemorialResponse> memorialResponses = memorialService.getMemorialList(cursor, size);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage,memorialResponses));
    }

    @GetMapping("/{donateSeq}")
    public ResponseEntity<ApiResponse<MemorialDetailResponse>> getMemorialByDonateSeq(
            @PathVariable Integer donateSeq)
            throws  MemorialNotFoundException,
            InvalidDonateSeqException
    {
        /* 게시글 상세 조회 */

        String successMessage = messageSourceAccessor.getMessage("board.read.success", new Object[] {});
        MemorialDetailResponse memorialDetailResponse = memorialService.getMemorialByDonateSeq(donateSeq);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage, memorialDetailResponse));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorPaginationResponse<MemorialResponse>>> getSearchMemorialList(
            @RequestParam(defaultValue = "1900-01-01") String startDate,
            @RequestParam(defaultValue = "2100-12-31") String endDate,
            @RequestParam(defaultValue = "") String searchWord,
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "20") int size)
            throws  InvalidPaginationRangeException,
            MissingSearchDateParameterException,
            InvalidSearchDateFormatException,
            InvalidSearchDateRangeException
    {
        /* 게시글 검색 조건 조회 */

        String successMessage = messageSourceAccessor.getMessage("board.search.read.success", new Object[] {});
        CursorPaginationResponse<MemorialResponse> memorialResponses = memorialService.getSearchMemorialList(startDate, endDate, searchWord, cursor, size);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage, memorialResponses));
    }

    @PatchMapping("/{donateSeq}/{emotion}")
    public ResponseEntity<ApiResponse<String>> updateMemorialLikeCount(
            @PathVariable Integer donateSeq,
            @PathVariable String emotion)
            throws  InvalidEmotionTypeException,
            MemorialNotFoundException,
            InvalidDonateSeqException
    {
        /* 이모지 카운트 수 업데이트 */
        /* flower, love, see, miss, proud, hard, sad */

        String successMessage = messageSourceAccessor.getMessage("board.emotion.update.success", new Object[] {});
        memorialService.emotionCountUpdate(donateSeq, emotion);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }
}

