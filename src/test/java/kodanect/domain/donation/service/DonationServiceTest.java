package kodanect.domain.donation.service;


import kodanect.domain.donation.dto.request.DonationStoryCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyStoryPasscodeDto;
import kodanect.domain.donation.dto.response.AreaCode;
import kodanect.domain.donation.dto.response.DonationStoryDetailDto;
import kodanect.domain.donation.dto.response.DonationStoryListDto;
import kodanect.domain.donation.dto.response.DonationStoryWriteFormDto;
import kodanect.domain.donation.entity.entity.DonationStory;
import kodanect.domain.donation.repository.DonationRepository;
import kodanect.domain.donation.service.impl.DonationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DonationServiceTest {

    @Mock
    private DonationRepository repository;

    @InjectMocks
    private DonationService service;

    @Test
    public void findAllDonationStories() {
        DonationStory story1 = DonationStory.builder()
                .storyTitle("제목1")
                .storyWriter("작가1")
                .readCount(10)
                .writeTime(LocalDateTime.now())
                .build();

        DonationStory story2 = DonationStory.builder()
                .storyTitle("제목2")
                .storyWriter("작가2")
                .readCount(10)
                .writeTime(LocalDateTime.now())
                .build();

        List<DonationStory> donationStories = Arrays.asList(story1, story2);

        Pageable pageable = PageRequest.of(0, 20);
        Page<DonationStory> page = new PageImpl<>(donationStories, pageable, 2);

        when(repository.findAllDonationStories(pageable)).thenReturn(page);

        Page<DonationStoryListDto> list = service.findAllDonationStories(pageable);

        assertEquals(2, list.getContent().size());

    }

    @Test
    public void loadDonationStoryFormData() {
        DonationStoryWriteFormDto dto = service.loadDonationStoryFormData();

        assertEquals(AreaCode.AREA100, dto.getAreaOptions().get(0));
        assertEquals(3, dto.getAreaOptions().size());
        assertEquals(Arrays.asList(AreaCode.AREA100, AreaCode.AREA200, AreaCode.AREA300),
                dto.getAreaOptions());
    }

    @Test
    public void createDonationStory() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "hello, world".getBytes()
        );

        DonationStoryCreateRequestDto requestDto = new DonationStoryCreateRequestDto(
                AreaCode.AREA100, "제목1", "fekofqkfq", "작가1", "안녕하세요", "ㅇㅈㅈㅇㅂ", file);

        Path uploadPath = Paths.get("./target/test-uploads");
        Files.createDirectories(uploadPath);

        service.createDonationStory(requestDto);

        ArgumentCaptor<DonationStory> captor = ArgumentCaptor.forClass(DonationStory.class);
        verify(repository, times(1)).save(captor.capture());

        DonationStory saved = captor.getValue();

        assertEquals(requestDto.getAreaCode(), saved.getAreaCode());
        assertEquals("안녕하세요", saved.getStoryContents());
        assertEquals(requestDto.getStoryPasscode(), saved.getStoryPasscode());
    }

    @Test
    public void findDonationStory_success() {
        Long storySeq = 1L;
        DonationStory story = DonationStory.builder()
                .storySeq(storySeq)
                .storyTitle("제목1")
                .storyWriter("작성자1")
                .writeTime(LocalDateTime.now())
                .areaCode(AreaCode.AREA100)
                .readCount(10)
                .storyContents("내용입니다")
                .fileName("abc123.jpg")
                .orgFileName("원본.jpg")
                .comments(new ArrayList<>())
                .build();

        when(repository.findWithCommentsById(storySeq)).thenReturn(Optional.of(story));

        DonationStoryDetailDto detailDto = service.findDonationStory(storySeq);

        assertEquals(storySeq, detailDto.getStorySeq());
        assertEquals("제목1", detailDto.getTitle());
        assertEquals("작성자1", detailDto.getStoryWriter());
        assertEquals("AREA100", detailDto.getAreaCode().toString());
        assertNotNull(detailDto);
    }

    @Test
    public void modifyDonationStory() {
        Long storySeq = 1L;
        DonationStory story = DonationStory.builder()
                .storySeq(storySeq)
                .storyTitle("제목1")
                .storyWriter("작성자1")
                .writeTime(LocalDateTime.now())
                .areaCode(AreaCode.AREA100)
                .readCount(10)
                .storyContents("내용입니다")
                .fileName("abc123.jpg")
                .orgFileName("원본.jpg")
                .comments(new ArrayList<>())
                .build();

        when(repository.findById(storySeq)).thenReturn(Optional.of(story));

        DonationStoryModifyRequestDto modifyDto = DonationStoryModifyRequestDto.builder()
                .areaCode(AreaCode.AREA200)
                .storyTitle("제목제목1")
                .build();

        service.modifyDonationStory(storySeq, modifyDto);

        assertEquals("AREA200", story.getAreaCode().toString());
        assertEquals("제목제목1", story.getStoryTitle());
    }

    @Test
    public void deleteDonationStory() {
        Long storySeq = 1L;
        DonationStory story = DonationStory.builder()
                .storySeq(storySeq)
                .storyTitle("제목1")
                .storyPasscode("12345678")
                .storyWriter("작성자1")
                .writeTime(LocalDateTime.now())
                .areaCode(AreaCode.AREA100)
                .readCount(10)
                .storyContents("내용입니다")
                .fileName("abc123.jpg")
                .orgFileName("원본.jpg")
                .comments(new ArrayList<>())
                .build();

        VerifyStoryPasscodeDto passcodeDto = new VerifyStoryPasscodeDto("12345678");
        when(repository.findById(storySeq)).thenReturn(Optional.of(story));

        service.deleteDonationStory(storySeq, passcodeDto);

        assertNotNull(story.getStoryPasscode());
        assertEquals(Integer.valueOf(10), story.getReadCount());

        ArgumentCaptor<DonationStory> captor = ArgumentCaptor.forClass(DonationStory.class);
        verify(repository).delete(captor.capture());
        assertEquals(storySeq, captor.getValue().getStorySeq());
    }
}