package kodanect.domain.donation.service;

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
import kodanect.domain.donation.service.impl.DonationCommentServiceImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DonationCommentServiceImplTest {

    @Mock
    private DonationCommentRepository commentRepository;

    @Mock
    private DonationRepository storyRepository;

    @Mock
    private MessageResolver messageResolver;

    @InjectMocks
    private DonationCommentServiceImpl service;


    @Test
    public void findCommentsWithCursor_정상_조회() {
        Long storySeq = 1L;
        Long cursor = null;
        int size = 2;
        Pageable pageable = PageRequest.of(0, size + 1);

        DonationStory story = DonationStory.builder()
                .storySeq(storySeq)
                .build();

        DonationStoryComment comment1 = DonationStoryComment.builder().commentSeq(10L).commentWriter("작성자1")
                .commentPasscode("pass1234").contents("댓글1").writeTime(LocalDateTime.now()).delFlag("N")
                .build();

        DonationStoryComment comment2 = DonationStoryComment.builder().commentSeq(9L).commentWriter("작성자2")
                .commentPasscode("pass1234").contents("댓글2").writeTime(LocalDateTime.now().minusMinutes(1)).delFlag("N")
                .build();

        DonationStoryComment comment3 = DonationStoryComment.builder().commentSeq(8L).commentWriter("작성자3").commentPasscode("pass1234")
                .contents("댓글3").writeTime(LocalDateTime.now().minusMinutes(2)).delFlag("N")
                .build();
        List<DonationStoryComment> entities = List.of(comment1, comment2, comment3);
        given(commentRepository.findByCursorEntity(storySeq, cursor, pageable)).willReturn(entities);
        given(commentRepository.countAllByStorySeq(storySeq)).willReturn(3L);

        CursorPaginationResponse<DonationStoryCommentDto, Long> response =
                service.findCommentsWithCursor(storySeq, cursor, size);

        assertThat(response.getContent()).hasSize(size);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getNextCursor()).isEqualTo(comment2.getCommentSeq());
        assertThat(response.getContent().get(0).getCursorId()).isEqualTo(10L);
    }

    @Test
    public void createDonationStoryComment_정상_등록() {
        Long storySeq = 1L;
        DonationStory story = DonationStory.builder()
                .storySeq(storySeq)
                .comments(new ArrayList<>())
                .build();
        given(storyRepository.findById(storySeq)).willReturn(Optional.of(story));

        DonationCommentCreateRequestDto dto = DonationCommentCreateRequestDto.builder()
                .commentWriter("홍길동")
                .commentPasscode("Abcd1234")
                .contents("댓글 내용")
                .build();

        service.createDonationStoryComment(storySeq, dto);

        assertThat(story.getComments()).hasSize(1);
        DonationStoryComment saved = story.getComments().get(0);
        assertThat(saved.getCommentWriter()).isEqualTo("홍길동");
        assertThat(saved.getCommentPasscode()).isEqualTo("Abcd1234");
        verify(commentRepository).save(saved);
    }

    @Test(expected = DonationNotFoundException.class)
    public void createDonationStoryComment_스토리_없음_예외() {
        given(storyRepository.findById(anyLong())).willReturn(Optional.empty());
        service.createDonationStoryComment(1L, new DonationCommentCreateRequestDto("writer","pass","contents"));
    }

    @Test(expected = BadRequestException.class)
    public void createDonationStoryComment_작성자_누락_예외() {
        Long storySeq = 1L;
        DonationStory story = DonationStory.builder().storySeq(storySeq).build();
        given(storyRepository.findById(storySeq)).willReturn(Optional.of(story));

        DonationCommentCreateRequestDto dto = DonationCommentCreateRequestDto.builder()
                .commentWriter("")
                .commentPasscode("Abcd1234")
                .contents("내용")
                .build();
        service.createDonationStoryComment(storySeq, dto);
    }

    @Test(expected = PasscodeMismatchException.class)
    public void createDonationStoryComment_패스코드_누락_예외() {
        Long storySeq = 1L;
        DonationStory story = DonationStory.builder().storySeq(storySeq).build();
        given(storyRepository.findById(storySeq)).willReturn(Optional.of(story));

        DonationCommentCreateRequestDto dto = DonationCommentCreateRequestDto.builder()
                .commentWriter("writer")
                .commentPasscode("")
                .contents("내용")
                .build();
        service.createDonationStoryComment(storySeq, dto);
    }

    @Test(expected = BadRequestException.class)
    public void createDonationStoryComment_패스코드_형식오류_예외() {
        Long storySeq = 1L;
        DonationStory story = DonationStory.builder().storySeq(storySeq).build();
        given(storyRepository.findById(storySeq)).willReturn(Optional.of(story));

        DonationCommentCreateRequestDto dto = DonationCommentCreateRequestDto.builder()
                .commentWriter("writer")
                .commentPasscode("1234")
                .contents("내용")
                .build();
        given(messageResolver.get("donation.error.invalid.passcode.format")).willReturn("Invalid");

        service.createDonationStoryComment(storySeq, dto);
    }
    @Test
    public void verifyPasswordWithPassword_정상_인증성공() {
        Long storySeq = 1L;
        Long commentSeq = 2L;
        String validPassword = "Abcd1234!";

        DonationStory story = DonationStory.builder()
                .storySeq(storySeq)
                .storyPasscode(validPassword)
                .build();

        DonationStoryComment comment = DonationStoryComment.builder()
                .commentSeq(commentSeq)
                .story(story)
                .commentPasscode(validPassword)
                .build();

        given(storyRepository.findById(storySeq)).willReturn(Optional.of(story));
        given(commentRepository.findById(commentSeq)).willReturn(Optional.of(comment));

        VerifyCommentPasscodeDto dto = new VerifyCommentPasscodeDto(validPassword);

        service.verifyPasswordWithPassword(storySeq, commentSeq, dto);
        // 예외 없이 통과하면 성공
    }

    // --- 스토리 없음 ---
    @Test(expected = DonationNotFoundException.class)
    public void verifyPasswordWithPassword_스토리없음_예외() {
        Long storySeq = 1L;
        given(storyRepository.findById(storySeq)).willReturn(Optional.empty());

        service.verifyPasswordWithPassword(storySeq, 2L,
                new VerifyCommentPasscodeDto("Abcd1234"));
    }

    // --- 댓글 없음 ---
    @Test(expected = DonationCommentNotFoundException.class)
    public void verifyPasswordWithPassword_댓글없음_예외() {
        Long storySeq = 1L;
        Long commentSeq = 2L;

        given(storyRepository.findById(storySeq)).willReturn(Optional.of(DonationStory.builder()
                .storySeq(storySeq).storyPasscode("Abcd1234").build()));
        given(commentRepository.findById(commentSeq)).willReturn(Optional.empty());

        service.verifyPasswordWithPassword(storySeq, commentSeq,
                new VerifyCommentPasscodeDto("Abcd1234"));
    }

    // --- 비밀번호 형식 오류 ---
    @Test(expected = BadRequestException.class)
    public void verifyPasswordWithPassword_비밀번호형식_예외() {
        Long storySeq = 1L;
        Long commentSeq = 2L;

        DonationStory mockStory = DonationStory.builder()
                .storySeq(storySeq)
                .storyPasscode("Abcd1234")
                .build();

        DonationStoryComment comment = DonationStoryComment.builder()
                .commentSeq(commentSeq)
                .story(mockStory)
                .commentPasscode("Wrong")  // 또는 실제 비밀번호
                .build();

        given(storyRepository.findById(storySeq)).willReturn(Optional.of(mockStory));
        given(commentRepository.findById(commentSeq)).willReturn(Optional.of(comment));
        given(messageResolver.get("donation.error.invalid.passcode.format"))
                .willReturn("비밀번호 형식 오류");

        service.verifyPasswordWithPassword(storySeq, commentSeq,
                new VerifyCommentPasscodeDto("bad"));  // 8자 미만 또는 숫자/문자 조합 아님
    }

    // --- 비밀번호 불일치 ---
    @Test(expected = PasscodeMismatchException.class)
    public void verifyPasswordWithPassword_비밀번호불일치_예외() {
        Long storySeq = 1L;
        Long commentSeq = 2L;

        DonationStory story = DonationStory.builder()
                .storySeq(storySeq)
                .storyPasscode("Abcd1234")
                .build();

        DonationStoryComment comment = DonationStoryComment.builder()
                .commentSeq(commentSeq)
                .story(story)
                .commentPasscode("Correct123") // ✅ 실제 저장된 비밀번호
                .build();

        given(storyRepository.findById(storySeq)).willReturn(Optional.of(story));
        given(commentRepository.findById(commentSeq)).willReturn(Optional.of(comment));

        // 입력 비밀번호를 틀리게 줌
        service.verifyPasswordWithPassword(storySeq, commentSeq,
                new VerifyCommentPasscodeDto("Wrong123"));
    }

    @Test
    public void updateDonationComment_정상_수정() {
        Long storySeq = 1L;
        Long commentSeq = 2L;
        DonationStory story = DonationStory.builder().storySeq(storySeq).build();
        DonationStoryComment comment = DonationStoryComment.builder()
                .commentSeq(commentSeq)
                .commentWriter("old")
                .commentPasscode("Abcd1234")
                .contents("old content")
                .story(story)
                .build();
        when(storyRepository.findById(storySeq)).thenReturn(Optional.of(story));
        when(commentRepository.findById(commentSeq)).thenReturn(Optional.of(comment));

        DonationStoryCommentModifyRequestDto dto = DonationStoryCommentModifyRequestDto.builder()
                .commentWriter("new")
                .commentContents("new content")
                .build();

        service.updateDonationComment(storySeq, commentSeq, dto);

        assertThat(comment.getCommentWriter()).isEqualTo("new");
        assertThat(comment.getContents()).isEqualTo("new content");
    }

    @Test(expected = DonationNotFoundException.class)
    public void updateDonationComment_스토리_없음_예외() {
        when(storyRepository.findById(anyLong())).thenReturn(Optional.empty());
        service.updateDonationComment(1L, 2L, new DonationStoryCommentModifyRequestDto("w","c"));
    }

    @Test(expected = DonationCommentNotFoundException.class)
    public void updateDonationComment_댓글_없음_예외() {
        Long storySeq = 1L;
        when(storyRepository.findById(storySeq)).thenReturn(Optional.of(DonationStory.builder().storySeq(storySeq).build()));
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());
        service.updateDonationComment(storySeq, 99L, new DonationStoryCommentModifyRequestDto("w","c"));
    }

    @Test(expected = BadRequestException.class)
    public void updateDonationComment_작성자_누락_예외() {
        Long storySeq = 1L, commentSeq = 2L;
        DonationStory story = DonationStory.builder().storySeq(storySeq).build();
        DonationStoryComment comment = DonationStoryComment.builder().commentSeq(commentSeq).build();
        when(storyRepository.findById(storySeq)).thenReturn(Optional.of(story));
        when(commentRepository.findById(commentSeq)).thenReturn(Optional.of(comment));

        service.updateDonationComment(storySeq, commentSeq,
                DonationStoryCommentModifyRequestDto.builder()
                        .commentWriter("")
                        .commentContents("c")
                        .build());
    }

    @Test
    public void deleteDonationComment_정상_삭제() {
        Long storySeq = 1L, commentSeq = 2L;
        DonationStory story = DonationStory.builder()
                .storySeq(storySeq)
                .comments(new ArrayList<>())
                .build();
        DonationStoryComment comment = DonationStoryComment.builder()
                .commentSeq(commentSeq)
                .commentPasscode("Abcd1234")
                .build();
        story.addComment(comment);
        comment.setStory(story);
        when(storyRepository.findById(storySeq)).thenReturn(Optional.of(story));
        when(commentRepository.findById(commentSeq)).thenReturn(Optional.of(comment));

        service.deleteDonationComment(storySeq, commentSeq,
                new VerifyCommentPasscodeDto("Abcd1234"));

        assertThat(story.getComments()).isEmpty();
        ArgumentCaptor<DonationStoryComment> captor = ArgumentCaptor.forClass(DonationStoryComment.class);
        verify(commentRepository).delete(captor.capture());
        assertThat(captor.getValue().getCommentSeq()).isEqualTo(commentSeq);
    }

    @Test(expected = DonationNotFoundException.class)
    public void deleteDonationComment_스토리_없음_예외() {
        when(storyRepository.findById(anyLong())).thenReturn(Optional.empty());
        service.deleteDonationComment(1L, 2L, new VerifyCommentPasscodeDto("p"));
    }

    @Test(expected = NotFoundException.class)
    public void deleteDonationComment_댓글_없음_예외() {
        DonationStory story = DonationStory.builder().storySeq(1L).build();
        when(storyRepository.findById(1L)).thenReturn(Optional.of(story));
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());
        service.deleteDonationComment(1L, 2L, new VerifyCommentPasscodeDto("p"));
    }

    @Test(expected = PasscodeMismatchException.class)
    public void deleteDonationComment_패스코드_불일치_예외() {
        Long storySeq = 1L, commentSeq = 2L;
        DonationStory story = DonationStory.builder()
                .storySeq(storySeq)
                .comments(new ArrayList<>())
                .build();
        DonationStoryComment comment = DonationStoryComment.builder()
                .commentSeq(commentSeq)
                .commentPasscode("Abcd1234")
                .build();
        when(storyRepository.findById(storySeq)).thenReturn(Optional.of(story));
        when(commentRepository.findById(commentSeq)).thenReturn(Optional.of(comment));
        when(messageResolver.get("donation.error.delete.password_mismatch")).thenReturn("Mismatch");

        service.deleteDonationComment(storySeq, commentSeq, new VerifyCommentPasscodeDto("WrongPass"));
    }
}
