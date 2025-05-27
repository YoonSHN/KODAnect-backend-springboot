package kodanect.domain.recipient.controller;

import kodanect.common.exception.RecipientInvalidDataException;
import kodanect.common.response.ApiResponse;
import kodanect.common.response.PageApiResponse;
import kodanect.domain.recipient.dto.RecipientResponseDto;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.service.RecipientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/recipientLetters")
public class RecipientController {

    private static final Logger logger = LoggerFactory.getLogger(RecipientController.class);
    private static final int DEFAULT_PAGE_SIZE = 20; // 상수로 정의

    private final RecipientService recipientService;

    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    // 게시판 조회 (페이지, 검색 포함)
    @GetMapping
    public ResponseEntity<PageApiResponse<RecipientResponseDto>> search(@ModelAttribute RecipientEntity searchVO,
                                                                        @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "writeTime", direction = org.springframework.data.domain.Sort.Direction.DESC)
                                                                        Pageable pageable){
        try {
            Page<RecipientResponseDto> recipientPage = recipientService.selectRecipientListPaged(searchVO,pageable);

            PageApiResponse<RecipientResponseDto> response = PageApiResponse.success(
                    recipientPage, "수혜자 편지 목록 가져오기 성공" // 요청하신 메시지
            );
            return ResponseEntity.ok(response);  //  200 OK
        }
        catch (Exception e) {
            logger.error("게시물 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PageApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "게시물 목록 조회 중 오류가 발생했습니다."));  // 500 Internal Server Error
        }
    }

    // 게시판 등록 페이지 요청 (단순 200 응답)
    @GetMapping("/new")
    public ResponseEntity<ApiResponse<Void>> writeForm() {
        return ResponseEntity.ok(ApiResponse.success(200, "게시물 작성 페이지 접근 성공", null)); // 200 OK
    }

    // 게시판 등록
    @PostMapping
    public ResponseEntity<ApiResponse<RecipientResponseDto>> write(@Valid @RequestBody RecipientEntity recipientEntityRequest) {
        try {
            // RecipientEntity 객체에서 captchaToken을 추출하여 서비스로 전달
            RecipientResponseDto createdRecipient = recipientService.insertRecipient(
                    recipientEntityRequest,recipientEntityRequest.getCaptchaToken() // RecipientEntity에서 captchaToken 추출
            );
            // 성공 시 RecipientResponseDto 객체를 본문에 담아 반환
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(HttpStatus.CREATED.value(), "게시물이 성공적으로 등록되었습니다.", createdRecipient)); // 201 Created
        }
        catch (RecipientInvalidDataException e) {
            logger.warn("게시물 등록 실패: 유효하지 않은 데이터 - 제목: {}, 메시지: {}", recipientEntityRequest.getLetterTitle(), e.getMessage());
            // 실패 시 ApiResponse 객체를 본문에 담아 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage())); // 400 Bad Request
        } catch (Exception e) {
            logger.error("게시물 등록 중 오류 발생 - 제목: {}", recipientEntityRequest.getLetterTitle(), e);
            // 실패 시 ApiResponse 객체를 본문에 담아 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "게시물 등록 중 알 수 없는 오류가 발생했습니다.")); // 500 Internal Server Error
        }
    }

    // 특정 게시판 조회
    @GetMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<RecipientResponseDto>> view(@PathVariable("letterSeq") int letterSeq){
        try {
            RecipientResponseDto recipientDto = recipientService.selectRecipient(letterSeq);
            return ResponseEntity.ok(ApiResponse.success(200, "게시물 조회 성공", recipientDto)); // 200 OK
        } catch (NoSuchElementException e) { // 게시물이 존재하지 않거나 삭제된 경우
            logger.warn("게시물을 찾을 수 없습니다: {}", letterSeq);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "요청하신 게시물을 찾을 수 없습니다.")); // 404 Not Found
        } catch (Exception e) {
            logger.error("게시물 조회 중 오류 발생 - letterSeq: {}", letterSeq, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "게시물 조회 중 오류가 발생했습니다.")); // 500 Internal Server Error
        }
    }

    // 게시물 수정을 위한 비밀번호 인증
    @PostMapping("/{letterSeq}/verifyPwd")
    public ResponseEntity<ApiResponse<Void>> verifyPassword(@PathVariable("letterSeq") int letterSeq,
                                                            @RequestParam("letterPasscode") String letterPasscode) {
        try {
            boolean isVerified = recipientService.verifyLetterPassword(letterSeq, letterPasscode);
            if (isVerified) {
                return ResponseEntity.ok(ApiResponse.success(200, "비밀번호 인증 성공")); // 200 OK (인증 성공)
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "비밀번호가 일치하지 않습니다.")); // 401 Unauthorized (비밀번호 불일치)
            }
        }
        catch (NoSuchElementException e) { // 게시물이 존재하지 않거나 삭제된 경우
            logger.warn("비밀번호 인증 실패: 게시물을 찾을 수 없습니다. letterSeq: {}", letterSeq);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "비밀번호 인증 실패: 게시물을 찾을 수 없습니다.")); // 404 Not Found
        }
        catch (Exception e) {
            logger.error("비밀번호 인증 중 오류 발생 - letterSeq: {}", letterSeq, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "비밀번호 인증 중 오류가 발생했습니다.")); // 500 Internal Server Error
        }
    }

    // 게시물 수정
    @PatchMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<RecipientResponseDto>> edit(@PathVariable("letterSeq") int letterSeq,
                                                                  @Valid @RequestBody RecipientEntity recipientEntityRequest) {
        try {
            RecipientResponseDto updatedRecipientVO = recipientService.updateRecipient(recipientEntityRequest, letterSeq, recipientEntityRequest.getLetterPasscode());
            return ResponseEntity.ok(ApiResponse.success(200, "게시물이 성공적으로 수정되었습니다.", updatedRecipientVO)); // 200 OK
        }
        catch (NoSuchElementException e) { // 게시물을 찾을 수 없는 경우
            logger.warn("게시물 수정 실패: 게시물을 찾을 수 없습니다. letterSeq: {}", letterSeq);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "게시물 수정 실패: 게시물을 찾을 수 없습니다.")); // 404 Not Found
        }
        catch (IllegalArgumentException e) { // 비밀번호 불일치 등 유효하지 않은 인자
            logger.warn("게시물 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage())); // 400 Bad Request
        }
        catch (Exception e) {
            logger.error("게시물 수정 중 오류 발생 - letterSeq: {}", letterSeq, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "게시물 수정 중 알 수 없는 오류가 발생했습니다.")); // 500 Internal Server Error
        }
    }

    // 게시물 삭제
    @DeleteMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("letterSeq") int letterSeq,
                                                    @RequestBody RecipientEntity recipientEntityRequest){
        try {
            recipientService.deleteRecipient(letterSeq, recipientEntityRequest.getLetterPasscode());
            return ResponseEntity.ok(ApiResponse.success(200, "게시물이 성공적으로 삭제되었습니다.")); // 200 OK
        }
        catch (NoSuchElementException e) { // 게시물을 찾을 수 없는 경우
            logger.warn("게시물 삭제 실패: 게시물을 찾을 수 없습니다. letterSeq: {}", letterSeq);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(HttpStatus.NOT_FOUND.value(), "게시물 삭제 실패: 게시물을 찾을 수 없습니다.")); // 404 Not Found
        }
        catch (IllegalArgumentException e) { // 비밀번호 불일치
            logger.warn("게시물 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), "비밀번호가 일치하지 않습니다.")); // 401 Unauthorized
        }
        catch (Exception e) {
            logger.error("게시물 삭제 중 오류 발생 - letterSeq: {}", letterSeq, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "게시물 삭제 중 알 수 없는 오류가 발생했습니다.")); // 500 Internal Server Error
        }
    }
}
