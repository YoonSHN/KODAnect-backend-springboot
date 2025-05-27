package kodanect.domain.donation.service;

import koda.dto.request.DonationCommentCreateRequestDto;
import koda.dto.request.DonationStoryCommentModifyRequestDto;
import koda.dto.request.VerifyCommentPasscodeDto;
import koda.dto.response.AreaCode;
import koda.entity.DonationStory;
import koda.entity.DonationStoryComment;
import koda.repository.DonationCommentRepository;
import koda.repository.DonationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DonationCommentServiceTest {

    @Mock
    private DonationRepository donationRepository;
    @Mock
    private DonationCommentRepository commentRepository;

    @InjectMocks
    private DonationCommentService service;

    @Test
    public void createDonationStoryComment() {
        Long storySeq = 1L;
        Long commentSeq = 100L;
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

        DonationCommentCreateRequestDto requestDto = DonationCommentCreateRequestDto.builder()
                .commentWriter("작성자111")
                .commentPasscode("assddasdd")
                .contents("dqwdqwdq")
                .captchaToken("sadkso").build();


        when(donationRepository.findById(storySeq)).thenReturn(Optional.of(story));

        service.createDonationStoryComment(storySeq, requestDto);

        assertNotNull(story.getComments().get(0));
        assertEquals("작성자111", story.getComments().get(0).getCommentWriter());
        assertEquals("assddasdd", story.getComments().get(0).getCommentPasscode());

    }

    @Test
    public void modifyDonationComment() {
        Long storySeq = 1L;
        Long commentSeq = 1L;

        DonationStoryComment comment = DonationStoryComment.builder()
                .commentSeq(commentSeq)
                .commentWriter("댓글 작성자1")
                .commentPasscode("eotrmfqlalf")
                .contents("qdokqwpdoqwkpdqwkdoq").build();

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
                .comments(new ArrayList<>(List.of(comment)))
                .build();

        DonationStoryCommentModifyRequestDto requestDto = DonationStoryCommentModifyRequestDto.builder()
                .commentWriter("ㅁasasdkasd")
                .commentContents("asdkoasdkqow")
                .commentPasscode("eotrmfqlalf")
                .captchaToken("wqdokqwpdqwd").build();

        when(commentRepository.findById(commentSeq)).thenReturn(Optional.of(comment));
        service.modifyDonationComment(commentSeq,requestDto);

        DonationStoryComment modifiedComment = story.getComments().get(0);
        assertEquals(story.getComments().getFirst().getCommentPasscode(), modifiedComment.getCommentPasscode());
        assertEquals(story.getComments().getFirst().getContents(), modifiedComment.getContents());
        assertEquals(story.getComments().getFirst().getCommentWriter(), modifiedComment.getCommentWriter());
    }

    @Test
    public void deleteDonationComment() {
        Long storySeq = 1L;
        Long commentSeq = 1L;
        DonationStoryComment comment = DonationStoryComment.builder()
                .commentSeq(commentSeq)
                .commentWriter("댓글 작성자1")
                .commentPasscode("eotrmfqlalf")
                .contents("qdokqwpdoqwkpdqwkdoq").build();

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

        story.addComment(comment);
        VerifyCommentPasscodeDto passcodeDto = VerifyCommentPasscodeDto.builder().commentPasscode("eotrmfqlalf").build();
        when(commentRepository.findById(commentSeq)).thenReturn(Optional.of(comment));

        service.deleteDonationComment(commentSeq, passcodeDto);

        assertEquals(0, story.getComments().size());

        ArgumentCaptor<DonationStoryComment> captor = ArgumentCaptor.forClass(DonationStoryComment.class);
        verify(commentRepository).delete(captor.capture());
        assertEquals(storySeq, captor.getValue().getCommentSeq());
    }
}