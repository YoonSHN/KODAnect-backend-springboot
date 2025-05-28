package kodanect.domain.remembrance.controller;

import kodanect.common.config.EgovConfigCommon;
import kodanect.domain.remembrance.dto.MemorialDetailDto;
import kodanect.domain.remembrance.dto.MemorialListDto;
import kodanect.domain.remembrance.dto.MemorialReplyDto;
import kodanect.domain.remembrance.service.MemorialService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/* JUnit 4 */
@WebMvcTest(MemorialController.class)
@ExtendWith(MockitoExtension.class)
@Import(EgovConfigCommon.class)
class MemorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemorialService memorialService;

    /* 검색 조건 */
    @Captor ArgumentCaptor<String> pageCaptor;
    @Captor ArgumentCaptor<String> sizeCaptor;
    @Captor ArgumentCaptor<String> searchWordCaptor;
    @Captor ArgumentCaptor<String> startDateCaptor;
    @Captor ArgumentCaptor<String> endDateCaptor;

    @Test
    @DisplayName("게시글 리스트 조회 성공")
    void getMemorialList() throws Exception {
        /* 기증자 추모관 게시글 리스트 조회 테스트 */

        List<MemorialListDto> content = List.of(
                new MemorialListDto(1, "홍길동", "N", "20200101", "M", 10, "N",12),
                new MemorialListDto(2, "김길동", "Y", "20211231", "F", 20, "Y", 22),
                new MemorialListDto(3, "나길동", "N", "20220101", "M", 30, "N", 32)
        );

        Page<MemorialListDto> page = new PageImpl<>(content, PageRequest.of(0,20), 3);

        given(memorialService.getMemorialList("1", "20")).willReturn(page);

        mockMvc.perform(get("/remembrance")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시글 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].donateSeq").value(1))
                .andExpect(jsonPath("$.data.content[1].donateSeq").value(2))
                .andExpect(jsonPath("$.data.content[2].donateSeq").value(3))
                .andExpect(jsonPath("$.data.content[0].donorName").value("홍길동"))
                .andExpect(jsonPath("$.data.content[1].donorName").value("김길동"))
                .andExpect(jsonPath("$.data.content[2].donorName").value("나길동"))
                .andExpect(jsonPath("$.data.content[0].anonymityFlag").value("N"))
                .andExpect(jsonPath("$.data.content[1].anonymityFlag").value("Y"))
                .andExpect(jsonPath("$.data.content[2].anonymityFlag").value("N"))
                .andExpect(jsonPath("$.data.content[0].donateDate").value("20200101"))
                .andExpect(jsonPath("$.data.content[1].donateDate").value("20211231"))
                .andExpect(jsonPath("$.data.content[2].donateDate").value("20220101"))
                .andExpect(jsonPath("$.data.content[0].genderFlag").value("M"))
                .andExpect(jsonPath("$.data.content[1].genderFlag").value("F"))
                .andExpect(jsonPath("$.data.content[2].genderFlag").value("M"))
                .andExpect(jsonPath("$.data.content[0].donateAge").value(10))
                .andExpect(jsonPath("$.data.content[1].donateAge").value(20))
                .andExpect(jsonPath("$.data.content[2].donateAge").value(30))
                .andExpect(jsonPath("$.data.content[0].delFlag").value("N"))
                .andExpect(jsonPath("$.data.content[1].delFlag").value("Y"))
                .andExpect(jsonPath("$.data.content[2].delFlag").value("N"))
                .andExpect(jsonPath("$.data.content[0].replyCount").value(12))
                .andExpect(jsonPath("$.data.content[1].replyCount").value(22))
                .andExpect(jsonPath("$.data.content[2].replyCount").value(32))
                .andExpect(jsonPath("$.data.content.length()").value(3))
        ;

    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getMemorialByDonateSeq() throws Exception {
        /* 기증자 추모관 상세 게시글 조회 테스트 */

        /* 게시글 댓글 조회1 */
        MemorialReplyDto reply1 = MemorialReplyDto.builder()
                .replySeq(1)
                .donateSeq(1)
                .replyWriter("홍길동")
                .replyPassword("1234")
                .replyContents("안녕하세요")
                .replyWriteTime(LocalDateTime.of(2024,1,1,12,0,0))
                .replyWriterId("운영자")
                .replyModifyTime(LocalDateTime.of(2024,1,1,20,0,0))
                .replyModifierId("운영자")
                .delFlag("N")
                .build();

        /* 게시글 댓글 조회2 */
        MemorialReplyDto reply2 = MemorialReplyDto.builder()
                .replySeq(2)
                .donateSeq(1)
                .replyWriter("김길동")
                .replyPassword("1234")
                .replyContents("잘가세요")
                .replyWriteTime(LocalDateTime.of(2022,1,1,12,0,0))
                .replyWriterId("운영자")
                .replyModifyTime(LocalDateTime.of(2024,1,1,22,0,0))
                .replyModifierId("운영자")
                .delFlag("N")
                .build();

        /* 게시글 댓글 리스트 */
        List<MemorialReplyDto> replies = List.of(
                reply1,
                reply2
        );

        MemorialDetailDto memorial = MemorialDetailDto.builder()
                .donateSeq(1)
                .donorName("홍길동")
                .anonymityFlag("N")
                .donateTitle("당신을 기억합니다.")
                .areaCode("A1")
                .contents("감사한 마음을 전합니다.")
                .fileName("image.jpg")
                .orgFileName("original.jpg")
                .writer("관리자")
                .donateDate("20240101")
                .genderFlag("M")
                .donateAge(43)
                .flowerCount(1)
                .loveCount(2)
                .seeCount(3)
                .missCount(4)
                .proudCount(5)
                .hardCount(6)
                .sadCount(7)
                .writeTime(LocalDateTime.of(2024,1,1,12,0,0))
                .writerId("운영자")
                .modifyTime(LocalDateTime.of(2024,1,1,20,0,0))
                .modifierId("운영자")
                .delFlag("N")
                .memorialReplyDtoList(replies)
                .build();

        given(memorialService.getMemorialByDonateSeq(1)).willReturn(memorial);

        mockMvc.perform(get("/remembrance/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시글 조회 성공"))
                .andExpect(jsonPath("$.data.donateSeq").value(1))
                .andExpect(jsonPath("$.data.donorName").value("홍길동"))
                .andExpect(jsonPath("$.data.anonymityFlag").value("N"))
                .andExpect(jsonPath("$.data.donateTitle").value("당신을 기억합니다."))
                .andExpect(jsonPath("$.data.areaCode").value("A1"))
                .andExpect(jsonPath("$.data.contents").value("감사한 마음을 전합니다."))
                .andExpect(jsonPath("$.data.fileName").value("image.jpg"))
                .andExpect(jsonPath("$.data.orgFileName").value("original.jpg"))
                .andExpect(jsonPath("$.data.writer").value("관리자"))
                .andExpect(jsonPath("$.data.donateDate").value("20240101"))
                .andExpect(jsonPath("$.data.genderFlag").value("M"))
                .andExpect(jsonPath("$.data.donateAge").value(43))
                .andExpect(jsonPath("$.data.flowerCount").value(1))
                .andExpect(jsonPath("$.data.loveCount").value(2))
                .andExpect(jsonPath("$.data.seeCount").value(3))
                .andExpect(jsonPath("$.data.missCount").value(4))
                .andExpect(jsonPath("$.data.proudCount").value(5))
                .andExpect(jsonPath("$.data.hardCount").value(6))
                .andExpect(jsonPath("$.data.sadCount").value(7))
                .andExpect(jsonPath("$.data.writeTime").value("2024-01-01T12:00:00"))
                .andExpect(jsonPath("$.data.writerId").value("운영자"))
                .andExpect(jsonPath("$.data.modifyTime").value("2024-01-01T20:00:00"))
                .andExpect(jsonPath("$.data.modifierId").value("운영자"))
                .andExpect(jsonPath("$.data.delFlag").value("N"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[0].replySeq").value(1))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[0].donateSeq").value(1))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[0].replyWriter").value("홍길동"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[0].replyPassword").value("1234"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[0].replyContents").value("안녕하세요"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[0].replyWriteTime").value("2024-01-01T12:00:00"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[0].replyWriterId").value("운영자"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[0].replyModifyTime").value("2024-01-01T20:00:00"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[0].delFlag").value("N"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[1].replySeq").value(2))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[1].donateSeq").value(1))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[1].replyWriter").value("김길동"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[1].replyPassword").value("1234"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[1].replyContents").value("잘가세요"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[1].replyWriteTime").value("2022-01-01T12:00:00"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[1].replyWriterId").value("운영자"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[1].replyModifyTime").value("2024-01-01T22:00:00"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList[1].delFlag").value("N"))
                .andExpect(jsonPath("$.data.memorialReplyDtoList.length()").value(2));


    }

    @Test
    @DisplayName("게시글 검색 조회 성공")
    void getSearchMemorialList() throws Exception {
        /* 기증자 추모관 게시글 검색 리스트 조회 */
        /* 검색 조건이 없는 경우 */
        /* 컨트롤러 테스트는 필터링 로직이 반영되지 않음 */

        List<MemorialListDto> content = List.of(
                new MemorialListDto(1, "홍길동", "N", "20200101", "M", 10, "N",12),
                new MemorialListDto(2, "김길동", "Y", "20200102", "F", 20, "Y", 22),
                new MemorialListDto(3, "나길동", "N", "20220103", "M", 30, "N", 32)
        );

        Page<MemorialListDto> page = new PageImpl<>(content, PageRequest.of(0,20), 3);
        given(memorialService.getSearchMemorialList(anyString(), anyString(), anyString(), anyString(), anyString())).willReturn(page);

        mockMvc.perform(get("/remembrance/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시글 검색 조회 성공"))
                .andExpect(jsonPath("$.data.content.length()").value(3));

        verify(memorialService).getSearchMemorialList(
                pageCaptor.capture(),
                sizeCaptor.capture(),
                startDateCaptor.capture(),
                endDateCaptor.capture(),
                searchWordCaptor.capture()
        );

        assertThat(pageCaptor.getValue()).isEqualTo("1");
        assertThat(sizeCaptor.getValue()).isEqualTo("20");
        assertThat(startDateCaptor.getValue()).isEqualTo("1900-01-01");
        assertThat(endDateCaptor.getValue()).isEqualTo("2100-12-31");
        assertThat(searchWordCaptor.getValue()).isEqualTo("");
    }

    @Test
    @DisplayName("이모지 카운트 증가 업데이트 성공")
    void updateMemorialLikeCount() throws Exception {
        /* 기증자 추모관 상세 게시글 이모지 카운트 업데이트 */

        /* 게시글 댓글 조회1 */
        MemorialReplyDto reply1 = MemorialReplyDto.builder()
                .replySeq(1)
                .donateSeq(1)
                .replyWriter("홍길동")
                .replyPassword("1234")
                .replyContents("안녕하세요")
                .replyWriteTime(LocalDateTime.of(2024,1,1,12,0,0))
                .replyWriterId("운영자")
                .replyModifyTime(LocalDateTime.of(2024,1,1,20,0,0))
                .replyModifierId("운영자")
                .delFlag("N")
                .build();

        /* 게시글 댓글 조회2 */
        MemorialReplyDto reply2 = MemorialReplyDto.builder()
                .replySeq(2)
                .donateSeq(1)
                .replyWriter("김길동")
                .replyPassword("1234")
                .replyContents("잘가세요")
                .replyWriteTime(LocalDateTime.of(2022,1,1,12,0,0))
                .replyWriterId("운영자")
                .replyModifyTime(LocalDateTime.of(2024,1,1,22,0,0))
                .replyModifierId("운영자")
                .delFlag("N")
                .build();

        /* 게시글 댓글 리스트 */
        List<MemorialReplyDto> replies = List.of(
                reply1,
                reply2
        );

        MemorialDetailDto memorial = MemorialDetailDto.builder()
                .donateSeq(1)
                .donorName("홍길동")
                .anonymityFlag("N")
                .donateTitle("당신을 기억합니다.")
                .areaCode("A1")
                .contents("감사한 마음을 전합니다.")
                .fileName("image.jpg")
                .orgFileName("original.jpg")
                .writer("관리자")
                .donateDate("20240101")
                .genderFlag("M")
                .donateAge(43)
                .flowerCount(0)
                .loveCount(0)
                .seeCount(0)
                .missCount(0)
                .proudCount(0)
                .hardCount(0)
                .sadCount(0)
                .writeTime(LocalDateTime.of(2024,1,1,12,0,0))
                .writerId("운영자")
                .modifyTime(LocalDateTime.of(2024,1,1,20,0,0))
                .modifierId("운영자")
                .delFlag("N")
                .memorialReplyDtoList(replies)
                .build();

        /* 반환 타입이 없는 서비스 */
        doNothing().when(memorialService).emotionCountUpdate(eq(1), anyString());

        mockMvc.perform(patch("/remembrance/1/flower"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시글 이모지 카운트 업데이트 성공"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(memorialService).emotionCountUpdate(eq(1), eq("flower"));

    }
}