package kodanect.domain.heaven.service.impl;

import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.util.HeavenFinder;
import kodanect.domain.heaven.dto.HeavenDto;
import kodanect.domain.heaven.dto.response.HeavenCommentResponse;
import kodanect.domain.heaven.dto.response.HeavenDetailResponse;
import kodanect.domain.heaven.dto.response.HeavenResponse;
import kodanect.domain.heaven.repository.HeavenCommentRepository;
import kodanect.domain.heaven.repository.HeavenRepository;
import kodanect.domain.heaven.service.FileService;
import kodanect.domain.heaven.service.HeavenCommentService;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HeavenServiceImplTest {

    @InjectMocks
    private HeavenServiceImpl heavenServiceImpl;
    @Mock
    private HeavenRepository heavenRepository;
    @Mock
    private HeavenCommentRepository heavenCommentRepository;
    @Mock
    private HeavenCommentService heavenCommentService;
    @Mock
    private HeavenFinder heavenFinder;
    @Mock
    private FileService fileService;

    @Test
    @DisplayName("게시물 전체 조회 테스트")
    public void getHeavenListTest() {
        /* given */
        Integer cursor = 1000;
        int size = 20;
        Long heavenCount = 50L;

        String memorialAnonymityFlag = "N";
        String heavenAnonymityFlag = "N";
        int readCount = 5;
        LocalDateTime now = LocalDateTime.now();

        List<HeavenResponse> heavenResponseList = new ArrayList<>();

        for (int i = 1; i <= heavenCount; i++) {
            heavenResponseList.add(new HeavenResponse(i, "제목"+i, "기증자"+i, memorialAnonymityFlag, "작성자"+i, heavenAnonymityFlag, readCount, now));
        }

        when(heavenRepository.findByCursor(eq(cursor), any(Pageable.class))).thenReturn(heavenResponseList);
        when(heavenRepository.countByDelFlag()).thenReturn(heavenCount);

        /* when */
        CursorPaginationResponse<HeavenResponse, Integer> cursorPaginationResponse = heavenServiceImpl.getHeavenList(cursor, size);
        HeavenResponse firstHeavenResponse = cursorPaginationResponse.getContent().get(0);

        /* then */
        assertNotNull(cursorPaginationResponse);
        assertEquals(size, cursorPaginationResponse.getContent().size());
        assertTrue(cursorPaginationResponse.isHasNext());
        assertEquals(heavenCount, cursorPaginationResponse.getTotalCount());

        assertEquals(1, firstHeavenResponse.getLetterSeq());
        assertEquals("제목1", firstHeavenResponse.getLetterTitle());
        assertEquals("기증자1", firstHeavenResponse.getDonorName());
        assertEquals(memorialAnonymityFlag, firstHeavenResponse.getMemorialAnonymityFlag());
        assertEquals("작성자1", firstHeavenResponse.getLetterWriter());
        assertEquals(heavenAnonymityFlag, firstHeavenResponse.getHeavenAnonymityFlag());
        assertEquals(Integer.valueOf(readCount), firstHeavenResponse.getReadCount());
        assertEquals(now.toLocalDate().toString(), firstHeavenResponse.getWriteTime());
    }

    @Test
    @DisplayName("검색을 통한 게시물 전체 조회 테스트")
    public void getHeavenListSearchResultTest() {
        /* given */
        String type = "ALL";
        String keyWord = "제목";
        Integer cursor = 1000;
        int size = 20;
        Long heavenCount = 30L;

        String memorialAnonymityFlag = "N";
        String heavenAnonymityFlag = "N";
        int readCount = 13;
        LocalDateTime now = LocalDateTime.now();

        List<HeavenResponse> heavenResponseList = new ArrayList<>();

        for (int i = 1; i <= heavenCount; i++) {
            heavenResponseList.add(new HeavenResponse(i, "제목"+i, "기증자"+i, memorialAnonymityFlag, "작성자"+i, heavenAnonymityFlag, readCount, now));
        }

        when(heavenRepository.findByTitleOrContentsContaining(eq(keyWord), eq(cursor), any(Pageable.class))).thenReturn(heavenResponseList);
        when(heavenRepository.countByTitleOrContentsContaining(keyWord)).thenReturn(30L);

        /* when */
        CursorPaginationResponse<HeavenResponse, Integer> cursorPaginationResponse = heavenServiceImpl.getHeavenListSearchResult(type, keyWord, cursor, size);
        HeavenResponse firstHeavenResponse = cursorPaginationResponse.getContent().get(0);

        /* then */
        assertNotNull(cursorPaginationResponse);
        assertEquals(size, cursorPaginationResponse.getContent().size());
        assertTrue(cursorPaginationResponse.isHasNext());
        assertEquals(heavenCount, cursorPaginationResponse.getTotalCount());

        assertEquals(1, firstHeavenResponse.getLetterSeq());
        assertEquals("제목1", firstHeavenResponse.getLetterTitle());
        assertEquals("기증자1", firstHeavenResponse.getDonorName());
        assertEquals(memorialAnonymityFlag, firstHeavenResponse.getMemorialAnonymityFlag());
        assertEquals("작성자1", firstHeavenResponse.getLetterWriter());
        assertEquals(heavenAnonymityFlag, firstHeavenResponse.getHeavenAnonymityFlag());
        assertEquals(Integer.valueOf(readCount), firstHeavenResponse.getReadCount());
        assertEquals(now.toLocalDate().toString(), firstHeavenResponse.getWriteTime());
    }

    @Test
    @DisplayName("게시물 상세 조회 테스트")
    public void getHeavenDetailTest() {
        // 조회 수 증가 테스트

        /* given */
        int letterSeq = 1;
        LocalDateTime now = LocalDateTime.now();
        int commentSize = 3;
        long commentCount = 10;

        HeavenDto heavenDto = HeavenDto.builder()
                .letterSeq(letterSeq)
                .letterTitle("사랑하는 가족에게")
                .letterWriter("작성자")
                .heavenAnonymityFlag("N")
                .readCount(0)
                .letterContents("이 편지는 하늘로 보냅니다.")
                .writeTime(now)
                .build();

        List<HeavenCommentResponse> heavenCommentResponseList = new ArrayList<>();
        for (int i = 1; i <= commentCount; i++) {
            heavenCommentResponseList.add(new HeavenCommentResponse(i, "댓글 작성자"+i, "댓글 내용"+i, now));
        }

        when(heavenFinder.findAnonymizedByIdOrThrow(letterSeq)).thenReturn(heavenDto);
        when(heavenCommentService.getHeavenCommentList(letterSeq, null, commentSize + 1)).thenReturn(heavenCommentResponseList);
        when(heavenCommentRepository.countByLetterSeq(letterSeq)).thenReturn(commentCount);

        /* when */
        HeavenDetailResponse heavenDetailResponse = heavenServiceImpl.getHeavenDetail(letterSeq);
        CursorCommentPaginationResponse<HeavenCommentResponse, Integer> cursorCommentPaginationResponse = heavenDetailResponse.getCursorCommentPaginationResponse();
        HeavenCommentResponse firstHeavenCommentResponse = cursorCommentPaginationResponse.getContent().get(0);

        /* then */
        assertNotNull(cursorCommentPaginationResponse);
        assertEquals(commentSize, cursorCommentPaginationResponse.getContent().size());
        assertEquals(Integer.valueOf(3), cursorCommentPaginationResponse.getCommentNextCursor());
        assertTrue(cursorCommentPaginationResponse.isCommentHasNext());

        assertEquals(letterSeq, heavenDetailResponse.getHeavenDto().getLetterSeq());
        assertEquals("사랑하는 가족에게", heavenDetailResponse.getHeavenDto().getLetterTitle());
        assertEquals("작성자", heavenDetailResponse.getHeavenDto().getLetterWriter());
        assertEquals("이 편지는 하늘로 보냅니다.", heavenDetailResponse.getHeavenDto().getLetterContents());
        assertEquals(now.toLocalDate().toString(), heavenDetailResponse.getWriteTime());

        assertEquals(1, firstHeavenCommentResponse.getCommentSeq());
        assertEquals("댓글 작성자1", firstHeavenCommentResponse.getCommentWriter());
        assertEquals("댓글 내용1", firstHeavenCommentResponse.getContents());
        assertEquals(now.toLocalDate().toString(), firstHeavenCommentResponse.getWriteTime());
    }
}