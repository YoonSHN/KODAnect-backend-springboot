package kodanect.domain.recipient.controller;

import kodanect.domain.recipient.dto.RecipientResponseDto;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.service.RecipientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private final RecipientService recipientService;

    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    // 게시판 조회 (페이지, 검색 포함)
    @GetMapping
    public ResponseEntity<Page<RecipientResponseDto>> search(@ModelAttribute RecipientEntity searchVO,
                                                             @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "writeTime", direction = org.springframework.data.domain.Sort.Direction.DESC)
                                                    Pageable pageable){
        try {
            Page<RecipientResponseDto> recipientPage = recipientService.selectRecipientListPaged(searchVO,pageable);
            return ResponseEntity.ok(recipientPage);  //  200 OK
        }
        catch (Exception e) {
            logger.error("게시물 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // 500 Internal Server Error
        }
    }

    // 게시판 등록 페이지 요청 (단순 200 응답)
    @GetMapping("/new")
    public ResponseEntity<Void> writeForm() {
        return ResponseEntity.ok().build(); // 200 OK


    // 게시판 등록
    @PostMapping //
    public ResponseEntity<RecipientResponseDto> write(@Valid @RequestBody RecipientEntity recipientEntityRequest
                                         /*, // 캡챠 인증 적용시 주석 해제
                                         @RequestParam(value = "captchaResponse", required = false) String captchaResponse  */) {
        try {
            RecipientResponseDto createdRecipient = recipientService.insertRecipient(recipientEntityRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRecipient); // 201 Created
        }
        catch (Exception e) {
            logger.error("게시물 등록 중 오류 발생 - 제목: {}", recipientEntityRequest.getLetterTitle(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        }
    }

    // 특정 게시판 조회
    @GetMapping("/{letterSeq}")
    public ResponseEntity<RecipientResponseDto> view(@PathVariable("letterSeq") int letterSeq){
        try {
            RecipientResponseDto recipientDto = recipientService.selectRecipient(letterSeq);
            return ResponseEntity.ok(recipientDto);  // 200 OK
        }
        catch (NoSuchElementException e) { // 게시물이 존재하지 않거나 삭제된 경우
            logger.warn("게시물을 찾을 수 없습니다: {}", letterSeq);
            return ResponseEntity.notFound().build();   // 404 Not Found
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // 500 Internal Server Error
        }
    }

    // 게시물 수정을 위한 비밀번호 인증
    @PostMapping("/{letterSeq}/verifyPwd")
    public ResponseEntity<Void> verifyPassword(@PathVariable("letterSeq") int letterSeq,
                                               @RequestParam("letterPasscode") String letterPasscode) {
        try {
            boolean isVerified = recipientService.verifyLetterPassword(letterSeq, letterPasscode);
            if (isVerified) {
                return ResponseEntity.ok().build(); // 200 OK (인증 성공)
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized (비밀번호 불일치)
            }
        }
        catch (NoSuchElementException e) { // 게시물이 존재하지 않거나 삭제된 경우
            logger.warn("비밀번호 인증 실패: 게시물을 찾을 수 없습니다. letterSeq: {}", letterSeq);
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
        catch (Exception e) {
            logger.error("비밀번호 인증 중 오류 발생 - letterSeq: {}", letterSeq, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }

    // 게시물 수정
    @PatchMapping("/{letterSeq}")
    public ResponseEntity<RecipientResponseDto> edit(@PathVariable("letterSeq") int letterSeq,
                                                     @Valid @RequestBody RecipientEntity recipientEntityRequest) {
        try {
            RecipientResponseDto updatedRecipientVO = recipientService.updateRecipient(recipientEntityRequest, letterSeq, recipientEntityRequest.getLetterPasscode());

            return ResponseEntity.ok(updatedRecipientVO);   // 200 OK
        }
        catch (NoSuchElementException e) { // 게시물을 찾을 수 없는 경우
            logger.warn("게시물 수정 실패: 게시물을 찾을 수 없습니다. letterSeq: {}", letterSeq);
            return ResponseEntity.notFound().build();   // 404 Not Found
        }
        catch (IllegalArgumentException e) { // 비밀번호 불일치 등 유효하지 않은 인자
            logger.warn("게시물 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 Bad Request 또는 401 Unauthorized
        }
        catch (Exception e) {
            logger.error("게시물 수정 중 오류 발생 - letterSeq: {}", letterSeq, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // 500 Internal Server Error
        }
    }

    // 게시물 삭제
    @DeleteMapping("/{letterSeq}")
    public ResponseEntity<Void> delete(@PathVariable("letterSeq") int letterSeq,
                                       @RequestBody RecipientEntity recipientEntityRequest){
        try {
            recipientService.deleteRecipient(letterSeq, recipientEntityRequest.getLetterPasscode());
            return ResponseEntity.noContent().build();  // 204 No Content (성공적으로 삭제됨)
        }
        catch (NoSuchElementException e) { // 게시물을 찾을 수 없는 경우
            logger.warn("게시물 삭제 실패: 게시물을 찾을 수 없습니다. letterSeq: {}", letterSeq);
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
        catch (IllegalArgumentException e) { // 비밀번호 불일치
            logger.warn("게시물 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }
        catch (Exception e) {
            logger.error("게시물 삭제 중 오류 발생 - letterSeq: {}", letterSeq, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }
}
