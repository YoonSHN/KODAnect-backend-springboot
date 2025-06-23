package kodanect.domain.donation.service.impl;

import kodanect.common.exception.config.SecureLogger;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DonationCommentServiceImpl implements DonationCommentService {

    // 로거 선언
    private static final SecureLogger logger = SecureLogger.getLogger(DonationCommentServiceImpl.class);


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
        logger.debug(">>> findCommentWithCursor() 호출");
        logger.info("댓글 더보기 조회 - storySeq : {}, cursor : {}, size ; {} ", storySeq, cursor, size);
        Pageable pageable = PageRequest.of(0, size + 1);

        List<DonationStoryComment> commentEntities = commentRepository.findByCursorEntity(storySeq, cursor, pageable);
        logger.debug("조회된 댓글 수: {}", commentEntities.size());

        //  Entity → DTO 변환 (정적 팩토리 메서드 사용)
        List<DonationStoryCommentDto> comments = commentEntities.stream()
                .map(DonationStoryCommentDto::fromEntity)
                .toList();

        long totalCount = commentRepository.countAllByStorySeq(storySeq);
        logger.debug("전체 댓글 수 : {}", totalCount);
        return CursorFormatter.cursorFormat(comments, size, totalCount);
    }


    /**
     * 댓글 등록
     */
    public void createDonationStoryComment(Long storySeq, DonationCommentCreateRequestDto requestDto)
            throws NotFoundException, BadRequestException, DonationCommentNotFoundException {
        logger.debug(">>> createDonationStoryComment() 호출");
        logger.info("댓글 등록 - storySeq : {}, requestDto : {}" , storySeq, requestDto);
        DonationStory story = storyRepository.findById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(messageResolver.get(DONATION_NOT_FOUND_MESSAGE)));
        // 작성자 필수 검증
        if (requestDto.getCommentWriter() == null || requestDto.getCommentWriter().isBlank()) {
            throw new BadRequestException(messageResolver.get("donation.error.required.writer"));
        }

        validateWriter(requestDto.getCommentWriter());

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
        logger.info("댓글 등록 - comment : {}", comment);
    }

    /**
     *댓글 수정 인증
     */

    @Override
    public void verifyPasswordWithPassword(Long storySeq, Long commentSeq, VerifyCommentPasscodeDto commentPassCodeDto) {
        logger.debug(">>> verifyPasswordWithPassword() 호출");
        logger.info("댓글 수정 인증 - storySeq : {}, commentSeq : {}",storySeq, commentSeq);
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

        logger.info("댓글 수정 인증 성공 - commentSeq: {}", commentSeq);
    }


    /**
     * 댓글 수정
     */
    public void updateDonationComment(Long storySeq, Long commentSeq, DonationStoryCommentModifyRequestDto requestDto) {
        logger.debug(">>> updateDonationComment() 호출");
        logger.info("댓글 수정 - storySeq : {}, commentSeq : {}, requestDto : {}",storySeq, commentSeq, requestDto);
        DonationStory story = storyRepository.findById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(messageResolver.get("donation.error.delete.not_found")));
        DonationStoryComment storyComment = commentRepository.findById(commentSeq)
                .orElseThrow(() -> new DonationCommentNotFoundException(messageResolver.get(DONATION_COMMENT_ERROR_NOTFOUND)));

        if (commentRepository.existsCommentInStory(storySeq, commentSeq) == 0) {
            throw new PasscodeMismatchException("error.comment.story.isNotMatch");
        }
        // 작성자 검증
        if (requestDto.getCommentWriter() == null || requestDto.getCommentWriter().isBlank()) {
            throw new PasscodeMismatchException(messageResolver.get("donation.error.required.writer"));
        }
        validateWriter(requestDto.getCommentWriter());
        // 댓글 내용 수정
        storyComment.modifyDonationStoryComment(requestDto);
        logger.info("댓글 수정 완료 : {}", requestDto);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteDonationComment(Long storySeq, Long commentSeq, VerifyCommentPasscodeDto commentDto) {
        logger.debug(">>> deleteDonationComment() 호출");
        logger.info("댓글 삭제 - storySeq : {}, commentSeq : {}", storySeq, commentSeq);
        DonationStory story = storyRepository.findById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(messageResolver.get("donation.error.delete.not_found")));
        DonationStoryComment storyComment = commentRepository.findById(commentSeq)
                .orElseThrow(() -> new PasscodeMismatchException(messageResolver.get(DONATION_COMMENT_ERROR_NOTFOUND)));

        if (commentRepository.existsCommentInStory(storySeq, commentSeq) == 0) {
            throw new PasscodeMismatchException("error.comment.story.isNotMatch");
        }

        // 비밀번호 일치 여부 확인
        if (!commentDto.getCommentPasscode().equals(storyComment.getCommentPasscode())) {
            throw new PasscodeMismatchException(messageResolver.get("donation.error.delete.password_mismatch"));
        }

        // 연관된 스토리에서도 댓글 제거
        story = storyComment.getStory();
        if (story != null) {
            story.removeComment(storyComment);
        }

        storyComment.softDelete();
        commentRepository.save(storyComment);
        logger.info("댓글 삭제 완료 - comment : {}", storyComment);
    }

    /**
     * 댓글 비밀번호 유효성 검사
     */
    public boolean validatePassword(String password) {
        return password != null && password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,16}$");
    }

    // 작성자 닉네임 유효성 추가( 한글, 영어, 공백 1~30 글자 가능, 특수 문자,숫자 불가능)
    private void validateWriter(String writer){
        // 한글, 영어, 공백만 허용 (1~30자), 숫자와 특수문자는 불가
        if(writer == null || !writer.matches("^[a-zA-Z가-힣\\s]{1,30}$")){
            throw new InvalidWriterException(messageResolver.get("donation.writer.invalid"));
        }
    }

}
