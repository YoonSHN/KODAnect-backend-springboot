package kodanect.domain.donation.service;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.donation.dto.request.DonationStoryCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyStoryPasscodeDto;
import kodanect.domain.donation.dto.response.DonationStoryDetailDto;
import kodanect.domain.donation.dto.response.DonationStoryListDto;
import kodanect.domain.donation.dto.response.DonationStoryWriteFormDto;

public interface DonationService {

    CursorPaginationResponse<DonationStoryListDto, Long> findStoriesWithCursor(Long cursor, int size);

    CursorPaginationResponse<DonationStoryListDto, Long> findSearchStoriesWithCursor(String type, String keyword, Long cursor, int size);

    // 기증 스토리 상세 조회
    DonationStoryDetailDto findDonationStoryWithStoryId(Long storySeq);

    // 스토리 작성 폼 데이터 로드
    DonationStoryWriteFormDto loadDonationStoryFormData();

    // 기증 스토리 등록
    void createDonationStory(DonationStoryCreateRequestDto requestDto);



    // 비밀번호 검증
    void verifyPasswordWithPassword(Long storySeq, VerifyStoryPasscodeDto verifyPassword);

    // 기증 스토리 수정
    void updateDonationStory(Long storySeq, DonationStoryModifyRequestDto requestDto);

    // 기증 스토리 삭제
    void deleteDonationStory(Long storySeq, VerifyStoryPasscodeDto storyPasscodeDto);

    // 비밀번호 유효성 검사
    boolean validatePassword(String password);



}