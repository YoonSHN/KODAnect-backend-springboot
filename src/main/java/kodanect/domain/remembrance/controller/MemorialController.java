package kodanect.domain.remembrance.controller;

import kodanect.common.response.ApiResponse;
import kodanect.domain.remembrance.dto.MemorialDetailDto;
import kodanect.domain.remembrance.dto.MemorialListDto;
import kodanect.domain.remembrance.service.MemorialService;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/remembrance")
public class MemorialController {

    /* 상수 */
    private final int SUCCESS_RESPONSE_CODE = 200;

    private final MemorialService memorialService;
    private final MessageSource messageSource;

    public MemorialController(MemorialService memorialService, MessageSource messageSource){
        this.memorialService = memorialService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MemorialListDto>>> getMemorialList(
            @RequestParam(defaultValue = "1") String page,
            @RequestParam(defaultValue = "20") String size) throws Exception{
        /* 게시글 리스트 조회 */

        String successMessage = messageSource.getMessage("board.read.success", null, Locale.getDefault());
        Page<MemorialListDto> memorial = memorialService.getMemorialList(page, size);
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_RESPONSE_CODE, successMessage,memorial));
    }

    @GetMapping("/{donateSeq}")
    public ResponseEntity<ApiResponse<MemorialDetailDto>> getMemorialByDonateSeq(
            @PathVariable Integer donateSeq) throws Exception{
        /* 게시글 상세 조회 */

        String successMessage = messageSource.getMessage("board.read.success", null, Locale.getDefault());
        MemorialDetailDto memorial = memorialService.getMemorialByDonateSeq(donateSeq);
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_RESPONSE_CODE, successMessage, memorial));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<MemorialListDto>>> getSearchMemorialList(
            @RequestParam(defaultValue = "1900-01-01") String startDate,
            @RequestParam(defaultValue = "2100-12-31") String endDate,
            @RequestParam(defaultValue = "") String searchWord,
            @RequestParam(defaultValue = "1") String page,
            @RequestParam(defaultValue = "20") String size) throws Exception{
        /* 게시글 검색 조건 조회 */

        String successMessage = messageSource.getMessage("board.search.read.success", null, Locale.getDefault());
        Page<MemorialListDto> memorial = memorialService.getSearchMemorialList(page, size, startDate, endDate, searchWord);
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_RESPONSE_CODE, successMessage, memorial));
    }

    @PatchMapping("/{donateSeq}/{emotion}")
    public ResponseEntity<ApiResponse<String>> updateMemorialLikeCount(
            @PathVariable Integer donateSeq,
            @PathVariable String emotion) throws Exception{
        /* 이모지 카운트 수 업데이트 */
        /* flower, love, see, miss, proud, hard, sad */

        String successMessage = messageSource.getMessage("board.emotion.update.success", null, Locale.getDefault());
        memorialService.emotionCountUpdate(donateSeq, emotion);
        return ResponseEntity.ok(ApiResponse.success(SUCCESS_RESPONSE_CODE, successMessage));
    }
}

