package kodanect.domain.donation.service.impl;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.util.CursorFormatter;
import kodanect.common.util.MessageResolver;
import kodanect.domain.donation.dto.request.DonationCommentCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryCommentModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyCommentPasscodeDto;
import kodanect.domain.donation.dto.response.DonationStoryCommentDto;
import kodanect.domain.donation.entity.DonationStory;
import kodanect.domain.donation.entity.DonationStoryComment;
import kodanect.domain.donation.exception.*;
import kodanect.domain.donation.repository.DonationCommentRepository;
import kodanect.domain.donation.repository.DonationRepository;
import kodanect.domain.donation.service.DonationCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DonationCommentServiceImpl implements DonationCommentService {

    private static final String DONATION_NOT_FOUND_MESSAGE = "donation.error.notfound";
    private static final String DONATION_COMMENT_ERROR_NOTFOUND = "donation.comment.error.notfound";

    private final DonationCommentRepository commentRepository;
    private final DonationRepository storyRepository;
    private final MessageResolver messageResolver;


    /**
     *
     * 댓글 더보기 조회
     */
    @Override
    public CursorPaginationResponse<DonationStoryCommentDto, Long> findCommentsWithCursor(Long storySeq, Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);

        List<DonationStoryComment> commentEntities = commentRepository.findByCursorEntity(storySeq, cursor, pageable);

        //  Entity → DTO 변환 (정적 팩토리 메서드 사용)
        List<DonationStoryCommentDto> comments = commentEntities.stream()
                .map(DonationStoryCommentDto::fromEntity)
                .toList();

        long totalCount = commentRepository.countAllByStorySeq(storySeq);
        return CursorFormatter.cursorFormat(comments, size, totalCount);
    }


    /**
     * 댓글 등록
     */
    public void createDonationStoryComment(Long storySeq, DonationCommentCreateRequestDto requestDto)
            throws NotFoundException, BadRequestException, DonationCommentNotFoundException {

        DonationStory story = storyRepository.findById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(messageResolver.get(DONATION_NOT_FOUND_MESSAGE)));;

        // 작성자 필수 검증
        if (requestDto.getCommentWriter() == null || requestDto.getCommentWriter().isBlank()) {
            throw new BadRequestException(messageResolver.get("donation.error.required.writer"));
        }

        // 비밀번호 필수 및 형식 검증
        if (requestDto.getCommentPasscode() == null || requestDto.getCommentPasscode().isBlank()) {
            throw new PasscodeMismatchException(messageResolver.get("donation.error.required.passcode"));
        }
        if (!validatePassword(requestDto.getCommentPasscode())) {
            throw new BadRequestException(messageResolver.get("donation.error.invalid.passcode.format"));
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
     *댓글 수정 인증
     */

    @Override
    public void verifyPasswordWithPassword(Long storySeq, Long commentSeq, VerifyCommentPasscodeDto commentPassCodeDto) {
        DonationStory story = storyRepository.findById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(messageResolver.get(DONATION_NOT_FOUND_MESSAGE)));

        DonationStoryComment comment = commentRepository.findById(commentSeq)
                .orElseThrow(() -> new DonationCommentNotFoundException(messageResolver.get(DONATION_COMMENT_ERROR_NOTFOUND)));

        if (!comment.getStory().getStorySeq().equals(story.getStorySeq())) {
            throw new BadRequestException("해당 댓글은 지정된 스토리에 속하지 않습니다.");
        }

        if (!validatePassword(commentPassCodeDto.getCommentPasscode())) {
            throw new BadRequestException(messageResolver.get("donation.error.invalid.passcode.format"));
        }
        if(!commentPassCodeDto.getCommentPasscode().equals(comment.getCommentPasscode())){
            throw new PasscodeMismatchException("donation.error.passcode.mismatch");
        }
    }


    /**
     * 댓글 수정
     */
    public void updateDonationComment(Long storySeq, Long commentSeq, DonationStoryCommentModifyRequestDto requestDto) {
        DonationStory story = storyRepository.findById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(messageResolver.get("donation.error.delete.not_found")));
        DonationStoryComment storyComment = commentRepository.findById(commentSeq)
                .orElseThrow(() -> new DonationCommentNotFoundException(messageResolver.get(DONATION_COMMENT_ERROR_NOTFOUND)));

        // 작성자 검증
        if (requestDto.getCommentWriter() == null || requestDto.getCommentWriter().isBlank()) {
            throw new BadRequestException(messageResolver.get("donation.error.required.writer"));
        }
        // 댓글 내용 수정
        storyComment.modifyDonationStoryComment(requestDto);
    }

    /**
     * 댓글 삭제
     */
    public void deleteDonationComment(Long storySeq, Long commentSeq, VerifyCommentPasscodeDto commentDto) {
        DonationStory story = storyRepository.findById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(messageResolver.get("donation.error.delete.not_found")));
        DonationStoryComment storyComment = commentRepository.findById(commentSeq)
                .orElseThrow(() -> new NotFoundException(messageResolver.get(DONATION_COMMENT_ERROR_NOTFOUND)));

        // 비밀번호 일치 여부 확인
        if (!commentDto.getCommentPasscode().equals(storyComment.getCommentPasscode())) {
            throw new PasscodeMismatchException(messageResolver.get("donation.error.delete.password_mismatch"));
        }

        // 연관된 스토리에서도 댓글 제거
        story = storyComment.getStory();
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


}
