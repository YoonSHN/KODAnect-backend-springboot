package kodanect.domain.donation.service;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.util.MessageResolver;
import kodanect.domain.donation.dto.request.DonationStoryCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyStoryPasscodeDto;
import kodanect.domain.donation.dto.response.*;
import kodanect.domain.donation.entity.DonationStory;
import kodanect.domain.donation.exception.BadRequestException;
import kodanect.domain.donation.exception.DonationNotFoundException;
import kodanect.domain.donation.exception.PasscodeMismatchException;
import kodanect.domain.donation.repository.DonationCommentRepository;
import kodanect.domain.donation.repository.DonationRepository;
import kodanect.domain.donation.service.impl.DonationServiceImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;


@RunWith(MockitoJUnitRunner.class)
public class DonationServiceImplTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private DonationCommentRepository commentRepository;

    @Mock
    private MessageResolver messageResolver;

    @InjectMocks
    private DonationServiceImpl donationService;


    // --- Cursor Paging 기본 조회 ---
    @Test
    public void findStoriesWithCursor_ShouldReturnPageResult() {
        Long cursor = null;
        int size = 2;

        Pageable pg = PageRequest.of(0, size + 1);
        List<DonationStoryListDto> dtoList = List.of(
                new DonationStoryListDto(1L, "A", "W", 0, (LocalDateTime) null),
                new DonationStoryListDto(2L, "B", "X", 0, (LocalDateTime) null),
                new DonationStoryListDto(3L, "C", "Y", 0, (LocalDateTime) null)
        );

        given(donationRepository.findByCursor(cursor, pg)).willReturn(dtoList);
        given(donationRepository.countAll()).willReturn(5L);

        CursorPaginationResponse<DonationStoryListDto, Long> resp = donationService.findStoriesWithCursor(cursor, size);

        assertThat(resp.getContent()).hasSize(size);
        assertThat(resp.isHasNext()).isTrue();
        assertThat(resp.getNextCursor()).isEqualTo(dtoList.get(size - 1).getCursorId());
    }

    // --- Cursor Paging 검색(title, contents, default) ---
    @Test
    public void findSearchStoriesWithCursor_ByTitle() {
        List<DonationStoryListDto> list = List.of(
                new DonationStoryListDto(10L, "T", "U", 0, (LocalDateTime) null),
                new DonationStoryListDto(20L, "T2","V", 0, (LocalDateTime) null),
                new DonationStoryListDto(30L, "T3","W", 0, (LocalDateTime) null),
                new DonationStoryListDto(40L, "T4","X", 0, (LocalDateTime) null)
        );
        given(donationRepository.findByTitleCursor(eq("foo"), eq(null), any(Pageable.class))).willReturn(list);
        given(donationRepository.countByTitle("foo")).willReturn(10L);

        var resp = donationService.findSearchStoriesWithCursor("title", "foo", null, 3);
        assertThat(resp.getContent()).hasSize(3);
        assertThat(resp.isHasNext()).isTrue();
    }

    @Test
    public void findSearchStoriesWithCursor_ByContents() {

        given(donationRepository.countByContents("bar")).willReturn(0L);

        var resp = donationService.findSearchStoriesWithCursor("contents", "bar", 5L, 4);
        assertThat(resp.getContent()).isEmpty();
        assertThat(resp.isHasNext()).isFalse();
    }

    @Test
    public void findSearchStoriesWithCursor_DefaultCase() {
        given(donationRepository.countByTitleAndContents("kw")).willReturn(0L);

        var resp = donationService.findSearchStoriesWithCursor("other", "kw", null, 2);
        assertThat(resp.getContent()).isEmpty();
    }

    // --- 스토리 생성 ---
    @Test
    public void createDonationStory_ValidDto_ShouldSave() {
        DonationStoryCreateRequestDto requestDto = DonationStoryCreateRequestDto.builder()
                .areaCode(AreaCode.AREA100) // 올바른 enum 또는 문자열
                .storyTitle("테스트 제목")
                .storyWriter("작성자")
                .storyPasscode("pass1234")  // 영문+숫자 8~16자
                .storyContents("<p>내용</p>") // 이건 필수는 아님
                .build();


        donationService.createDonationStory(requestDto);
        then(donationRepository).should().save(any(DonationStory.class));
    }

    @Test(expected = BadRequestException.class)
    public void createDonationStory_InvalidPasscode_ShouldThrow() {
        var dto = new DonationStoryCreateRequestDto(
                kodanect.domain.donation.dto.response.AreaCode.AREA100,
                "T", "short", "W", "C"
        );
        given(messageResolver.get("donation.error.invalid.passcode.format")).willReturn("Bad");
        donationService.createDonationStory(dto);
    }

    // --- private imgParsing 검증 via Reflection ---
    @Test
    public void imgParsing_ShouldExtractFilenames() {
        String html = "<div><img src=\"/path/orig.jpg\"/></div>"
                + "<div><img src=\"/another/a.png\"/></div>";
        String[] result = (String[]) ReflectionTestUtils.invokeMethod(
                donationService, "imgParsing", html
        );
        assertThat(result).hasSize(2);
        // orgFileNames
        assertThat(result[0]).contains("orig.jpg").contains("a.png");
        // fileNames: 대문자 알파벳+숫자 (UUID 형태)
        String[] uuids = result[1].split(",");
        for (String u : uuids) {
            assertThat(u).matches("^[A-Z0-9]{32}$");
        }
    }

    // --- UUID 포맷 검증 ---
    @Test
    public void makeStoredFileName_ShouldBe32CharUpperHex() {
        String fname = donationService.makeStoredFileName();
        assertThat(fname).hasSize(32);
        assertThat(Pattern.matches("^[A-Z0-9]{32}$", fname)).isTrue();
    }

    // --- 비밀번호 검증 ---
    @Test(expected = DonationNotFoundException.class)
    public void verifyPassword_NoStory_ShouldThrowNotFound() {
        given(donationRepository.findById(1L)).willReturn(Optional.empty());
        donationService.verifyPasswordWithPassword(1L, new VerifyStoryPasscodeDto("Abcd1234"));
    }

    @Test(expected = BadRequestException.class)
    public void verifyPassword_InvalidFormat_ShouldThrowBadRequest() {
        var story = DonationStory.builder().storySeq(1L).storyPasscode("Pass1234").build();
        given(donationRepository.findById(1L)).willReturn(Optional.of(story));
        given(messageResolver.get("donation.error.invalid.passcode.format")).willReturn("fmtErr");
        donationService.verifyPasswordWithPassword(1L, new VerifyStoryPasscodeDto("bad"));
    }

    @Test(expected = BadRequestException.class)
    public void verifyPassword_Mismatch_ShouldThrowBadRequest() {
        var story = DonationStory.builder().storySeq(1L).storyPasscode("Abcd1234").build();
        given(donationRepository.findById(1L)).willReturn(Optional.of(story));
        donationService.verifyPasswordWithPassword(1L, new VerifyStoryPasscodeDto("Wrong"));
    }

    // --- 상세 조회 & 조회수 증가 ---
    @Test
    public void findDonationStoryWithStoryId_ShouldIncreaseReadCount() {
        DonationStory s = DonationStory.builder()
                .storySeq(5L).readCount(0).storyPasscode("X").storyTitle("T")
                .storyContents("C").areaCode(kodanect.domain.donation.dto.response.AreaCode.AREA100)
                .writeTime(LocalDateTime.now()).build();
        given(donationRepository.findStoryOnlyById(5L)).willReturn(Optional.of(s));

        var dto = donationService.findDonationStoryWithStoryId(5L);
        assertThat(dto.getStorySeq()).isEqualTo(5L);
        assertThat(s.getReadCount()).isEqualTo(1);
    }

    @Test
    public void findDonationStoryWithStoryId_최신댓글3개_반환() {
        // given
        DonationStory story = DonationStory.builder()
                .storySeq(1L).readCount(0).storyTitle("타이틀").writeTime(LocalDateTime.now())
                        .build();

        List<DonationStoryCommentDto> comments = List.of(
                new DonationStoryCommentDto(10L, "홍길동", "댓글1", LocalDateTime.now()),
                new DonationStoryCommentDto(9L, "김길동", "댓글2", LocalDateTime.now()),
                new DonationStoryCommentDto(8L, "박길동", "댓글3", LocalDateTime.now())
        );

        given(donationRepository.findStoryOnlyById(1L)).willReturn(Optional.of(story));
        given(commentRepository.findLatestComments(eq(1L), any(Pageable.class))).willReturn(comments);
        given(commentRepository.countAllByStorySeq(1L)).willReturn(10L);

        // when
        DonationStoryDetailDto dto = donationService.findDonationStoryWithStoryId(1L);

        // then
        assertThat(dto.getComments().getContent()).hasSize(3);
        assertThat(dto.getComments().getContent().get(0).getCommentSeq()).isEqualTo(10L);
    }

    @Test
    public void findDonationStoryWithStoryId_댓글3개미만_hasNextFalse() {
        // given
        DonationStory story = DonationStory.builder()
                .storySeq(2L).readCount(0).storyTitle("제목").writeTime(LocalDateTime.now()).build();

        List<DonationStoryCommentDto> fewComments = List.of(
                new DonationStoryCommentDto(10L, "유재석", "하하", LocalDateTime.now())
        );

        given(donationRepository.findStoryOnlyById(2L)).willReturn(Optional.of(story));
        given(commentRepository.findLatestComments(eq(2L), any(Pageable.class))).willReturn(fewComments);
        given(commentRepository.countAllByStorySeq(2L)).willReturn(1L);

        // when
        DonationStoryDetailDto dto = donationService.findDonationStoryWithStoryId(2L);

        // then
        assertThat(dto.getComments().getContent()).hasSize(1);
    }

    @Test(expected = DonationNotFoundException.class)
    public void findDonationStoryWithStoryId_NotFound_ShouldThrow() {
        given(donationRepository.findStoryOnlyById(99L)).willReturn(Optional.empty());
        donationService.findDonationStoryWithStoryId(99L);
    }

    // --- 수정 ---
    @Test
    public void updateDonationStory_Valid_ShouldModifyFields() {
        DonationStory s = DonationStory.builder()
                .storySeq(7L).storyTitle("Old").areaCode(kodanect.domain.donation.dto.response.AreaCode.AREA100)
                .build();
        given(donationRepository.findStoryOnlyById(7L)).willReturn(Optional.of(s));

        var dto = DonationStoryModifyRequestDto.builder()
                .storyTitle("New").areaCode(kodanect.domain.donation.dto.response.AreaCode.AREA200)
                .storyWriter("W").storyContents("<img src=\"/f.png\"/>Foo").build();

        donationService.updateDonationStory(7L, dto);
        assertThat(s.getStoryTitle()).isEqualTo("New");
        assertThat(s.getAreaCode()).isEqualTo(kodanect.domain.donation.dto.response.AreaCode.AREA200);
    }

    // --- 삭제 ---
    @Test
    public void deleteDonationStory_Valid_ShouldCallDelete() {
        DonationStory s = DonationStory.builder().storySeq(3L).storyPasscode("Abcd1234").build();
        given(donationRepository.findStoryOnlyById(3L)).willReturn(Optional.of(s));

        donationService.deleteDonationStory(3L, new VerifyStoryPasscodeDto("Abcd1234"));
        then(donationRepository).should().delete(s);
    }

    @Test(expected = PasscodeMismatchException.class)
    public void deleteDonationStory_Mismatch_ShouldThrow() {
        DonationStory s = DonationStory.builder().storySeq(3L).storyPasscode("Abcd1234").build();
        given(donationRepository.findStoryOnlyById(3L)).willReturn(Optional.of(s));
        given(messageResolver.get("donation.error.delete.password_mismatch"))
                .willReturn("nope");

        donationService.deleteDonationStory(3L, new VerifyStoryPasscodeDto("Wrong"));
    }
}