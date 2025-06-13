package kodanect.domain.remembrance.service;

import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.common.util.MemorialFinder;
import kodanect.common.util.MemorialCommentFinder;
import kodanect.domain.remembrance.dto.MemorialCommentCreateRequest;
import kodanect.domain.remembrance.dto.MemorialCommentPasswordRequest;
import kodanect.domain.remembrance.dto.MemorialCommentResponse;
import kodanect.domain.remembrance.dto.MemorialCommentUpdateRequest;
import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.entity.MemorialComment;
import kodanect.domain.remembrance.repository.MemorialCommentRepository;
import kodanect.domain.remembrance.service.impl.MemorialCommentServiceImpl;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MemorialCommentServiceImplTest {

    @InjectMocks
    private MemorialCommentServiceImpl memorialCommentService;

    @Mock
    private MemorialCommentRepository memorialCommentRepository;

    @Mock
    private MemorialFinder memorialFinder;

    @Mock
    private MemorialCommentFinder memorialCommentFinder;


    @Test
    @DisplayName("추모관 댓글 생성")
    public void 추모관_댓글_생성() {

        Integer donateSeq = 1;

        MemorialCommentCreateRequest request =
                MemorialCommentCreateRequest.builder()
                        .commentWriter("홍길동")
                        .commentPasscode("1234")
                        .contents("내용")
                        .build();

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(mock(Memorial.class));
        when(memorialCommentRepository.save(any(MemorialComment.class))).thenReturn(null);

        memorialCommentService.createComment(donateSeq, request);

        verify(memorialCommentRepository, times(1)).save(any(MemorialComment.class));
    }

    @Test
    @DisplayName("추모관 댓글 수정")
    public void 추모관_댓글_수정() {

        Integer donateSeq = 1;
        Integer commentSeq = 1;

        MemorialCommentUpdateRequest request =
                MemorialCommentUpdateRequest
                        .builder()
                        .commentWriter("홍길동")
                        .contents("수정 내용")
                        .build();

        MemorialComment mockComment =
                MemorialComment
                        .builder()
                        .donateSeq(donateSeq)
                        .commentSeq(commentSeq)
                        .commentWriter("홍길동")
                        .contents("안바뀐 내용")
                        .commentPasscode("1234")
                        .build();

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(mock(Memorial.class));
        when(memorialCommentFinder.findByIdOrThrow(commentSeq)).thenReturn(mockComment);

        memorialCommentService.updateComment(donateSeq, commentSeq, request);

        verify(memorialCommentRepository, times(1)).updateCommentContents(commentSeq,"수정 내용", "홍길동");
    }

    @Test
    @DisplayName("추모관 댓글 삭제")
    public void 추모관_댓글_삭제() {

        Integer donateSeq = 1;
        Integer commentSeq = 1;

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest
                        .builder()
                        .commentPasscode("1234")
                        .build();

        MemorialComment mockComment = MemorialComment.builder()
                .commentSeq(commentSeq)
                .donateSeq(donateSeq)
                .commentPasscode("1234")
                .delFlag("N")
                .build();

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(mock(Memorial.class));
        when(memorialCommentFinder.findByIdOrThrow(commentSeq)).thenReturn(mockComment);
        when(memorialCommentRepository.save(any(MemorialComment.class))).thenReturn(null);

        memorialCommentService.deleteComment(donateSeq, commentSeq, request);

        assertEquals("Y", mockComment.getDelFlag());
        verify(memorialCommentRepository, times(1)).save(mockComment);
    }

    @Test
    @DisplayName("추모관 비밀번호 인증")
    public void 추모관_비밀번호_인증() {

        Integer donateSeq = 1;
        Integer commentSeq = 1;

        MemorialCommentPasswordRequest request =
                MemorialCommentPasswordRequest.builder()
                        .commentPasscode("1234asdf")
                        .build();

        MemorialComment mockComment =
                MemorialComment
                        .builder()
                        .donateSeq(donateSeq)
                        .commentSeq(commentSeq)
                        .commentWriter("홍길동")
                        .contents("안바뀐 내용")
                        .commentPasscode("1234asdf")
                        .build();

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(mock(Memorial.class));
        when(memorialCommentFinder.findByIdOrThrow(commentSeq)).thenReturn(mockComment);

        memorialCommentService.varifyComment(donateSeq, commentSeq, request);
    }

    @Test
    @DisplayName("추모관 댓글 조회")
    public void 추모관_댓글_조회() {

        Integer donateSeq = 1;
        Integer cursor = 1;
        int size = 3;

        MemorialCommentResponse comment = MemorialCommentResponse
                .builder()
                .commentSeq(1)
                .commentWriter("작성자")
                .contents("내용입니다")
                .writeTime(LocalDateTime.of(2024, 6, 1, 10, 0))
                .build();

        List<MemorialCommentResponse> content = List.of(comment);

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(mock(Memorial.class));
        when(memorialCommentRepository.findByCursor(eq(donateSeq), eq(cursor), any(Pageable.class))).thenReturn(content);

        List<MemorialCommentResponse> page = memorialCommentService.getMemorialCommentList(donateSeq, cursor, size);

        assertNotNull(page);
        assertEquals(1, page.size());
        assertEquals(1, page.get(0).getCommentSeq());
        assertEquals("작성자", page.get(0).getCommentWriter());
        assertEquals("내용입니다",  page.get(0).getContents());
        assertEquals("2024-06-01", page.get(0).getWriteTime());

        verify(memorialCommentRepository, times(1)).findByCursor(eq(donateSeq), eq(cursor), any(Pageable.class));
    }

    @Test
    @DisplayName("추모관 댓글 더보기")
    public void 추모관_댓글_더보기() {

        Integer donateSeq = 1;
        Integer cursor = 1;
        int size = 3;

        MemorialCommentResponse comment = MemorialCommentResponse.builder()
                .commentSeq(1)
                .commentWriter("작성자")
                .contents("내용입니다")
                .writeTime(LocalDateTime.of(2024, 6, 1, 10, 0))
                .build();

        List<MemorialCommentResponse> content = List.of(comment);

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(mock(Memorial.class));
        when(memorialCommentRepository.findByCursor(eq(donateSeq), eq(cursor), any(Pageable.class))).thenReturn(content);

        CursorCommentPaginationResponse<MemorialCommentResponse, Integer> page =
                memorialCommentService.getMoreCommentList(donateSeq, cursor, size);

        assertNotNull(page);
        assertEquals(1, content.size());
        assertEquals(1, content.get(0).getCommentSeq());
        assertEquals("작성자", content.get(0).getCommentWriter());
        assertEquals("내용입니다",  content.get(0).getContents());
        assertEquals("2024-06-01", content.get(0).getWriteTime());

        verify(memorialCommentRepository, times(1)).findByCursor(eq(donateSeq), eq(cursor), any(Pageable.class));
    }
}