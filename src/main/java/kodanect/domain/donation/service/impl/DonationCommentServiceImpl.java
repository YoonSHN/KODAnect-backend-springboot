package kodanect.domain.donation.service.impl;

import kodanect.common.util.MessageResolver;
import kodanect.domain.donation.dto.request.DonationCommentCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryCommentModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyCommentPasscodeDto;
import kodanect.domain.donation.dto.response.DonationStoryCommentDto;
import kodanect.domain.donation.entity.DonationStory;
import kodanect.domain.donation.entity.DonationStoryComment;
import kodanect.domain.donation.exception.BadRequestException;
import kodanect.domain.donation.exception.NotFoundException;
import kodanect.domain.donation.repository.DonationCommentRepository;
import kodanect.domain.donation.repository.DonationRepository;
import kodanect.domain.donation.service.DonationCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationCommentServiceImpl implements DonationCommentService {

    private final DonationCommentRepository commentRepository;
    private final DonationRepository storyRepository;
    private final CaptchaService captchaService;
    private final MessageResolver messageResolver;

    /**
     * 댓글 등록
     */
    @Transactional
    public void createDonationStoryComment(Long storySeq, DonationCommentCreateRequestDto requestDto) {
        DonationStory story = storyRepository.findById(storySeq)
                .orElseThrow(() -> new NotFoundException(messageResolver.get("donation.error.notfound")));

        // 작성자 필수 검증
        if (requestDto.getCommentWriter() == null || requestDto.getCommentWriter().isBlank()) {
            throw new BadRequestException(messageResolver.get("donation.error.required.writer"));
        }

        // 비밀번호 필수 및 형식 검증
        if (requestDto.getCommentPasscode() == null || requestDto.getCommentPasscode().isBlank()) {
            throw new BadRequestException(messageResolver.get("donation.error.required.passcode"));
        }
        if (!validatePassword(requestDto.getCommentPasscode())) {
            throw new BadRequestException(messageResolver.get("donation.error.invalid.passcode.format"));
        }

        // 캡차 검증 (추후 적용 시 true로 수정)
        if (false /* !captchaService.verifyCaptcha(requestDto.getCaptchaToken()) */) {
            throw new BadRequestException(messageResolver.get("donation.error.captcha.failed"));
        }

        // 댓글 생성 및 연관 관계 설정
        DonationStoryComment comment = DonationStoryComment.builder()
                .commentWriter(requestDto.getCommentWriter())
                .commentPasscode(requestDto.getCommentPasscode())
                .contents(requestDto.getContents())
                .writerId(null)
                .modifyTime(null)
                .build();

        story.addComment(comment); // 연관관계 편의 메서드
        commentRepository.save(comment);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public void modifyDonationComment(Long commentSeq, DonationStoryCommentModifyRequestDto requestDto) {
        DonationStoryComment storyComment = commentRepository.findById(commentSeq)
                .orElseThrow(() -> new NotFoundException(messageResolver.get("donation.comment.error.notfound")));

        // 작성자 검증
        if (requestDto.getCommentWriter() == null || requestDto.getCommentWriter().isBlank()) {
            throw new BadRequestException(messageResolver.get("donation.error.required.writer"));
        }

        // 비밀번호 형식 및 일치 여부 확인
        if (!validatePassword(requestDto.getCommentPasscode())) {
            throw new BadRequestException(messageResolver.get("donation.error.invalid.passcode.format"));
        }
        if (!storyComment.getCommentPasscode().equals(requestDto.getCommentPasscode())) {
            throw new BadRequestException(messageResolver.get("donation.error.passcode.mismatch"));
        }

        // 캡차 검증 (추후 적용 시 true로 수정)
        if (false /* !captchaService.verifyCaptcha(requestDto.getCaptchaToken()) */) {
            throw new BadRequestException(messageResolver.get("donation.error.captcha.failed"));
        }

        // 댓글 내용 수정
        storyComment.modifyDonationStoryComment(requestDto);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteDonationComment(Long commentSeq, VerifyCommentPasscodeDto commentDto) {
        DonationStoryComment storyComment = commentRepository.findById(commentSeq)
                .orElseThrow(() -> new NotFoundException(messageResolver.get("donation.comment.error.notfound")));

        // 비밀번호 일치 여부 확인
        if (!commentDto.getCommentPasscode().equals(storyComment.getCommentPasscode())) {
            throw new BadRequestException(messageResolver.get("donation.comment.error.passcode.mismatch"));
        }

        // 연관된 스토리에서도 댓글 제거
        DonationStory story = storyComment.getStory();
        if (story != null) {
            story.removeComment(storyComment);
        }

        commentRepository.delete(storyComment);
    }

    /**
     * 댓글 비밀번호 유효성 검사
     */
    public boolean validatePassword(String password) {
        return password != null && password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,16}$");
    }

    /**
     * 특정 게시글의 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<DonationStoryCommentDto> getCommentsByStoryId(Long storySeq, Pageable pageable) {
        return commentRepository.findCommentsByStoryId(storySeq, pageable);
    }

}
