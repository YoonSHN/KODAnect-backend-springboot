package kodanect.domain.remembrance.service;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.util.MemorialFinder;
import kodanect.domain.remembrance.dto.MemorialDetailResponse;
import kodanect.domain.remembrance.dto.MemorialResponse;
import kodanect.domain.remembrance.dto.MemorialReplyResponse;
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
class MemorialServiceImplTest {

    @InjectMocks
    private MemorialServiceImpl memorialService;

    @Mock
    private MemorialRepository memorialRepository;

    @Mock
    private MemorialReplyService memorialReplyService;

    @Mock
    private MemorialFinder memorialFinder;

    @Test
    @DisplayName("추모관 이모지 카운팅")
    void 추모관_이모지_카운팅() throws Exception {
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
    void 추모관_게시글_검색_조회() throws Exception {
        /* getSearchMemorialList */
        Integer cursor = 1;
        int size = 20;
        String startDate = "2023-01-01";
        String endDate = "2024-01-01";
        String searchWord = "홍길동";

        List<MemorialResponse> content = List.of(
                new MemorialResponse(1, "홍길동", "N", "20230101", "M", 40, 5)
        );

        when(memorialRepository.findSearchByCursor(
                eq(cursor), any(Pageable.class), eq("20230101"), eq("20240101"), eq("%홍길동%"))
        ).thenReturn(content);

        CursorPaginationResponse<MemorialResponse> result
                = memorialService.getSearchMemorialList(startDate, endDate, searchWord, cursor, size);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(Integer.valueOf(1), result.getContent().get(0).getDonateSeq());
        assertEquals("홍길동", result.getContent().get(0).getDonorName());
        assertEquals("N", result.getContent().get(0).getAnonymityFlag());
        assertEquals("20230101", result.getContent().get(0).getDonateDate());
        assertEquals("M", result.getContent().get(0).getGenderFlag());
        assertEquals(Integer.valueOf(40), result.getContent().get(0).getDonateAge());
        assertEquals(5, result.getContent().get(0).getReplyCount());
    }

    @Test
    @DisplayName("추모관 게시글 리스트 조회")
    void 추모관_게시글_리스트_조회() throws Exception {
        /* getMemorialList */

        Integer cursor = 1;
        int size = 20;

        List<MemorialResponse> content = List.of(
                new MemorialResponse(1, "홍길동", "N", "20230101", "M", 40, 5),
                new MemorialResponse(2, "김길동", "Y", "20230102", "F", 20, 2)
        );

        when(memorialRepository.findByCursor(eq(cursor), any(Pageable.class))).thenReturn(content);

        CursorPaginationResponse<MemorialResponse> page = memorialService.getMemorialList(cursor, size);

        assertNotNull(page);
        assertEquals(2, page.getContent().size());

        MemorialResponse dto1 = page.getContent().get(0);
        assertEquals(Integer.valueOf(1), dto1.getDonateSeq());
        assertEquals("홍길동", dto1.getDonorName());
        assertEquals("N", dto1.getAnonymityFlag());
        assertEquals("20230101", dto1.getDonateDate());
        assertEquals("M", dto1.getGenderFlag());
        assertEquals(Integer.valueOf(40), dto1.getDonateAge());
        assertEquals(5, dto1.getReplyCount());

        MemorialResponse dto2 = page.getContent().get(1);
        assertEquals(Integer.valueOf(2), dto2.getDonateSeq());
        assertEquals("김길동", dto2.getDonorName());
        assertEquals("Y", dto2.getAnonymityFlag());
        assertEquals("20230102", dto2.getDonateDate());
        assertEquals("F", dto2.getGenderFlag());
        assertEquals(Integer.valueOf(20), dto2.getDonateAge());
        assertEquals(2, dto2.getReplyCount());

    }

    @Test
    @DisplayName("추모관 게시글 상세 조회")
    void 추모관_게시글_상세_조회() throws Exception {
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

        List<MemorialReplyResponse> page = List.of(
                MemorialReplyResponse.builder()
                        .replySeq(1)
                        .replyWriter("김길동")
                        .replyContents("감사합니다")
                        .replyWriteTime(LocalDateTime.of(2024, 1, 1, 14, 0))
                        .build()
        );

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(memorial);
        when(memorialReplyService.getMemorialReplyList(donateSeq, null, size+1)).thenReturn(page);

        MemorialDetailResponse result = memorialService.getMemorialByDonateSeq(donateSeq);

        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getDonateSeq());
        assertEquals("홍길동", result.getDonorName());
        assertEquals("N", result.getAnonymityFlag());
        assertEquals("기억합니다", result.getDonateTitle());
        assertEquals("내용입니다", result.getContents());
        assertEquals("20240101", result.getDonateDate());
        assertEquals("M", result.getGenderFlag());
        assertEquals(Integer.valueOf(40), result.getDonateAge());
        assertEquals(1, result.getFlowerCount());
        assertEquals(2, result.getLoveCount());
        assertEquals(3, result.getSeeCount());
        assertEquals(4, result.getMissCount());
        assertEquals(5, result.getProudCount());
        assertEquals(6, result.getHardCount());
        assertEquals(7, result.getSadCount());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), result.getWriteTime());
        assertEquals(1, result.getMemorialReplyResponses().size());
    }

}