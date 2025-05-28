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
    public ResponseEntity<ApiResponse<Page<RecipientResponseDto>>> search(@ModelAttribute RecipientEntity searchVO,
                                                                        @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "writeTime", direction = org.springframework.data.domain.Sort.Direction.DESC)
                                                                        Pageable pageable){

        Page<RecipientResponseDto> recipientPage = recipientService.selectRecipientListPaged(searchVO,pageable);
        // ApiResponse에 Page<RecipientResponseDto>를 직접 담아서 반환
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "수혜자 편지 목록 가져오기 성공", recipientPage));
    }

    // 게시판 등록 페이지 요청 (단순 200 응답)
    @GetMapping("/new")
    public ResponseEntity<ApiResponse<Void>> writeForm() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물 작성 페이지 접근 성공", null)); // 200 OK
    }

    // 게시판 등록
    @PostMapping
    public ResponseEntity<ApiResponse<RecipientResponseDto>> write(@Valid @RequestBody RecipientEntity recipientEntityRequest) {
        // RecipientEntity 객체에서 captchaToken을 추출하여 서비스로 전달
        RecipientResponseDto createdRecipient = recipientService.insertRecipient(
                recipientEntityRequest,recipientEntityRequest.getCaptchaToken() // RecipientEntity에서 captchaToken 추출
        );
        // 성공 시 RecipientResponseDto 객체를 본문에 담아 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "게시물이 성공적으로 등록되었습니다.", createdRecipient)); // 201 Created
    }

    // 특정 게시판 조회
    @GetMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<RecipientResponseDto>> view(@PathVariable("letterSeq") int letterSeq){
        RecipientResponseDto recipientDto = recipientService.selectRecipient(letterSeq);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물 조회 성공", recipientDto)); // 200 OK
    }

    // 게시물 수정을 위한 비밀번호 인증
    @PostMapping("/{letterSeq}/verifyPwd")
    public ResponseEntity<ApiResponse<Void>> verifyPassword(@PathVariable("letterSeq") int letterSeq,
                                                            @RequestParam("letterPasscode") String letterPasscode) {
        // 서비스 계층에서 isVerified가 false일 경우 InvalidPasscodeException을 던지도록 로직 변경
        // 게시물을 찾을 수 없으면 RecipientNotFoundException을 던지도록 로직 변경
        recipientService.verifyLetterPassword(letterSeq, letterPasscode); // 성공 시 true 반환, 실패 시 예외 던짐
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "비밀번호 인증 성공"));
    }

    // 게시물 수정
    @PatchMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<RecipientResponseDto>> edit(@PathVariable("letterSeq") int letterSeq,
                                                                  @Valid @RequestBody RecipientEntity recipientEntityRequest) {
        RecipientResponseDto updatedRecipientVO = recipientService.updateRecipient(recipientEntityRequest, letterSeq, recipientEntityRequest.getLetterPasscode());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물이 성공적으로 수정되었습니다.", updatedRecipientVO)); // 200 OK
    }

    // 게시물 삭제
    @DeleteMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("letterSeq") int letterSeq,
                                                    @RequestBody RecipientEntity recipientEntityRequest){
        recipientService.deleteRecipient(letterSeq, recipientEntityRequest.getLetterPasscode());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물이 성공적으로 삭제되었습니다.")); // 200 OK
    }
}
