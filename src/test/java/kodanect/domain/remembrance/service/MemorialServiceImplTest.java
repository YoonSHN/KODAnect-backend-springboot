package kodanect.domain.remembrance.service;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.util.MemorialFinder;
import kodanect.domain.heaven.dto.response.MemorialHeavenResponse;
import kodanect.domain.heaven.service.HeavenService;
import kodanect.domain.remembrance.TestMemorialResponse;
import kodanect.domain.remembrance.dto.MemorialDetailResponse;
import kodanect.domain.remembrance.dto.MemorialResponse;
import kodanect.domain.remembrance.dto.MemorialCommentResponse;
import kodanect.domain.remembrance.dto.common.MemorialNextCursor;
import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.repository.MemorialRepository;
import kodanect.domain.remembrance.service.impl.MemorialServiceImpl;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class MemorialServiceImplTest {

    @InjectMocks
    private MemorialServiceImpl memorialService;

    @Mock
    private MemorialRepository memorialRepository;

    @Mock
    private MemorialCommentService memorialCommentService;

    @Mock
    private MemorialFinder memorialFinder;

    @Mock
    private HeavenService heavenService;

    @Test
    @DisplayName("추모관 이모지 카운팅")
    public void 추모관_이모지_카운팅() throws Exception {
        /* emotionCountUpdate */

        Integer donateSeq = 1;
        final String FLOWER = "flower";
        final String SAD = "SAD";
        final String SEE = "SEE";
        final String HARD = "HARD";
        final String MISS = "MISS";
        final String PROUD = "PROUD";
        final String LOVE = "love";

        Memorial memorial = Memorial.builder()
                .donateSeq(donateSeq)
                .donorName("홍길동")
                .anonymityFlag("N")
                .donateTitle("기억합니다")
                .areaCode("A1")
                .contents("내용입니다")
                .fileName("img.jpg")
                .orgFileName("origin.jpg")
                .writer("관리자")
                .donateDate("20240101")
                .genderFlag("M")
                .donateAge(40)
                .flowerCount(1)
                .loveCount(2)
                .seeCount(3)
                .missCount(4)
                .proudCount(5)
                .hardCount(6)
                .sadCount(7)
                .writeTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .writerId("admin")
                .modifyTime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .modifierId("admin")
                .delFlag("N")
                .build();

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(memorial);
        doNothing().when(memorialRepository).incrementFlower(donateSeq);
        doNothing().when(memorialRepository).incrementHard(donateSeq);
        doNothing().when(memorialRepository).incrementLove(donateSeq);
        doNothing().when(memorialRepository).incrementMiss(donateSeq);
        doNothing().when(memorialRepository).incrementSad(donateSeq);
        doNothing().when(memorialRepository).incrementProud(donateSeq);
        doNothing().when(memorialRepository).incrementSee(donateSeq);

        memorialService.emotionCountUpdate(donateSeq, FLOWER);
        memorialService.emotionCountUpdate(donateSeq, HARD);
        memorialService.emotionCountUpdate(donateSeq, LOVE);
        memorialService.emotionCountUpdate(donateSeq, MISS);
        memorialService.emotionCountUpdate(donateSeq, SAD);
        memorialService.emotionCountUpdate(donateSeq, PROUD);
        memorialService.emotionCountUpdate(donateSeq, SEE);

        verify(memorialRepository, times(1)).incrementFlower(donateSeq);
        verify(memorialRepository, times(1)).incrementProud(donateSeq);
        verify(memorialRepository, times(1)).incrementSad(donateSeq);
        verify(memorialRepository, times(1)).incrementMiss(donateSeq);
        verify(memorialRepository, times(1)).incrementSee(donateSeq);
        verify(memorialRepository, times(1)).incrementLove(donateSeq);
        verify(memorialRepository, times(1)).incrementHard(donateSeq);

    }

    @Test
    @DisplayName("추모관 게시글 검색 조회")
    public void 추모관_게시글_검색_조회() throws Exception {
        /* getSearchMemorialList */
        MemorialNextCursor nextCursor = new MemorialNextCursor(null, null);
        int size = 20;
        String startDate = "2023-01-01";
        String endDate = "2024-01-01";
        String searchWord = "홍길동";

        List<MemorialResponse> content = List.of(
                new TestMemorialResponse(2, "홍길동", "2023-01-02", "M", 40, 5, 3),
                new TestMemorialResponse(3, "홍길동", "2023-01-03", "M", 42, 3, 3)
        );

        when(memorialRepository.findSearchByCursor(
                eq(null),             // cursor.getDate()
                eq(null),             // cursor.getCursor()
                eq("20230101"),       // startDateStr
                eq("20240101"),       // endDateStr
                eq("%홍길동%"),        // keyWord
                any(Pageable.class)   // Pageable
        )).thenReturn(content);

        when(memorialRepository.countBySearch(
                "20230101", "20240101", "%홍길동%")
        ).thenReturn(2L);

        CursorPaginationResponse<MemorialResponse, MemorialNextCursor> result
                = memorialService.getSearchMemorialList(startDate, endDate, searchWord, nextCursor, size);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        MemorialResponse item = result.getContent().get(0);
        assertEquals("홍길동", item.getDonorName());
        assertEquals("2023-01-02", item.getDonateDate());
        assertEquals("M", item.getGenderFlag());
        assertEquals(Integer.valueOf(40), item.getDonateAge());
        assertEquals(5, item.getCommentCount());
    }


    @Test
    @DisplayName("추모관 게시글 리스트 조회")
    public void 추모관_게시글_리스트_조회() throws Exception {
        /* getMemorialList */

        MemorialNextCursor nextCursor = new MemorialNextCursor(null, null);
        int size = 20;

        List<MemorialResponse> content = List.of(
                new TestMemorialResponse(1, "홍길동", "20230101", "M", 40, 5, 3),
                new TestMemorialResponse(2, "김길동", "20230102", "F", 20, 2, 3)
        );

        when(memorialRepository.findByCursor(eq(nextCursor.getCursor()), eq(nextCursor.getDate()), any(Pageable.class))).thenReturn(content);

        CursorPaginationResponse<MemorialResponse, MemorialNextCursor> page = memorialService.getMemorialList(nextCursor, size);

        assertNotNull(page);
        assertEquals(2, page.getContent().size());

        MemorialResponse dto1 = page.getContent().get(0);
        assertEquals(Integer.valueOf(1), dto1.getDonateSeq());
        assertEquals("홍길동", dto1.getDonorName());
        assertEquals("2023-01-01", dto1.getDonateDate());
        assertEquals("M", dto1.getGenderFlag());
        assertEquals(Integer.valueOf(40), dto1.getDonateAge());
        assertEquals(5, dto1.getCommentCount());

        MemorialResponse dto2 = page.getContent().get(1);
        assertEquals(Integer.valueOf(2), dto2.getDonateSeq());
        assertEquals("김길동", dto2.getDonorName());
        assertEquals("2023-01-02", dto2.getDonateDate());
        assertEquals("F", dto2.getGenderFlag());
        assertEquals(Integer.valueOf(20), dto2.getDonateAge());
        assertEquals(2, dto2.getCommentCount());

    }

    @Test
    @DisplayName("추모관 게시글 상세 조회")
    public void 추모관_게시글_상세_조회() throws Exception {
        /* getMemorialByDonateSeq */

        Integer donateSeq = 1;
        int size = 3;

        Memorial memorial = Memorial.builder()
                .donateSeq(donateSeq)
                .donorName("홍길동")
                .anonymityFlag("N")
                .donateTitle("기억합니다")
                .areaCode("A1")
                .contents("내용입니다")
                .fileName("img.jpg")
                .orgFileName("origin.jpg")
                .writer("관리자")
                .donateDate("2024-01-01")
                .genderFlag("M")
                .donateAge(40)
                .flowerCount(1)
                .loveCount(2)
                .seeCount(3)
                .missCount(4)
                .proudCount(5)
                .hardCount(6)
                .sadCount(7)
                .writeTime(LocalDateTime.of(2024, 1, 1, 12, 0))
                .writerId("admin")
                .modifyTime(LocalDateTime.of(2024, 1, 2, 12, 0))
                .modifierId("admin")
                .delFlag("N")
                .build();

        List<MemorialCommentResponse> page = List.of(
                MemorialCommentResponse.builder()
                        .commentSeq(1)
                        .commentWriter("김길동")
                        .contents("감사합니다")
                        .writeTime(LocalDateTime.of(2024, 1, 1, 14, 0))
                        .build()
        );

        CursorPaginationResponse<MemorialHeavenResponse, Integer> letters =
                CursorPaginationResponse.<MemorialHeavenResponse, Integer>builder()
                        .content(List.of(
                                MemorialHeavenResponse.builder()
                                        .letterSeq(1)
                                        .letterTitle("하늘나라에 보낸 편지")
                                        .readCount(10)
                                        .writeTime(LocalDateTime.of(2024, 1, 1, 15, 0))
                                        .build()
                        ))
                        .nextCursor(null)
                        .hasNext(false)
                        .build();


        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(memorial);
        when(memorialCommentService.getMemorialCommentList(eq(donateSeq), eq(null), anyInt())).thenReturn(page);
        when(heavenService.getMemorialHeavenList(eq(donateSeq), eq(null), anyInt())).thenReturn(letters);

        MemorialDetailResponse result = memorialService.getMemorialByDonateSeq(donateSeq);

        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getDonateSeq());
        assertEquals("홍길동", result.getDonorName());
        assertEquals("기억합니다", result.getDonateTitle());
        assertEquals("내용입니다", result.getContents());
        assertEquals("2024-01-01", result.getDonateDate());
        assertEquals("M", result.getGenderFlag());
        assertEquals(Integer.valueOf(40), result.getDonateAge());
        assertEquals(1, result.getFlowerCount());
        assertEquals(2, result.getLoveCount());
        assertEquals(3, result.getSeeCount());
        assertEquals(4, result.getMissCount());
        assertEquals(5, result.getProudCount());
        assertEquals(6, result.getHardCount());
        assertEquals(7, result.getSadCount());
        assertEquals("2024-01-01", result.getWriteTime());
        assertEquals(1, result.getHeavenLetterResponses().getContent().get(0).getLetterSeq());
    }

}