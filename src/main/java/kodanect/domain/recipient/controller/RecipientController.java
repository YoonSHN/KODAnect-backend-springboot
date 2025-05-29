package kodanect.domain.recipient.controller;

import kodanect.common.response.ApiResponse;
import kodanect.domain.recipient.dto.*;
import kodanect.domain.recipient.service.RecipientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
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

    // 게시물 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecipientListResponseDto>>> getRecipientList(
            RecipientSearchCondition searchCondition,
            @RequestParam(required = false) Integer lastId, // 첫 조회 시 null, 더보기 시 마지막 게시물 ID
            @RequestParam(defaultValue = "20") int size     // 한 번에 가져올 개수
    ) {
        List<RecipientListResponseDto> list = recipientService.selectRecipientList(searchCondition, lastId, size);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,"게시물 목록 조회 성공", list));
    }

    // 게시판 등록 페이지 요청 (단순 200 응답)
    @GetMapping("/new")
    public ResponseEntity<ApiResponse<Void>> writeForm() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "게시물 작성 페이지 접근 성공", null)); // 200 OK
    }

    // 게시판 등록
    @PostMapping
    public ResponseEntity<ApiResponse<RecipientDetailResponseDto>> write(@Valid @RequestBody RecipientRequestDto recipientRequestDto) {
        logger.info("게시물 등록 요청: title={}", recipientRequestDto.getLetterTitle());
        // 서비스에 RecipientRequestDto 자체를 전달
        RecipientDetailResponseDto createdRecipient = recipientService.insertRecipient(recipientRequestDto); // 서비스 시그니처 변경 필요
        // 성공 시 RecipientResponseDto 객체를 본문에 담아 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "게시물이 성공적으로 등록되었습니다.", createdRecipient)); // 201 Created
    }

    // 특정 게시판 조회
    @GetMapping("/{letterSeq}")
    public ResponseEntity<ApiResponse<RecipientDetailResponseDto>> view(@PathVariable("letterSeq") int letterSeq){
        logger.info("게시물 상세 조회 요청: letterSeq={}", letterSeq);
        RecipientDetailResponseDto recipientDto = recipientService.selectRecipient(letterSeq);
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
    public ResponseEntity<ApiResponse<RecipientDetailResponseDto>> edit(@PathVariable("letterSeq") int letterSeq,
                                                                        @Valid @RequestBody RecipientRequestDto recipientRequestDto) {
        logger.info("게시물 수정 요청: letterSeq={}, title={}", letterSeq, recipientRequestDto.getLetterTitle());
        // 서비스에 RecipientRequestDto 자체와 letterSeq, requestPasscode를 전달
        // 서비스 시그니처 변경 필요: updateRecipient(Integer letterSeq, String requestPasscode, RecipientRequestDto requestDto)
        RecipientDetailResponseDto updatedRecipient = recipientService.updateRecipient(
                letterSeq,
                recipientRequestDto.getLetterPasscode(), // 요청 비밀번호는 DTO에서 가져옴
                recipientRequestDto // DTO 자체를 서비스로 전달 (캡차 토큰 포함)
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

    // 특정 게시물의 "더보기" 댓글 조회 API
    @GetMapping("/{letterSeq}/comments")
    public ResponseEntity<ApiResponse<List<RecipientCommentResponseDto>>> getPaginatedCommentsForRecipient(
            @PathVariable("letterSeq") int letterSeq,
            @RequestParam(required = false) Integer lastCommentId, // 마지막으로 조회된 댓글의 ID
            @RequestParam(defaultValue = "3") int size) { // 한 번에 가져올 댓글 개수 (기본값 3개)
        logger.info("페이징된 댓글 조회 요청: letterSeq={}, lastCommentId={}, size={}", letterSeq, lastCommentId, size);
        List<RecipientCommentResponseDto> comments = recipientService.selectPaginatedCommentsForRecipient(letterSeq, lastCommentId, size);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "댓글 목록 조회 성공", comments));
    }
}
