package kodanect.domain.donation.service;

import kodanect.common.util.MessageResolver;
import kodanect.domain.donation.dto.OffsetBasedPageRequest;
import kodanect.domain.donation.dto.request.DonationStoryCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyStoryPasscodeDto;
import kodanect.domain.donation.dto.response.AreaCode;
import kodanect.domain.donation.dto.response.DonationStoryDetailDto;
import kodanect.domain.donation.dto.response.DonationStoryListDto;
import kodanect.domain.donation.dto.response.DonationStoryWriteFormDto;
import kodanect.domain.donation.entity.DonationStory;
import kodanect.domain.donation.exception.BadRequestException;
import kodanect.domain.donation.exception.NotFoundException;
import kodanect.domain.donation.repository.DonationRepository;
import kodanect.domain.donation.service.impl.DonationServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DonationServiceImplTest {

    @Mock private DonationRepository donationRepository;
    @Mock private MessageResolver messageResolver;
    @InjectMocks private DonationServiceImpl donationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void findStoriesWithOffset_정상_조회() {
        Pageable pageable = new OffsetBasedPageRequest(0, 2, Sort.by("storySeq").descending());
        List<DonationStoryListDto> mockList = List.of(
                new DonationStoryListDto(1L, "제목1", "작성자1", 10, null),
                new DonationStoryListDto(2L, "제목2", "작성자2", 20, null),
                new DonationStoryListDto(3L, "제목3", "작성자3", 30, null)
        );

        when(donationRepository.findSliceDonationStoriesWithOffset(any())).thenReturn(mockList);

        var result = donationService.findStoriesWithOffset(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    public void loadDonationStoryFormData_정상() {
        DonationStoryWriteFormDto dto = donationService.loadDonationStoryFormData();
        assertThat(dto.getAreaOptions()).containsExactly(AreaCode.AREA100, AreaCode.AREA200, AreaCode.AREA300);
    }

    @Test
    public void createDonationStory_정상등록_파일없음() {
        DonationStoryCreateRequestDto dto = new DonationStoryCreateRequestDto(
                AreaCode.AREA100, "제목", "abcd1234", "작성자", "내용", null, null
        );
        donationService.createDonationStory(dto);
        verify(donationRepository).save(any(DonationStory.class));
    }

    @Test(expected = BadRequestException.class)
    public void createDonationStory_비밀번호형식오류_예외() {
        DonationStoryCreateRequestDto dto = new DonationStoryCreateRequestDto(
                AreaCode.AREA100, "제목", "1234", "작성자", "내용", null, null
        );
        when(messageResolver.get(any())).thenReturn("비밀번호 형식 오류");
        donationService.createDonationStory(dto); // 예외 발생
    }

    @Test
    public void findDonationStory_정상조회_조회수증가() {
        DonationStory story = DonationStory.builder()
                .storySeq(1L).storyTitle("제목").storyPasscode("abcd1234")
                .storyContents("내용").readCount(0).areaCode(AreaCode.AREA100)
                .writeTime(LocalDateTime.now()).delFlag("N")
                .build();

        when(donationRepository.findWithCommentsById(1L)).thenReturn(Optional.of(story));
        DonationStoryDetailDto dto = donationService.findDonationStory(1L);
        assertThat(dto.getTitle()).isEqualTo("제목");
        assertThat(story.getReadCount()).isEqualTo(1);
    }

    @Test(expected = NotFoundException.class)
    public void findDonationStory_존재하지않음_예외() {
        when(donationRepository.findWithCommentsById(99L)).thenReturn(Optional.empty());
        when(messageResolver.get(any())).thenReturn("찾을 수 없음");
        donationService.findDonationStory(99L); // 예외 발생
    }

    @Test(expected = BadRequestException.class)
    public void verifyPasswordWithPassword_불일치_예외() {
        DonationStory story = DonationStory.builder().storySeq(1L).storyPasscode("abcd1234").build();
        when(donationRepository.findById(1L)).thenReturn(Optional.of(story));
        when(messageResolver.get(any())).thenReturn("비밀번호 불일치");

        VerifyStoryPasscodeDto passcode = new VerifyStoryPasscodeDto("wrongpass");
        donationService.verifyPasswordWithPassword(1L, passcode); // 예외 발생
    }

    @Test
    public void deleteDonationStory_성공() {
        DonationStory story = DonationStory.builder().storySeq(1L).storyPasscode("abcd1234").build();
        when(donationRepository.findWithCommentsById(1L)).thenReturn(Optional.of(story));

        donationService.deleteDonationStory(1L, new VerifyStoryPasscodeDto("abcd1234"));

        verify(donationRepository).delete(story);
    }

    @Test
    public void modifyDonationStory_성공() {
        DonationStory story = DonationStory.builder().storySeq(1L).storyTitle("기존제목").areaCode(AreaCode.AREA100).build();
        when(donationRepository.findWithCommentsById(1L)).thenReturn(Optional.of(story));

        DonationStoryModifyRequestDto dto = DonationStoryModifyRequestDto.builder()
                .storyTitle("수정된제목").areaCode(AreaCode.AREA200).build();

        donationService.modifyDonationStory(1L, dto);

        assertThat(story.getStoryTitle()).isEqualTo("수정된제목");
        assertThat(story.getAreaCode()).isEqualTo(AreaCode.AREA200);
    }
}