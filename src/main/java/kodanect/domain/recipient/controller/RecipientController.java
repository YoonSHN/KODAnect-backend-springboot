package kodanect.domain.recipient.controller;

import kodanect.common.exception.config.SecureLogger;
import kodanect.common.response.ApiResponse;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.recipient.dto.*;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
import kodanect.domain.recipient.service.RecipientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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

    /**
     * 게시물 목록 조회 (일반 조회, 커서 기반 페이징 적용)
     * 파라미터: cursor, size
     * 응답: ApiResponse<CursorPaginationResponse<RecipientListResponseDto, Integer>>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPaginationResponse<RecipientListResponseDto, Integer>>> getRecipientList(
            @RequestParam(required = false) Integer cursor,     // 첫 조회 시 null, 더보기 시 마지막 게시물 ID
            @RequestParam(defaultValue = "20") int size         // 게시물 한 번에 가져올 개수 (기본값 20)
    ) {
        logger.info("게시물 목록 조회 요청이 수신되었습니다. cursor: {}, size: {}", cursor, size);
        CursorPaginationResponse<RecipientListResponseDto, Integer> responseData =
                recipientService.selectRecipientList(new RecipientSearchCondition(), cursor, size); // 빈 검색 조건 전달

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물 목록 조회 성공", responseData));
    }

    /** 게시물 검색 조회 (커서 기반 페이징 적용)

    **요청:** `GET /recipientLetters`
            **파라미터:** `type`, `keyWord`, `cursor`, `size`
            **응답:** `ApiResponse<CursorPaginationResponse<RecipientListResponseDto, Integer>>`
            */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorPaginationResponse<RecipientListResponseDto, Integer>>> searchRecipientLetters(
            RecipientSearchCondition searchCondition,
            @RequestParam(required = false) Integer cursor,      // 첫 조회 시 null, 더보기 시 마지막 게시물 ID
            @RequestParam(defaultValue = "20") int size         // 게시물 한 번에 가져올 개수 (기본값 20)**
    ) {
        logger.info("게시물 목록 조회 요청이 수신되었습니다. cursor: {}, size: {}", cursor, size);
        CursorPaginationResponse<RecipientListResponseDto, Integer> responseData =
                recipientService.selectRecipientList(searchCondition, cursor, size);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,"게시물 목록 조회 성공", responseData));
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

        recipientService.insertRecipient(recipientRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "게시물이 성공적으로 등록되었습니다.", null));
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

        try {
            // 서비스 계층에서 비밀번호 검증 수행.
            // requestDto를 inputData로 전달
            recipientService.verifyLetterPassword(letterSeq, letterPasscode);

            // 성공 시, data 필드에 true를 반환
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "비밀번호 확인", null));
        } catch (RecipientInvalidPasscodeException e) {
            logger.warn("게시물 비밀번호 확인 실패: letterSeq={}, error={}", letterSeq, e.getMessage());
            // 비밀번호 확인 실패 시, inputData를 반환하지 않음
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED, e.getMessage())); // data 필드 없음
        } catch (Exception e) {
            logger.error("게시물 비밀번호 확인 중 오류 발생: letterSeq={}, error={}", letterSeq, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "비밀번호 확인 중 오류가 발생했습니다."));
        }
    }

    /** ## 게시물 수정

    **요청:** `PATCH /recipientLetters/{letterSeq}`
            **컨텐츠 타입:** `multipart/form-data`
            **파라미터:** `letterSeq` (Path Variable), `@ModelAttribute @Valid RecipientRequestDto`
            **응답:** `ApiResponse<RecipientDetailResponseDto>`
            */
    @PatchMapping(value = "/{letterSeq}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Object>> edit(@PathVariable("letterSeq") Integer letterSeq,
                                               @ModelAttribute @Valid RecipientRequestDto recipientRequestDto,
                                               BindingResult bindingResult // @Valid 에 대한 에러를 처리하기 위함
    ) {
        logger.info("게시물 수정 요청: letterSeq={}, title={}", letterSeq, recipientRequestDto.getLetterTitle());

        // @Valid 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            // 유효성 검사 실패 시에도 사용자가 입력한 데이터를 반환
            return ResponseEntity.badRequest().body(ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMessage, recipientRequestDto));
        }

        try {
            recipientService.updateRecipient(
                    letterSeq,
                    recipientRequestDto
            );
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,"게시물이 성공적으로 수정되었습니다.", null));

        } catch (Exception e) {
            // 기타 예외 처리 (예: RecipientNotFoundException 등)
            logger.error("게시물 수정 중 오류 발생: letterSeq={}, error={}", letterSeq, e.getMessage(), e);
            // 일반적인 에러 응답. 이 경우 사용자가 입력한 데이터를 다시 반환할 필요는 없다고 판단
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR,"게시물 수정 중 오류가 발생했습니다."));
        }
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
