package kodanect.domain.donation.service;

import kodanect.domain.donation.dto.request.DonationStoryCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyStoryPasscodeDto;
import kodanect.domain.donation.dto.response.DonationStoryDetailDto;
import kodanect.domain.donation.dto.response.DonationStoryListDto;
import kodanect.domain.donation.dto.response.DonationStoryWriteFormDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface DonationService {

    // 더보기 방식 기증 스토리 목록 조회
    Slice<DonationStoryListDto> findStoriesWithOffset(Pageable pageable);

    // 검색 조건에 따른 기증 스토리 목록 조회 (더보기 방식)
    Slice<DonationStoryListDto> findDonationStorySearchResult(Pageable pageable, String type, String keyword);

    // 스토리 작성 폼 데이터 로드
    DonationStoryWriteFormDto loadDonationStoryFormData();

    // 기증 스토리 등록
    void createDonationStory(DonationStoryCreateRequestDto requestDto);

    // 기증 스토리 상세 조회
    DonationStoryDetailDto findDonationStory(Long storySeq);

    // 비밀번호 검증
    void verifyPasswordWithPassword(Long storySeq, VerifyStoryPasscodeDto verifyPassword);

    // 기증 스토리 수정
    void modifyDonationStory(Long storySeq, DonationStoryModifyRequestDto requestDto);

    // 기증 스토리 삭제
    void deleteDonationStory(Long storySeq, VerifyStoryPasscodeDto storyPasscodeDto);

    // 비밀번호 유효성 검사
    boolean validatePassword(String password);
}