package kodanect.domain.donation.controller;

import kodanect.common.response.ApiResponse;
import kodanect.domain.donation.dto.OffsetBasedPageRequest;
import kodanect.domain.donation.dto.request.*;
import kodanect.domain.donation.dto.response.AreaCode;
import kodanect.domain.donation.dto.response.DonationStoryDetailDto;
import kodanect.domain.donation.dto.response.DonationStoryListDto;
import kodanect.domain.donation.dto.response.DonationStoryWriteFormDto;
import kodanect.domain.donation.service.DonationCommentService;
import kodanect.domain.donation.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping()
public class DonationController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final DonationService donationService;
    private final DonationCommentService donationCommentService;
    private final MessageSourceAccessor messageSourceAccessor;

    /**
     * 기증 스토리 전체 목록 조회 (더보기 방식 페이징 포함)
     */
    @GetMapping("/donationLetters")
    public ResponseEntity<ApiResponse<?>> getAllDonationList(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, Sort.by("storySeq").descending());
        Slice<DonationStoryListDto> slice = donationService.findStoriesWithOffset(pageable);


        String message = messageSourceAccessor.getMessage("board.list.get.success");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, slice));
    }

    /**
     * 기증 스토리 검색 (제목/내용 기준, 페이징 포함)
     */
    @GetMapping("/donationLetters/search")
    public ResponseEntity<ApiResponse<?>> searchDonationStories(
            @RequestParam("type") String type,
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, Sort.by("storySeq").descending());
        Slice<DonationStoryListDto> slice = donationService.findDonationStorySearchResult(pageable, type, keyword);



        String message = messageSourceAccessor.getMessage("board.list.get.success");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, slice));
    }

    /**
     * 기증 스토리 작성 폼에 필요한 데이터 반환
     */
    @GetMapping("/donationLetters/new")
    public ResponseEntity<ApiResponse<DonationStoryWriteFormDto>> getDonationWriteForm() {
        DonationStoryWriteFormDto formDto = donationService.loadDonationStoryFormData();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "폼 데이터 로드 성공", formDto));
    }

    /**
     * 기증 스토리 등록
     */
    @PostMapping(value = "/donationLetters", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> createStory(@ModelAttribute @Valid DonationStoryCreateRequestDto requestDto) {
        donationService.createDonationStory(requestDto);
        String message = messageSourceAccessor.getMessage("donation.create.success");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, message));
    }

    /**
     * 특정 기증 스토리 상세 조회
     */
    @GetMapping("/donationLetters/{storySeq}")
    public ResponseEntity<ApiResponse<DonationStoryDetailDto>> getDonationStoryDetail(@PathVariable Long storySeq) {
        DonationStoryDetailDto detailDto = donationService.findDonationStory(storySeq);
        String message = messageSourceAccessor.getMessage("article.detail.success");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, detailDto));
    }

    /**
     * 기증 스토리 수정 인증
     */
    @PostMapping("/donationLetters/{storySeq}/verifyPwd")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyStoryPassword(
            @PathVariable Long storySeq,
            @RequestBody VerifyStoryPasscodeDto passCodeDto) {

        donationService.verifyPasswordWithPassword(storySeq, passCodeDto);
        String message = messageSourceAccessor.getMessage("donation.password.match");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, Map.of("result", 1)));
    }

    /**
     * 기증 스토리 수정
     */
    @PatchMapping(value = "/donationLetters/{storySeq}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> modifyStory(
            @PathVariable Long storySeq,
            @ModelAttribute @Valid DonationStoryModifyRequestDto requestDto) {

        donationService.modifyDonationStory(storySeq, requestDto);
        String message = messageSourceAccessor.getMessage("donation.update.success");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, "스토리가 성공적으로 수정되었습니다."));
    }

    /**
     * 기증 스토리 삭제
     */
    @DeleteMapping("/donationLetters/{storySeq}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteStory(
            @PathVariable Long storySeq,
            @RequestBody VerifyStoryPasscodeDto storyPasscodeDto) {

        donationService.deleteDonationStory(storySeq, storyPasscodeDto);
        String message = messageSourceAccessor.getMessage("donation.delete.success");
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, Map.of("result", 1)));
    }

    /**
     * 댓글 작성(등록)
     */
    @PostMapping("/donationLetters/{storySeq}/comments")
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
     * 댓글 수정
     */
    @PatchMapping("/donationLetters/{storySeq}/comments/{commentSeq}")
    public ResponseEntity<ApiResponse<Void>> modifyComment(
            @PathVariable Long storySeq,
            @PathVariable Long commentSeq,
            @RequestBody DonationStoryCommentModifyRequestDto requestDto) {

        donationCommentService.modifyDonationComment(commentSeq, requestDto);
        String message = messageSourceAccessor.getMessage("donation.comment.update.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message));
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/donationLetters/{storySeq}/comments/{commentSeq}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long storySeq,
            @PathVariable Long commentSeq,
            @RequestBody VerifyCommentPasscodeDto commentPassword) {

        donationCommentService.deleteDonationComment(commentSeq, commentPassword);
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