package kodanect.domain.donation.controller;

import kodanect.common.response.ApiResponse;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.donation.dto.request.*;
import kodanect.domain.donation.dto.response.*;
import kodanect.domain.donation.service.DonationCommentService;
import kodanect.domain.donation.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/donationLetters")
public class  DonationController {

    private static final String DEFAULT_STRING = "result";

    private final DonationService donationService;
    private final DonationCommentService donationCommentService;
    private final MessageSourceAccessor messageSourceAccessor;

    /**
     * 기증 스토리 전체 목록 조회 (더보기 방식 페이징 포함)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPaginationResponse<DonationStoryListDto, Long>>> getAllDonationList(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        CursorPaginationResponse<DonationStoryListDto, Long> response = donationService.findStoriesWithCursor(cursor, size);

        String message = messageSourceAccessor.getMessage("board.list.get.success");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, response));
    }

    /**
     * 기증 스토리 검색 (제목/내용 기준, 커서 기반 페이징 포함)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorPaginationResponse<DonationStoryListDto, Long>>> searchDonationStories(
            @RequestParam("type") String type,
            @RequestParam("keyword") String keyword,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        CursorPaginationResponse<DonationStoryListDto, Long> response =
                donationService.findSearchStoriesWithCursor(type, keyword, cursor, size);

        String message = messageSourceAccessor.getMessage("donation.search.success");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, response));
    }
    /**
     * 기증 스토리 작성 폼에 필요한 데이터 반환
     */
    @GetMapping("/new")
    public ResponseEntity<ApiResponse<DonationStoryWriteFormDto>> getDonationWriteForm() {
        DonationStoryWriteFormDto formDto = donationService.loadDonationStoryFormData();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "폼 데이터 로드 성공", formDto));
    }

    /**
     * 기증 스토리 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createStory(@ModelAttribute @Valid DonationStoryCreateRequestDto requestDto) {
        donationService.createDonationStory(requestDto);
        String message = messageSourceAccessor.getMessage("donation.create.success");
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, message));
    }

    /**
     * 특정 기증 스토리 상세 조회
     */
    @GetMapping("/{storySeq}")
    public ResponseEntity<ApiResponse<DonationStoryDetailDto>> getDonationStoryDetail(@PathVariable Long storySeq) {
        DonationStoryDetailDto detailDto = donationService.findDonationStoryWithStoryId(storySeq);
        String message = messageSourceAccessor.getMessage("board.read.success");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, detailDto));
    }
    /**
     * 기증 스토리 수정 인증
     */
    @PostMapping("/{storySeq}/verifyPwd")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyStoryPassword(
            @PathVariable Long storySeq,
            @RequestBody @Valid VerifyStoryPasscodeDto passCodeDto) {

        donationService.verifyPasswordWithPassword(storySeq, passCodeDto);

        String message = messageSourceAccessor.getMessage("donation.password.match");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, Map.of(DEFAULT_STRING, 1)));
    }


    /**
     * 기증 스토리 수정
     */
    @PatchMapping(value = "/{storySeq}")
    public ResponseEntity<ApiResponse<Void>> modifyStory(
            @PathVariable Long storySeq,
            @ModelAttribute @Valid DonationStoryModifyRequestDto requestDto) {

        donationService.updateDonationStory(storySeq, requestDto);
        String message = messageSourceAccessor.getMessage("donation.update.success");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, message));
    }

    /**
     * 기증 스토리 삭제
     */
    @DeleteMapping("/{storySeq}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteStory(
            @PathVariable Long storySeq,
            @RequestBody @Valid VerifyStoryPasscodeDto storyPasscodeDto) {

        donationService.deleteDonationStory(storySeq, storyPasscodeDto);
        String message = messageSourceAccessor.getMessage("donation.delete.success");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, Map.of(DEFAULT_STRING, 1)));
    }

    /**
     * 상세 페이지 더보기 댓글 조회
     */
    @GetMapping("/{storySeq}/comments")
    public ResponseEntity<ApiResponse<CursorPaginationResponse<DonationStoryCommentDto, Long>>> getAllDonationCommentList(
            @PathVariable("storySeq") Long storySeq,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "3") int size
    ){
        CursorPaginationResponse<DonationStoryCommentDto, Long> response = donationCommentService.findCommentsWithCursor(storySeq, cursor, size);

        String message = messageSourceAccessor.getMessage("donation.commentSuccess");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, response));
    }


    /**
     * 댓글 작성(등록)
     */
    @PostMapping("/{storySeq}/comments")
    public ResponseEntity<ApiResponse<Void>> createComment(
            @PathVariable Long storySeq,
            @RequestBody @Valid DonationCommentCreateRequestDto requestDto) {

        donationCommentService.createDonationStoryComment(storySeq, requestDto);
        String message = messageSourceAccessor.getMessage("donation.comment.create.success");
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                message));
    }

    /**
     * 댓글 수정 인증
     */
    @PostMapping("/{storySeq}/comments/{commentSeq}/verifyPwd")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyCommentPassword(
            @PathVariable Long storySeq,
            @PathVariable Long commentSeq,
            @RequestBody @Valid VerifyCommentPasscodeDto passCodeDto) {

        donationCommentService.verifyPasswordWithPassword(storySeq, commentSeq, passCodeDto);
        String message = messageSourceAccessor.getMessage("donation.password.match");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, Map.of(DEFAULT_STRING, 1)));
    }


    /**
     * 댓글 수정
     */
    @PatchMapping("/{storySeq}/comments/{commentSeq}")
    public ResponseEntity<ApiResponse<Void>> modifyComment(
            @PathVariable Long storySeq,
            @PathVariable Long commentSeq,
            @RequestBody  @Valid DonationStoryCommentModifyRequestDto requestDto) {

        donationCommentService.updateDonationComment(storySeq, commentSeq, requestDto);
        String message = messageSourceAccessor.getMessage("donation.comment.update.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message));
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{storySeq}/comments/{commentSeq}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long storySeq,
            @PathVariable Long commentSeq,
            @RequestBody @Valid VerifyCommentPasscodeDto commentPassword) {

        donationCommentService.deleteDonationComment(storySeq, commentSeq, commentPassword);
        String message = messageSourceAccessor.getMessage("donation.comment.delete.success");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK,message));
    }

    /**
     * 문자열로 넘어온 권역 코드를 enum 타입인 AreaCode로 변환해주는 바인더 등록 메서드
     * - 스프링 MVC가 문자열을 AreaCode 타입으로 자동 변환할 수 있게 도와줌
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(AreaCode.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(AreaCode.valueOf(text));
            }
        });
    }
}