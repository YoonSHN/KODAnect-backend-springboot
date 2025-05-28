package kodanect.domain.remembrance.service.impl;

import kodanect.common.util.MemorialFinder;
import kodanect.domain.remembrance.dto.MemorialDetailDto;
import kodanect.domain.remembrance.dto.MemorialListDto;
import kodanect.domain.remembrance.dto.MemorialReplyDto;
import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.repository.MemorialRepository;
import kodanect.domain.remembrance.service.MemorialReplyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class MemorialServiceImplTest {

    @InjectMocks
    private MemorialServiceImpl memorialService;

    @Mock
    private MemorialRepository memorialRepository;

    @Mock
    private MemorialReplyService memorialReplyService;

    @Mock
    private MemorialFinder memorialFinder;

    @Test
    public void getSearchMemorialList() throws Exception {
        String page = "1";
        String size = "20";
        String startDate = "2023-01-01";
        String endDate = "2024-01-01";
        String searchWord = "홍길동";

        Pageable pageable = PageRequest.of(0,20);
        List<MemorialListDto> content = List.of(
                new MemorialListDto(1, "홍길동", "N", "20230101", "M", 40, "N", 5)
        );
        Page<MemorialListDto> mockPage = new PageImpl<>(content, pageable, 1);

        when(memorialRepository.findSearchMemorialList(
                pageable, "20230101", "20240101", "%홍길동%")
        ).thenReturn(mockPage);

        Page<MemorialListDto> result = memorialService.getSearchMemorialList(page, size, startDate, endDate, searchWord);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(Integer.valueOf(1), result.getContent().get(0).getDonateSeq());
        assertEquals("홍길동", result.getContent().get(0).getDonorName());
        assertEquals("N", result.getContent().get(0).getAnonymityFlag());
        assertEquals("20230101", result.getContent().get(0).getDonateDate());
        assertEquals("M", result.getContent().get(0).getGenderFlag());
        assertEquals(Integer.valueOf(40), result.getContent().get(0).getDonateAge());
        assertEquals(5, result.getContent().get(0).getReplyCount());
    }

    @Test
    public void getMemorialList() throws Exception {
        String page = "1";
        String size = "20";

        Pageable pageable = PageRequest.of(0,20);
        List<MemorialListDto> content = List.of(
                new MemorialListDto(1, "홍길동", "N", "20230101", "M", 40, "N", 5),
                new MemorialListDto(2, "김길동", "Y", "20230102", "F", 20, "Y", 2)
        );
        Page<MemorialListDto> mockPage = new PageImpl<>(content, pageable, 2);

        when(memorialRepository.findMemorialList(pageable)).thenReturn(mockPage);

        Page<MemorialListDto> result = memorialService.getMemorialList(page, size);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        MemorialListDto dto1 = result.getContent().get(0);
        assertEquals(Integer.valueOf(1), dto1.getDonateSeq());
        assertEquals("홍길동", dto1.getDonorName());
        assertEquals("N", dto1.getAnonymityFlag());
        assertEquals("20230101", dto1.getDonateDate());
        assertEquals("M", dto1.getGenderFlag());
        assertEquals(Integer.valueOf(40), dto1.getDonateAge());
        assertEquals(5, dto1.getReplyCount());

        MemorialListDto dto2 = result.getContent().get(1);
        assertEquals(Integer.valueOf(2), dto2.getDonateSeq());
        assertEquals("김길동", dto2.getDonorName());
        assertEquals("Y", dto2.getAnonymityFlag());
        assertEquals("20230102", dto2.getDonateDate());
        assertEquals("F", dto2.getGenderFlag());
        assertEquals(Integer.valueOf(20), dto2.getDonateAge());
        assertEquals(2, dto2.getReplyCount());

    }

    @Test
    public void getMemorialByDonateSeq() throws Exception {

        int donateSeq = 1;

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

        List<MemorialReplyDto> replies = List.of(
                MemorialReplyDto.builder()
                        .replySeq(1)
                        .donateSeq(donateSeq)
                        .replyWriter("김길동")
                        .replyContents("감사합니다")
                        .replyWriteTime(LocalDateTime.of(2024, 1, 1, 14, 0))
                        .replyWriterId("admin")
                        .delFlag("N")
                        .build()
        );

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(memorial);
        when(memorialReplyService.findMemorialReplyList(donateSeq)).thenReturn(replies);

        MemorialDetailDto result = memorialService.getMemorialByDonateSeq(donateSeq);

        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getDonateSeq());
        assertEquals("홍길동", result.getDonorName());
        assertEquals("N", result.getAnonymityFlag());
        assertEquals("기억합니다", result.getDonateTitle());
        assertEquals("A1", result.getAreaCode());
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
        assertEquals("admin", result.getWriterId());
        assertEquals(LocalDateTime.of(2024, 1, 2, 12, 0), result.getModifyTime());
        assertEquals("admin", result.getModifierId());
        assertEquals("N", result.getDelFlag());
        assertEquals(1, result.getMemorialReplyDtoList().size());
    }

}