package kodanect.domain.remembrance.controller;

import kodanect.common.response.ApiResponse;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.remembrance.dto.HeavenMemorialResponse;
import kodanect.domain.remembrance.dto.MemorialDetailResponse;
import kodanect.domain.remembrance.dto.MemorialResponse;
import kodanect.domain.remembrance.dto.common.MemorialNextCursor;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.service.MemorialService;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.Page;
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
     * @param nextCursor 조회할 페이지 번호
     * @param size 조회할 페이지 사이즈
     *
     * */
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPaginationResponse<MemorialResponse, MemorialNextCursor>>> getMemorialList(
            @ModelAttribute MemorialNextCursor nextCursor,
            @RequestParam(defaultValue = "20") int size)
            throws InvalidPaginationException
    {
        /* 게시글 리스트 조회 */

        /* 페이징 요청 검증 */
        validatePagination(nextCursor, size);

        String successMessage = messageSourceAccessor.getMessage("board.read.success", new Object[] {});
        CursorPaginationResponse<MemorialResponse, MemorialNextCursor> memorialResponses = memorialService.getMemorialList(nextCursor, size);
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
            throws  MemorialNotFoundException,
                    InvalidContentsException
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
     * @param nextCursor 페이지 번호
     * @param size 페이지 사이즈
     *
     * */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorPaginationResponse<MemorialResponse, MemorialNextCursor>>> getSearchMemorialList(
            @RequestParam(defaultValue = "1900-01-01") String startDate,
            @RequestParam(defaultValue = "2100-12-31") String endDate,
            @RequestParam(defaultValue = "") String keyWord,
            @RequestParam(defaultValue = "20") int size,
            @ModelAttribute MemorialNextCursor nextCursor)
            throws  InvalidPaginationException,
                    MissingSearchDateParameterException,
                    InvalidSearchDateFormatException,
                    InvalidSearchDateRangeException
    {
        /* 게시글 검색 조건 조회 */

        /* 날짜 조건 검증 */
        validateSearchDates(startDate, endDate);

        /* 페이징 요청 검증 */
        validatePagination(nextCursor, size);

        String successMessage = messageSourceAccessor.getMessage("board.search.read.success", new Object[] {});
        CursorPaginationResponse<MemorialResponse, MemorialNextCursor> memorialResponses = memorialService.getSearchMemorialList(startDate, endDate, keyWord, nextCursor, size);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, successMessage, memorialResponses));
    }

    /**
     * 하늘나라 편지 팝업 조회 ->
     * 기증자 추모관 게시글 검색 조회 메서드
     *
     * @param startDate 시작 일
     * @param endDate 종료 일
     * @param keyWord 검색할 문자
     * @param page 페이지 번호
     * @param size 페이지 사이즈
     *
     * */
    @GetMapping("/heaven")
    public ResponseEntity<ApiResponse<Page<HeavenMemorialResponse>>> getMemorialByHeaven(
            @RequestParam(defaultValue = "1900-01-01") String startDate,
            @RequestParam(defaultValue = "2100-12-31") String endDate,
            @RequestParam(defaultValue = "") String keyWord,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") int size)
            throws  InvalidPaginationException,
                    MissingSearchDateParameterException,
                    InvalidSearchDateFormatException,
                    InvalidSearchDateRangeException
    {
        /* 하늘나라 편지 팝업 추모자 검색 조회 */

        /* 날짜 조건 검증 */
        validateSearchDates(startDate, endDate);

        /* 페이징 요청 검증 */
        validatePagination(page, size);

        String successMessage = messageSourceAccessor.getMessage("board.search.read.success", new Object[] {});
        Page<HeavenMemorialResponse> memorialResponses = memorialService.getSearchHeavenMemorialList(startDate, endDate, keyWord, page, size);
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

