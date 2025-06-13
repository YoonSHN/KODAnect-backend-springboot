package kodanect.domain.donation.service;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.donation.dto.request.DonationCommentCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryCommentModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyCommentPasscodeDto;
import kodanect.domain.donation.dto.response.DonationStoryCommentDto;


public interface DonationCommentService {

    // 기증 스토리 댓글 등록
    void createDonationStoryComment(Long storySeq, DonationCommentCreateRequestDto requestDto);

    //댓글 수정 인증
    void verifyPasswordWithPassword(Long storySeq, Long commentSeq, VerifyCommentPasscodeDto commentPassCodeDto);

    // 기증 스토리 댓글 수정
    void updateDonationComment(Long storySeq, Long commentSeq, DonationStoryCommentModifyRequestDto requestDto);

    // 기증 스토리 댓글 삭제
    void deleteDonationComment(Long storySeq, Long commentSeq, VerifyCommentPasscodeDto commentDto);

    // 비밀번호 유효성 검사
    boolean validatePassword(String password);

    // 댓글 조회
    CursorPaginationResponse<DonationStoryCommentDto, Long> findCommentsWithCursor(Long storySeq, Long cursor, int size);



}