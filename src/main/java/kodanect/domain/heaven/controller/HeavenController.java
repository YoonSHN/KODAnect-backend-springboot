package kodanect.domain.heaven.controller;

import kodanect.common.response.ApiResponse;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.heaven.dto.request.HeavenCreateRequest;
import kodanect.domain.heaven.dto.request.HeavenUpdateRequest;
import kodanect.domain.heaven.dto.request.HeavenVerifyRequest;
import kodanect.domain.heaven.dto.response.HeavenDetailResponse;
import kodanect.domain.heaven.dto.response.HeavenResponse;
import kodanect.domain.heaven.dto.response.MemorialHeavenResponse;
import kodanect.domain.heaven.service.HeavenService;
import kodanect.domain.remembrance.dto.common.BlankGroup;
import kodanect.domain.remembrance.dto.common.PatternGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/heavenLetters")
@Validated({BlankGroup.class, PatternGroup.class})
@RequiredArgsConstructor
public class HeavenController {

    private final HeavenService heavenService;
    private final MessageSourceAccessor messageSourceAccessor;

    /* 게시물 전체 조회 (페이징) */
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPaginationResponse<HeavenResponse, Integer>>> getHeavenList(
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        CursorPaginationResponse<HeavenResponse, Integer> heavenList = heavenService.getHeavenList(cursor, size);

        String message = messageSourceAccessor.getMessage("board.list.read.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, heavenList));
    }

    /* 검색을 통한 게시물 전체 조회 (페이징) */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorPaginationResponse<HeavenResponse, Integer>>> searchHeavenList(
            @RequestParam String type,
            @RequestParam String keyWord,
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        CursorPaginationResponse<HeavenResponse, Integer> heavenList = heavenService.getHeavenListSearchResult(type, keyWord, cursor, size);

        String message = messageSourceAccessor.getMessage("board.search.read.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, heavenList));
    }

    /* 게시물 상세 조회 */
    @GetMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<HeavenDetailResponse>> getHeavenDetail(
            @PathVariable Integer letterSeq
    ) {
        HeavenDetailResponse heavenDetailResponse = heavenService.getHeavenDetail(letterSeq);

        String message = messageSourceAccessor.getMessage("board.read.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, heavenDetailResponse));
    }

    /* 기증자 추모관 상세 조회 시 하늘나라 편지 전체 조회 */
    @GetMapping("/{donateSeq}/remembrance")
    public ResponseEntity<ApiResponse<CursorPaginationResponse<MemorialHeavenResponse, Integer>>> getMemorialHeavenList(
            @PathVariable Integer donateSeq,
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "3") int size
    ) {
        CursorPaginationResponse<MemorialHeavenResponse, Integer> memorialHeavenList = heavenService.getMemorialHeavenList(donateSeq, cursor, size);

        String message = messageSourceAccessor.getMessage("board.list.read.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, memorialHeavenList));
    }

    /* 게시물 등록 */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> createHeaven(
            @ModelAttribute @Valid HeavenCreateRequest heavenCreateRequest
    ) {
        heavenService.createHeaven(heavenCreateRequest);

        String message = messageSourceAccessor.getMessage("board.create.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, message));
    }

    /* 게시물 수정 인증 */
    @PostMapping("/{letterSeq}/verifyPwd")
    public ResponseEntity<ApiResponse<Void>> verifyHeavenPasscode(
            @PathVariable Integer letterSeq,
            @RequestBody @Valid HeavenVerifyRequest heavenVerifyRequest
    ) {
        heavenService.verifyHeavenPasscode(letterSeq, heavenVerifyRequest.getLetterPasscode());

        String message = messageSourceAccessor.getMessage("board.verify.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message));
    }

    /* 게시물 수정 */
    @PatchMapping(value = "/{letterSeq}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateHeaven(
        @PathVariable Integer letterSeq,
        @ModelAttribute @Valid HeavenUpdateRequest heavenUpdateRequest
    ) {
        heavenService.updateHeaven(letterSeq, heavenUpdateRequest);

        String message = messageSourceAccessor.getMessage("board.update.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message));
    }

    /* 게시물 삭제 */
    @DeleteMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<Void>> deleteHeaven(
        @PathVariable Integer letterSeq,
        @RequestBody @Valid HeavenVerifyRequest heavenVerifyRequest
    ) {
        heavenService.deleteHeaven(letterSeq, heavenVerifyRequest.getLetterPasscode());

        String message = messageSourceAccessor.getMessage("board.delete.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message));
    }
}
