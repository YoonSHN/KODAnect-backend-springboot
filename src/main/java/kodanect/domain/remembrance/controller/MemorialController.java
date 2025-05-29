package kodanect.domain.remembrance.controller;

import kodanect.common.response.ApiResponse;
import kodanect.domain.remembrance.dto.MemorialDetailResponse;
import kodanect.domain.remembrance.dto.MemorialListResponse;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.service.MemorialService;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<ApiResponse<Page<MemorialListResponse>>> getMemorialList(
            @RequestParam(defaultValue = "1") String page,
            @RequestParam(defaultValue = "20") String size)
            throws  MissingPaginationParameterException,
                    InvalidPaginationRangeException,
                    InvalidPaginationFormatException
    {
        /* 게시글 리스트 조회 */

        String successMessage = messageSourceAccessor.getMessage("board.read.success", new Object[] {});
        Page<MemorialListResponse> memorial = memorialService.getMemorialList(page, size);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage,memorial));
    }

    @GetMapping("/{donateSeq}")
    public ResponseEntity<ApiResponse<MemorialDetailResponse>> getMemorialByDonateSeq(
            @PathVariable Integer donateSeq)
            throws  MemorialNotFoundException,
                    InvalidDonateSeqException
    {
        /* 게시글 상세 조회 */

        String successMessage = messageSourceAccessor.getMessage("board.read.success", new Object[] {});
        MemorialDetailResponse memorial = memorialService.getMemorialByDonateSeq(donateSeq);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage, memorial));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<MemorialListResponse>>> getSearchMemorialList(
            @RequestParam(defaultValue = "1900-01-01") String startDate,
            @RequestParam(defaultValue = "2100-12-31") String endDate,
            @RequestParam(defaultValue = "") String searchWord,
            @RequestParam(defaultValue = "1") String page,
            @RequestParam(defaultValue = "20") String size)
            throws  MissingPaginationParameterException,
                    InvalidPaginationRangeException,
                    InvalidPaginationFormatException,
                    MissingSearchDateParameterException,
                    InvalidSearchDateFormatException,
                    InvalidSearchDateRangeException
    {
        /* 게시글 검색 조건 조회 */

        String successMessage = messageSourceAccessor.getMessage("board.search.read.success", new Object[] {});
        Page<MemorialListResponse> memorial = memorialService.getSearchMemorialList(page, size, startDate, endDate, searchWord);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage, memorial));
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

