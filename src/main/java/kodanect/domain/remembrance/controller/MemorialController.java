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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;

import static kodanect.common.exception.config.MessageKeys.DONATE_INVALID;
import static kodanect.common.validation.PaginationValidator.validatePagination;
import static kodanect.common.validation.SearchValidator.validateSearchDates;

/**
 *
 * 기증자 추모관 컨트롤러
 *
 * */
@RestController
@Validated
@RequestMapping("/remembrance")
public class MemorialController {

    private final MemorialService memorialService;
    private final MessageSourceAccessor messageSourceAccessor;

    public MemorialController(MemorialService memorialService, MessageSourceAccessor messageSourceAccessor){
        this.memorialService = memorialService;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    /**
     *
     * 기증자 추모관 게시물 목록 을 조회하는 메서드
     * @param cursor 조회할 페이지 번호
     * @param size 조회할 페이지 사이즈
     *
     * */
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPaginationResponse<MemorialResponse, Integer>>> getMemorialList(
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "20") int size)
            throws  InvalidPaginationRangeException
    {
        /* 게시글 리스트 조회 */

        /* 페이징 요청 검증 */
        validatePagination(cursor, size);

        String successMessage = messageSourceAccessor.getMessage("board.read.success", new Object[] {});
        CursorPaginationResponse<MemorialResponse, Integer> memorialResponses = memorialService.getMemorialList(cursor, size);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage,memorialResponses));
    }

    /**
     *
     * 기증자 추모관 상세 페이지 조회 메서드
     *
     * @param donateSeq 조회할 게시글 번호
     *
     * */
    @GetMapping("/{donateSeq}")
    public ResponseEntity<ApiResponse<MemorialDetailResponse>> getMemorialByDonateSeq(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq)
            throws  MemorialNotFoundException
    {
        /* 게시글 상세 조회 */

        String successMessage = messageSourceAccessor.getMessage("board.read.success", new Object[] {});
        MemorialDetailResponse memorialDetailResponse = memorialService.getMemorialByDonateSeq(donateSeq);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage, memorialDetailResponse));
    }

    /**
     *
     * 기증자 추모관 게시글 검색 조회 메서드
     *
     * @param startDate 시작 일
     * @param endDate 종료 일
     * @param keyWord 검색할 문자
     * @param cursor 페이지 번호
     * @param size 페이지 사이즈
     *
     * */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorPaginationResponse<MemorialResponse, Integer>>> getSearchMemorialList(
            @RequestParam(defaultValue = "1900-01-01") String startDate,
            @RequestParam(defaultValue = "2100-12-31") String endDate,
            @RequestParam(defaultValue = "") String keyWord,
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "20") int size)
            throws  InvalidPaginationRangeException,
                    MissingSearchDateParameterException,
                    InvalidSearchDateFormatException,
                    InvalidSearchDateRangeException
    {
        /* 게시글 검색 조건 조회 */

        /* 날짜 조건 검증 */
        validateSearchDates(startDate, endDate);

        /* 페이징 요청 검증 */
        validatePagination(cursor, size);

        String successMessage = messageSourceAccessor.getMessage("board.search.read.success", new Object[] {});
        CursorPaginationResponse<MemorialResponse, Integer> memorialResponses = memorialService.getSearchMemorialList(startDate, endDate, keyWord, cursor, size);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage, memorialResponses));
    }

    /**
     *
     * 기증자 추모관 게시글 상세 페이지의 이모지를 +1 해주는 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param emotion 증가시길 이모지
     *
     * */
    @PatchMapping("/{donateSeq}/{emotion}")
    public ResponseEntity<ApiResponse<String>> updateMemorialLikeCount(
            @PathVariable @Min(value = 1, message = DONATE_INVALID) Integer donateSeq,
            @PathVariable String emotion)
            throws  InvalidEmotionTypeException,
                    MemorialNotFoundException
    {
        /* 이모지 카운트 수 업데이트 */
        /* flower, love, see, miss, proud, hard, sad */

        String successMessage = messageSourceAccessor.getMessage("board.emotion.update.success", new Object[] {});
        memorialService.emotionCountUpdate(donateSeq, emotion);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage));
    }
}

