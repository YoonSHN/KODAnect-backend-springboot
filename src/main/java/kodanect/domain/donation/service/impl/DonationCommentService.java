package kodanect.domain.donation.service.impl;


import kodanect.domain.donation.dto.request.DonationCommentCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryCommentModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyCommentPasscodeDto;
import kodanect.domain.donation.entity.DonationStory;
import kodanect.domain.donation.entity.DonationStoryComment;
import kodanect.domain.donation.repository.DonationCommentRepository;
import kodanect.domain.donation.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DonationCommentService {

    private final DonationCommentRepository commentRepository;
    private final DonationRepository storyRepository;
    private final CaptchaService captchaService;

    @Transactional
    public void createDonationStoryComment(Long storySeq, DonationCommentCreateRequestDto requestDto) {
        DonationStory story = storyRepository.findById(storySeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if (requestDto.getCommentWriter() == null || requestDto.getCommentWriter().isBlank()) {
            throw new IllegalArgumentException("댓글 작성자가 존재하지 않습니다.");
        }
        if (requestDto.getCommentPasscode() == null || requestDto.getCommentPasscode().isBlank()) {
            throw new IllegalArgumentException("비밀번호 입력은 필수입니다.");
        }
        //!captchaService.verifyCaptcha(requestDto.getCaptchaToken())
        if (false) {
            throw new IllegalArgumentException("캡차 인증 실패");
        }
        if(!validatePassword(requestDto.getCommentPasscode())){
            throw new IllegalArgumentException("패스워드가 형식에 맞지 않습니다.");
        }

        DonationStoryComment comment = DonationStoryComment.builder()
                .commentWriter(requestDto.getCommentWriter())
                .commentPasscode(requestDto.getCommentPasscode())
                .contents(requestDto.getContents())
                .writerId(null)
                .modifyTime(null)
                .build();

        story.addComment(comment); //연관관계 편의 메서드로 양방향 관계 설정

        commentRepository.save(comment);
    }

    @Transactional
    public void modifyDonationComment(Long commentSeq, DonationStoryCommentModifyRequestDto requestDto){
        DonationStoryComment storyComment = commentRepository.findById(commentSeq)
                .orElseThrow( () -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        if(requestDto.getCommentWriter() == null || requestDto.getCommentWriter().isBlank()){
            throw new IllegalArgumentException("댓글 작성자가 존재하지 않습니다.");
        }
        //!captchaService.verifyCaptcha(requestDto.getCaptchaToken())
        if (false) {
            throw new IllegalArgumentException("캡차 인증 실패");
        }
        if(!validatePassword(requestDto.getCommentPasscode())){
            throw new IllegalArgumentException("패스워드가 형식에 맞지 않습니다.");
        }
        if(!storyComment.getCommentPasscode().equals(requestDto.getCommentPasscode())){
            throw new IllegalArgumentException("패스워드가 일치하지 않습니다.");
        }


        storyComment.modifyDonationStoryComment(requestDto);
    }

    @Transactional
    public void deleteDonationComment(Long commentSeq, VerifyCommentPasscodeDto commentDto){
        DonationStoryComment storyComment = commentRepository.findById(commentSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        if(!commentDto.getCommentPasscode().equals(storyComment.getCommentPasscode())){
            throw new IllegalArgumentException("패스워드가 틀립니다.");
        }
        DonationStory story = storyComment.getStory();
        if (story != null) {
            story.removeComment(storyComment);
        }

        commentRepository.delete(storyComment);

    }


    //비밀번호 검증 (영 + 숫자 + 8자 ~ 16자 코드) - story, comment 모두 처리
    public boolean validatePassword(String password){
        if(password.matches("^[a-zA-Z0-9]{8,16}$")){
            return true;
        }
        return false;
    }

}
