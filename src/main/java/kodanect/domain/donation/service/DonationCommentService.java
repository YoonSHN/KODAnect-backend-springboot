package kodanect.domain.donation.service;

import kodanect.domain.donation.dto.request.DonationCommentCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryCommentModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyCommentPasscodeDto;
import kodanect.domain.donation.dto.response.DonationStoryCommentDto;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface DonationCommentService {

    // 기증 스토리 댓글 등록
    void createDonationStoryComment(Long storySeq, DonationCommentCreateRequestDto requestDto);

    // 기증 스토리 댓글 수정
    void modifyDonationComment(Long commentSeq, DonationStoryCommentModifyRequestDto requestDto);

    // 기증 스토리 댓글 삭제
    void deleteDonationComment(Long commentSeq, VerifyCommentPasscodeDto commentDto);

    // 비밀번호 유효성 검사
    boolean validatePassword(String password);

    public List<DonationStoryCommentDto> getCommentsByStoryId(Long storySeq, Pageable pageable);

}