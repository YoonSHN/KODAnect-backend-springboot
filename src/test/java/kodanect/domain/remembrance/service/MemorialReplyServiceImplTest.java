package kodanect.domain.remembrance.service;

import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.common.util.MemorialFinder;
import kodanect.common.util.MemorialReplyFinder;
import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.dto.MemorialReplyDeleteRequest;
import kodanect.domain.remembrance.dto.MemorialReplyResponse;
import kodanect.domain.remembrance.dto.MemorialReplyUpdateRequest;
import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.entity.MemorialReply;
import kodanect.domain.remembrance.repository.MemorialReplyRepository;
import kodanect.domain.remembrance.service.impl.MemorialReplyServiceImpl;
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
public class MemorialReplyServiceImplTest {

    @InjectMocks
    private MemorialReplyServiceImpl memorialReplyService;

    @Mock
    private MemorialReplyRepository memorialReplyRepository;

    @Mock
    private MemorialFinder memorialFinder;

    @Mock
    private MemorialReplyFinder memorialReplyFinder;


    @Test
    @DisplayName("추모관 댓글 생성")
    public void 추모관_댓글_생성() {

        Integer donateSeq = 1;

        MemorialReplyCreateRequest request =
                MemorialReplyCreateRequest.builder()
                        .donateSeq(donateSeq)
                        .replyWriter("홍길동")
                        .replyPassword("1234")
                        .replyContents("내용")
                        .build();

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(mock(Memorial.class));
        when(memorialReplyRepository.save(any(MemorialReply.class))).thenReturn(null);

        memorialReplyService.createReply(donateSeq, request);

        verify(memorialReplyRepository, times(1)).save(any(MemorialReply.class));
    }

    @Test
    @DisplayName("추모관 댓글 수정")
    public void 추모관_댓글_수정() {

        Integer donateSeq = 1;
        Integer replySeq = 1;

        MemorialReplyUpdateRequest request =
                MemorialReplyUpdateRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(replySeq)
                        .replyContents("수정 내용")
                        .replyPassword("1234")
                        .build();

        MemorialReply mockReply =
                MemorialReply
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(replySeq)
                        .replyContents("안바뀐 내용")
                        .replyPassword("1234")
                        .build();

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(mock(Memorial.class));
        when(memorialReplyFinder.findByIdOrThrow(replySeq)).thenReturn(mockReply);

        memorialReplyService.updateReply(donateSeq, replySeq, request);

        verify(memorialReplyRepository, times(1)).updateReplyContents(eq(replySeq), eq("수정 내용"));
    }

    @Test
    @DisplayName("추모관 댓글 삭제")
    public void 추모관_댓글_삭제() {

        Integer donateSeq = 1;
        Integer replySeq = 1;

        MemorialReplyDeleteRequest request =
                MemorialReplyDeleteRequest
                        .builder()
                        .donateSeq(donateSeq)
                        .replySeq(replySeq)
                        .replyPassword("1234")
                        .build();

        MemorialReply mockReply = MemorialReply.builder()
                .replySeq(replySeq)
                .donateSeq(donateSeq)
                .replyPassword("1234")
                .delFlag("N")
                .build();

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(mock(Memorial.class));
        when(memorialReplyFinder.findByIdOrThrow(replySeq)).thenReturn(mockReply);
        when(memorialReplyRepository.save(any(MemorialReply.class))).thenReturn(null);

        memorialReplyService.deleteReply(donateSeq, replySeq, request);

        assertEquals("Y", mockReply.getDelFlag());
        verify(memorialReplyRepository, times(1)).save(mockReply);
    }

    @Test
    @DisplayName("추모관 댓글 조회")
    public void 추모관_댓글_조회() {

        Integer donateSeq = 1;
        Integer cursor = 1;
        int size = 3;

        MemorialReplyResponse reply = MemorialReplyResponse
                .builder()
                .replySeq(1)
                .replyWriter("작성자")
                .replyContents("내용입니다")
                .replyWriteTime(LocalDateTime.of(2024, 6, 1, 10, 0))
                .build();

        List<MemorialReplyResponse> content = List.of(reply);

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(mock(Memorial.class));
        when(memorialReplyRepository.findByCursor(eq(donateSeq), eq(cursor), any(Pageable.class))).thenReturn(content);

        List<MemorialReplyResponse> page = memorialReplyService.getMemorialReplyList(donateSeq, cursor, size);

        assertNotNull(page);
        assertEquals(1, page.size());
        assertEquals(1, page.get(0).getReplySeq());
        assertEquals("작성자", page.get(0).getReplyWriter());
        assertEquals("내용입니다",  page.get(0).getReplyContents());
        assertEquals(LocalDateTime.of(2024,6,1,10,0), page.get(0).getReplyWriteTime());

        verify(memorialReplyRepository, times(1)).findByCursor(eq(donateSeq), eq(cursor), any(Pageable.class));
    }

    @Test
    @DisplayName("추모관 댓글 더보기")
    public void 추모관_댓글_더보기() {

        Integer donateSeq = 1;
        Integer cursor = 1;
        int size = 3;

        MemorialReplyResponse reply = MemorialReplyResponse.builder()
                .replySeq(1)
                .replyWriter("작성자")
                .replyContents("내용입니다")
                .replyWriteTime(LocalDateTime.of(2024, 6, 1, 10, 0))
                .build();

        List<MemorialReplyResponse> content = List.of(reply);

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(mock(Memorial.class));
        when(memorialReplyRepository.findByCursor(eq(donateSeq), eq(cursor), any(Pageable.class))).thenReturn(content);

        CursorReplyPaginationResponse<MemorialReplyResponse> page =
                memorialReplyService.getMoreReplyList(donateSeq, cursor, size);

        assertNotNull(page);
        assertEquals(1, content.size());
        assertEquals(1, content.get(0).getReplySeq());
        assertEquals("작성자", content.get(0).getReplyWriter());
        assertEquals("내용입니다",  content.get(0).getReplyContents());
        assertEquals(LocalDateTime.of(2024,6,1,10,0), content.get(0).getReplyWriteTime());

        verify(memorialReplyRepository, times(1)).findByCursor(eq(donateSeq), eq(cursor), any(Pageable.class));
    }
}