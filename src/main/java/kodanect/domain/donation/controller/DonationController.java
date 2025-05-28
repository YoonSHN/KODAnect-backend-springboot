package kodanect.domain.donation.controller;


import kodanect.domain.donation.dto.request.*;
import kodanect.domain.donation.dto.response.AreaCode;
import kodanect.domain.donation.dto.response.DonationStoryDetailDto;
import kodanect.domain.donation.dto.response.DonationStoryListDto;
import kodanect.domain.donation.dto.response.DonationStoryWriteFormDto;
import kodanect.domain.donation.service.impl.DonationCommentService;
import kodanect.domain.donation.service.impl.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    private final DonationService donationService;
    private final DonationCommentService donationCommentService;

    private static final int DEFAULT_PAGE_SIZE = 20;

    @GetMapping("/donationLetters")
    public ResponseEntity<?> getAllDonationList(
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "storySeq", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        try {
            Page<DonationStoryListDto> page = donationService.findAllDonationStories(pageable);
            System.out.println("서비스 호출 완료");

            return ResponseEntity.ok(Map.of("success", true, "code", 200, "message", "기증 후 스토리 목록 가져오기 성공", "data", page.getContent(), "pageInfo", Map.of("totalPages", page.getTotalPages(), "totalElements", page.getTotalElements(), "currentPage", page.getNumber(), "pageSize", page.getSize(), "numberOfElements", page.getNumberOfElements(), "isFirst", page.isFirst(), "isLast", page.isLast(), "hasNext", page.hasNext(), "hasPrevious", page.hasPrevious(), "sort", page.getSort().toString())));
        } catch (RuntimeException re) {
            re.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "code", 500, "message", "기증 후 스토리 목록 가져오기 실패"));
        }
    }

    @GetMapping("/donationLetters/new")
    public ResponseEntity<?> getDonationWriteForm() {
        DonationStoryWriteFormDto formDto = donationService.loadDonationStoryFormData();
        if (formDto != null) {
            return ResponseEntity.ok(Map.of("success", true, "code", 200, "message", "폼 데이터 로드 성공", "data", donationService.loadDonationStoryFormData()));
        }
        return ResponseEntity.status(500).body(Map.of("success", false, "code", 500, "message", "폼 데이터 로드 실패"));

    }

    @PostMapping(value = "/donationLetters", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createStory(@ModelAttribute @Valid DonationStoryCreateRequestDto requestDto) throws Exception {
        try {
            donationService.createDonationStory(requestDto);
            return ResponseEntity.ok(Map.of("success", true, "code", 201, "message", "스토리가 성공적으로 등록되었습니다."));
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.status(400).body(Map.of("success", false, "code", 400, "message", "필수 입력값이 누락되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "code", 500, "message", "서버 내부 오류가 발생했습니다.", "error", e.getMessage()));
        }
    }

    @GetMapping("/donationLetters/{storySeq}")
    public ResponseEntity<?> getDonationStoryDetail(@PathVariable("storySeq") Long storySeq) {
        try {
            DonationStoryDetailDto donationDetailStory = donationService.findDonationStory(storySeq);

            return ResponseEntity.ok(Map.of("success", true, "status", 200, "message", "detail 페이지 출력", "data", donationDetailStory));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "status", 404, "message", "해당 스토리를 찾을 수 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "status", 500, "message", "서버 내부 오류가 발생했습니다.", "error", e.getMessage()));
        }
    }

    /*스토리 수정인증*/
    @PostMapping("/donationLetters/{storySeq}/verifyPwd")
    public ResponseEntity<?> verifyStoryPassword(@PathVariable("storySeq") Long storySeq, @RequestBody VerifyStoryPasscodeDto passCordDto) {
        try {
            donationService.verifyPasswordWithPassword(storySeq, passCordDto);

            return ResponseEntity.ok(Map.of("result", 1, "message", "비밀번호가 일치합니다."));
        } catch (IllegalArgumentException e) {
            String message = switch (e.getMessage()) {
                case "NOT_FOUND" -> "해당 게시글이 존재하지 않습니다.";
                case "MISMATCH_PWD" -> "비밀번호가 일치하지 않습니다.";
                default -> "알 수 없는 오류가 발생했습니다.";
            };
            return ResponseEntity.status(400).body(Map.of("result", 0, "message", message));
        }

    }

    /*
    기증 후 스토리 수정
     */
    @PatchMapping(value = "/donationLetters/{storySeq}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> modifyStory(@PathVariable("storySeq") Long storySeq, @ModelAttribute @Valid DonationStoryModifyRequestDto requestDto) {
        try {
            donationService.modifyDonationStory(storySeq, requestDto);

            return ResponseEntity.ok(Map.of("success", true, "code", 201, "message", "스토리가 성공적으로 수정되었습니다."));
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.status(400).body(Map.of("success", false, "code", 400, "message", "필수 입력값이 누락되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "code", 500, "message", "서버 내부 오류가 발생했습니다."));
        }
    }

    /*
    가증 후 스토리 스토리 삭제
     */
    @DeleteMapping("/donationLetters/{storySeq}")
    public ResponseEntity<?> deleteStory(@PathVariable("storySeq") Long storySeq, @RequestBody VerifyStoryPasscodeDto storyPasscodeDto) {
        try {
            donationService.deleteDonationStory(storySeq, storyPasscodeDto);
            return ResponseEntity.ok(Map.of("result", 1, "message", "스토리가 정상적으로 삭제 되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("result", 0, "message", "비밀번호가 일치하지 않습니다."));
        }
    }

    /*
    기증 후 스토리 댓글 등록
     */
    @PostMapping("/donationLetters/{storySeq}/comments")
    public ResponseEntity<?> createComment(@PathVariable("storySeq") Long storySeq, @RequestBody @Valid DonationCommentCreateRequestDto requestDto) {
        try {
            donationCommentService.createDonationStoryComment(storySeq, requestDto);

            return ResponseEntity.ok(Map.of("success", true, "code", 200, "message", "편지 댓글이 성공적으로 등록되었습니다."));
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.status(400).body(Map.of("success", false, "code", 400, "message", "필수 입력값이 누락되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "code", 500, "message", "서버 내부 오류가 발생했습니다."));
        }
    }

    @PatchMapping("/donationLetters/{storySeq}/comments/{commentSeq}")
    public ResponseEntity<?> modifyComment(@PathVariable("storySeq") Long storySeq, @PathVariable("commentSeq") Long commentSeq, @RequestBody DonationStoryCommentModifyRequestDto requestDto) {
        try {
            donationCommentService.modifyDonationComment(commentSeq, requestDto);

            return ResponseEntity.ok(Map.of("success", true, "code", 201, "message", "스토리 댓글이 성공적으로 수정되었습니다."));
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.status(400).body(Map.of("success", false, "code", 400, "message", "필수 입력값이 누락되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "code", 500, "message", "서버 내부 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/donationLetters/{storySeq}/comments/{commentSeq}")
    public ResponseEntity<?> deleteComment(@PathVariable("storySeq") Long storySeq, @PathVariable("commentSeq") Long commentSeq, @RequestBody VerifyCommentPasscodeDto commentPassword) {
        try {
            donationCommentService.deleteDonationComment(commentSeq, commentPassword);
            return ResponseEntity.ok(Map.of("success", true, "code", 201, "message", "스토리 댓글이 성공적으로 삭제되었습니다."));
        } catch (IllegalArgumentException ie) {
            return ResponseEntity.status(400).body(Map.of("success", false, "code", 400, "message", ie.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "code", 500, "message", e.getMessage()));
        }
    }

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

