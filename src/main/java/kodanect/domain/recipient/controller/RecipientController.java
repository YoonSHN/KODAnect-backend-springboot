package kodanect.domain.recipient.controller;

import kodanect.common.response.ApiResponse;
import kodanect.domain.recipient.dto.RecipientDeleteRequestDto;
import kodanect.domain.recipient.dto.RecipientRequestDto;
import kodanect.domain.recipient.dto.RecipientResponseDto;
import kodanect.domain.recipient.dto.RecipientSearchCondition;
import kodanect.domain.recipient.service.RecipientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

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
    public ResponseEntity<ApiResponse<Page<RecipientResponseDto>>> search(RecipientSearchCondition searchCondition, Pageable pageable){
        logger.info("게시물 목록 조회 요청: 검색 조건={}, 페이징={}", searchCondition, pageable);
        Page<RecipientResponseDto> recipientPage = recipientService.selectRecipientListPaged(searchCondition,pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "수혜자 편지 목록 가져오기 성공", recipientPage));
    }

    // 게시판 등록 페이지 요청 (단순 200 응답)
    @GetMapping("/new")
    public ResponseEntity<ApiResponse<Void>> writeForm() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물 작성 페이지 접근 성공", null)); // 200 OK
    }

    // 게시판 등록
    @PostMapping
    public ResponseEntity<ApiResponse<RecipientResponseDto>> write(@Valid @RequestBody RecipientRequestDto recipientRequestDto) {
        logger.info("게시물 등록 요청: title={}", recipientRequestDto.getLetterTitle());
        // RecipientEntity 객체에서 captchaToken을 추출하여 서비스로 전달
        RecipientResponseDto createdRecipient = recipientService.insertRecipient(
                recipientRequestDto.toEntity(), // DTO를 Entity로 변환
                recipientRequestDto.getCaptchaToken() // RecipientRequestDto에서 captchaToken 추출
        );
        // 성공 시 RecipientResponseDto 객체를 본문에 담아 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "게시물이 성공적으로 등록되었습니다.", createdRecipient)); // 201 Created
    }

    // 특정 게시판 조회
    @GetMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<RecipientResponseDto>> view(@PathVariable("letterSeq") int letterSeq){
        logger.info("게시물 상세 조회 요청: letterSeq={}", letterSeq);
        RecipientResponseDto recipientDto = recipientService.selectRecipient(letterSeq);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물 조회 성공", recipientDto)); // 200 OK
    }

    // 게시물 수정을 위한 비밀번호 인증
    @PostMapping("/{letterSeq}/verifyPwd")
    public ResponseEntity<ApiResponse<Boolean>> verifyPassword(@PathVariable("letterSeq") int letterSeq,
                                                            @RequestBody Map<String, String> requestBody) {
        String letterPasscode = requestBody.get("letterPasscode");
        logger.info("게시물 비밀번호 확인 요청: letterSeq={}", letterSeq);

        boolean isVerified = recipientService.verifyLetterPassword(letterSeq, letterPasscode);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "비밀번호 확인 결과", isVerified));
    }

    // 게시물 수정
    @PatchMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<RecipientResponseDto>> edit(@PathVariable("letterSeq") int letterSeq,
                                                                  @Valid @RequestBody RecipientRequestDto recipientRequestDto) {
        logger.info("게시물 수정 요청: letterSeq={}, title={}", letterSeq, recipientRequestDto.getLetterTitle());
        // 비밀번호와 캡차 토큰을 서비스로 전달
        RecipientResponseDto updatedRecipient = recipientService.updateRecipient(
                recipientRequestDto.toEntity(), // 업데이트할 내용이 담긴 RecipientEntity (DTO에서 변환)
                letterSeq,                      // PathVariable에서 받은 게시물 시퀀스
                recipientRequestDto.getLetterPasscode(), // 요청 비밀번호
                recipientRequestDto.getCaptchaToken()    // 캡차 토큰 전달
        );
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물이 성공적으로 수정되었습니다.", updatedRecipient));
    }

    // 게시물 삭제
    @DeleteMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("letterSeq") Integer letterSeq,
                                                    @Valid @RequestBody RecipientDeleteRequestDto requestDto){
        logger.info("게시물 삭제 요청: letterSeq={}", letterSeq);
        // requestDto에서 비밀번호와 캡차 토큰을 추출하여 서비스로 전달
        recipientService.deleteRecipient(
                letterSeq,
                requestDto.getLetterPasscode(),
                requestDto.getCaptchaToken()
        );
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물이 성공적으로 삭제되었습니다."));
    }
}
