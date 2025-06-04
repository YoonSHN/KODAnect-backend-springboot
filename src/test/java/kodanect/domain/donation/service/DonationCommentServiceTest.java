package kodanect.domain.donation.service;


import kodanect.domain.donation.dto.request.DonationCommentCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryCommentModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyCommentPasscodeDto;
import kodanect.domain.donation.dto.response.AreaCode;
import kodanect.domain.donation.entity.DonationStory;
import kodanect.domain.donation.entity.DonationStoryComment;
import kodanect.domain.donation.exception.BadRequestException;
import kodanect.domain.donation.repository.DonationCommentRepository;
import kodanect.domain.donation.repository.DonationRepository;
import kodanect.domain.donation.service.impl.DonationCommentServiceImpl;
import kodanect.common.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DonationCommentServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private MessageResolver messageResolver;

    @Mock
    private DonationCommentRepository commentRepository;

    @InjectMocks
    private DonationCommentServiceImpl service;

    @Test
    public void 댓글_등록_성공() {
        Long storySeq = 1L;
        DonationStory story = dummyStory(storySeq);

        DonationCommentCreateRequestDto requestDto = DonationCommentCreateRequestDto.builder()
                .commentWriter("홍길동")
                .commentPasscode("abcd1234")
                .contents("좋은 글 감사합니다.")
                .captchaToken("dummy")
                .build();

        when(donationRepository.findById(storySeq)).thenReturn(Optional.of(story));

        service.createDonationStoryComment(storySeq, requestDto);

        assertEquals(1, story.getComments().size());
        DonationStoryComment saved = story.getComments().get(0);
        assertEquals("홍길동", saved.getCommentWriter());
        assertEquals("abcd1234", saved.getCommentPasscode());

        verify(commentRepository).save(saved);
    }

    @Test(expected = BadRequestException.class)
    public void 댓글_등록_실패_작성자없음() {
        Long storySeq = 1L;
        DonationStory story = dummyStory(storySeq);

        DonationCommentCreateRequestDto requestDto = DonationCommentCreateRequestDto.builder()
                .commentWriter("")  // 빈 값
                .commentPasscode("abcd1234")
                .contents("내용")
                .captchaToken("dummy")
                .build();

        when(donationRepository.findById(storySeq)).thenReturn(Optional.of(story));

        service.createDonationStoryComment(storySeq, requestDto);
    }

    @Test
    public void 댓글_수정_성공() {
        Long commentSeq = 1L;

        DonationStoryComment comment = DonationStoryComment.builder()
                .commentSeq(commentSeq)
                .commentWriter("기존 작성자")
                .commentPasscode("abcd1234")
                .contents("기존 내용")
                .build();

        DonationStoryCommentModifyRequestDto requestDto = DonationStoryCommentModifyRequestDto.builder()
                .commentWriter("수정된 작성자")
                .commentContents("수정된 내용")
                .commentPasscode("abcd1234")
                .captchaToken("dummy")
                .build();

        when(commentRepository.findById(commentSeq)).thenReturn(Optional.of(comment));

        service.modifyDonationComment(commentSeq, requestDto);

        assertEquals("수정된 작성자", comment.getCommentWriter());
        assertEquals("수정된 내용", comment.getContents());
    }

    @Test
    public void 댓글_삭제_성공() {
        Long storySeq = 1L;
        Long commentSeq = 1L;

        DonationStory story = dummyStory(storySeq);
        DonationStoryComment comment = DonationStoryComment.builder()
                .commentSeq(commentSeq)
                .commentWriter("작성자")
                .commentPasscode("abcd1234")
                .contents("댓글 내용")
                .build();

        story.addComment(comment);
        comment.setStory(story); // 양방향 관계 설정

        VerifyCommentPasscodeDto dto = new VerifyCommentPasscodeDto("abcd1234");

        when(commentRepository.findById(commentSeq)).thenReturn(Optional.of(comment));

        service.deleteDonationComment(commentSeq, dto);

        assertEquals(0, story.getComments().size());

        ArgumentCaptor<DonationStoryComment> captor = ArgumentCaptor.forClass(DonationStoryComment.class);
        verify(commentRepository).delete(captor.capture());
        assertEquals(commentSeq, captor.getValue().getCommentSeq());
    }

    // ===== 공통 더미 스토리 =====
    private DonationStory dummyStory(Long storySeq) {
        return DonationStory.builder()
                .storySeq(storySeq)
                .storyTitle("제목1")
                .storyWriter("작성자1")
                .storyContents("내용입니다")
                .areaCode(AreaCode.AREA100)
                .writeTime(LocalDateTime.now())
                .readCount(0)
                .comments(new ArrayList<>())
                .build();
    }
}