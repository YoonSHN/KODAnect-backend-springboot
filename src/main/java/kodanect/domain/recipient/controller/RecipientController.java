package kodanect.domain.recipient.controller;

import kodanect.common.exception.config.SecureLogger;
import kodanect.common.response.ApiResponse;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.recipient.dto.*;
import kodanect.domain.recipient.service.RecipientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/recipientLetters")
public class RecipientController {

    private static final SecureLogger logger = SecureLogger.getLogger(RecipientController.class);

    private final RecipientService recipientService;

    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    /** ## 게시물 목록 조회 (커서 기반 페이징 적용)

    **요청:** `GET /recipientLetters`
            **파라미터:** `searchKeyword`, `searchType`, `cursor`, `size`
            **응답:** `ApiResponse<CursorPaginationResponse<RecipientListResponseDto, Integer>>`
            */
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPaginationResponse<RecipientListResponseDto, Integer>>> getRecipientList(
            RecipientSearchCondition searchCondition,
            @RequestParam(required = false) Integer cursor,      // 첫 조회 시 null, 더보기 시 마지막 게시물 ID
            @RequestParam(defaultValue = "20") int size         // 게시물 한 번에 가져올 개수 (기본값 20)**
    ) {
        logger.info("게시물 목록 조회 요청이 수신되었습니다. cursor: {}, size: {}", cursor, size);
        CursorPaginationResponse<RecipientListResponseDto, Integer> responseData =
                recipientService.selectRecipientList(searchCondition, cursor, size);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,"게시물 목록 조회 성공", responseData));
    }

    /** ## 게시판 등록 페이지 요청

    **요청:** `GET /recipientLetters/new`
            **응답:** `ApiResponse<Void>`
            */
    @GetMapping("/new")
    public ResponseEntity<ApiResponse<Void>> writeForm() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물 작성 페이지 접근 성공", null));
    }

    /** ## 게시판 등록

    **요청:** `POST /recipientLetters`
            **컨텐츠 타입:** `multipart/form-data`
            **파라미터:** `@ModelAttribute @Valid RecipientRequestDto`
            **응답:** `ApiResponse<RecipientDetailResponseDto>`
            */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RecipientDetailResponseDto>> write(@ModelAttribute @Valid RecipientRequestDto recipientRequestDto) {
        logger.info("게시물 등록 요청: title={}", recipientRequestDto.getLetterTitle());
        RecipientDetailResponseDto createdRecipient = recipientService.insertRecipient(recipientRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "게시물이 성공적으로 등록되었습니다.", createdRecipient));
    }

    /** ## 특정 게시판 조회

    **요청:** `GET /recipientLetters/{letterSeq}`
            **파라미터:** `letterSeq` (Path Variable)
            **응답:** `ApiResponse<RecipientDetailResponseDto>`
            */
    @GetMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<RecipientDetailResponseDto>> view(@PathVariable("letterSeq") Integer letterSeq){
        logger.info("게시물 상세 조회 요청: letterSeq={}", letterSeq);
        RecipientDetailResponseDto recipientDto = recipientService.selectRecipient(letterSeq);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물 조회 성공", recipientDto));
    }

    /** ## 게시물 수정을 위한 비밀번호 인증

    **요청:** `POST /recipientLetters/{letterSeq}/verifyPwd`
            **파라미터:** `letterSeq` (Path Variable), `requestBody` (비밀번호)
            **응답:** `ApiResponse<Boolean>`
            */
    @PostMapping("/{letterSeq}/verifyPwd")
    public ResponseEntity<ApiResponse<Boolean>> verifyPassword(@PathVariable("letterSeq") Integer letterSeq,
                                                               @RequestBody Map<String, String> requestBody) {
        String letterPasscode = requestBody.get("letterPasscode");
        logger.info("게시물 비밀번호 확인 요청: letterSeq={}", letterSeq);

        recipientService.verifyLetterPassword(letterSeq, letterPasscode);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "비밀번호 확인"));
    }

    /** ## 게시물 수정

    **요청:** `PATCH /recipientLetters/{letterSeq}`
            **컨텐츠 타입:** `multipart/form-data`
            **파라미터:** `letterSeq` (Path Variable), `@ModelAttribute @Valid RecipientRequestDto`
            **응답:** `ApiResponse<RecipientDetailResponseDto>`
            */
    @PatchMapping(value = "/{letterSeq}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RecipientDetailResponseDto>> edit(@PathVariable("letterSeq") Integer letterSeq,
                                                                        @ModelAttribute @Valid RecipientRequestDto recipientRequestDto) {
        logger.info("게시물 수정 요청: letterSeq={}, title={}", letterSeq, recipientRequestDto.getLetterTitle());
        RecipientDetailResponseDto updatedRecipient = recipientService.updateRecipient(
                letterSeq,
                recipientRequestDto
        );
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물이 성공적으로 수정되었습니다.", updatedRecipient));
    }

    /** ## 게시물 삭제

    **요청:** `DELETE /recipientLetters/{letterSeq}`
            **파라미터:** `letterSeq` (Path Variable), `@RequestBody RecipientDeleteRequestDto`
            **응답:** `ApiResponse<Void>`
            */
    @DeleteMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("letterSeq") Integer letterSeq,
                                                    @Valid @RequestBody RecipientDeleteRequestDto requestDto){
        logger.info("게시물 삭제 요청: letterSeq={}", letterSeq);
        recipientService.deleteRecipient(
                letterSeq,
                requestDto.getLetterPasscode()
        );
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물이 성공적으로 삭제되었습니다."));
    }
}
