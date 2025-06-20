package kodanect.domain.heaven.controller;

import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.heaven.dto.HeavenDto;
import kodanect.domain.heaven.dto.response.HeavenCommentResponse;
import kodanect.domain.heaven.dto.response.HeavenDetailResponse;
import kodanect.domain.heaven.dto.response.HeavenResponse;
import kodanect.domain.heaven.service.HeavenService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(HeavenController.class)
public class HeavenControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private HeavenService heavenService;
    @MockBean
    private MessageSourceAccessor messageSourceAccessor;

    @Before
    public void beforeEach() {
        when(messageSourceAccessor.getMessage("board.read.success")).thenReturn("게시물 조회 성공");
        when(messageSourceAccessor.getMessage("board.list.read.success")).thenReturn("게시물 리스트 조회 성공");
        when(messageSourceAccessor.getMessage("board.search.read.success")).thenReturn("검색을 통한 게시물 조회 성공");
    }

    @Test
    @DisplayName("게시물 전체 조회 테스트")
    public void getHeavenListTest() throws Exception {
        /* given */
        String memorialAnonymityFlag = "Y";
        String heavenAnonymityFlag = "N";
        int readCount = 5;
        LocalDateTime now = LocalDateTime.now();
        Integer nextCursor = 10;
        boolean hasNext = true;
        long totalCount = 30;


        List<HeavenResponse> heavenResponseList = new ArrayList<>();

        for (int i = 1; i <= totalCount; i++) {
            heavenResponseList.add(new HeavenResponse(i, "제목"+i, "기증자"+i, memorialAnonymityFlag, "작성자"+i, heavenAnonymityFlag, readCount, now));
        }

        CursorPaginationResponse<HeavenResponse, Integer> cursorPaginationResponse = CursorPaginationResponse.<HeavenResponse, Integer>builder()
                .content(heavenResponseList)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .totalCount(totalCount)
                .build();

        when(heavenService.getHeavenList(30, 20)).thenReturn(cursorPaginationResponse);

        /* when & then */
        mockMvc.perform(get("/heavenLetters")
                        .param("cursor", "30")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시물 리스트 조회 성공"))
                .andExpect(jsonPath("$.data.content[29].letterSeq").value(30))
                .andExpect(jsonPath("$.data.nextCursor").value(10))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(30));
    }

    @Test
    @DisplayName("검색을 통한 게시물 전체 조회 테스트")
    public void searchHeavenListTest() throws Exception {
        /* given */
        String memorialAnonymityFlag = "Y";
        String heavenAnonymityFlag = "N";
        int readCount = 5;
        LocalDateTime now = LocalDateTime.now();
        boolean hasNext = false;
        long totalCount = 10;


        List<HeavenResponse> heavenResponseList = new ArrayList<>();

        for (int i = 1; i <= totalCount; i++) {
            heavenResponseList.add(new HeavenResponse(i, "제목"+i, "기증자"+i, memorialAnonymityFlag, "작성자"+i, heavenAnonymityFlag, readCount, now));
        }

        CursorPaginationResponse<HeavenResponse, Integer> cursorPaginationResponse = CursorPaginationResponse.<HeavenResponse, Integer>builder()
                .content(heavenResponseList)
                .nextCursor(null)
                .hasNext(hasNext)
                .totalCount(totalCount)
                .build();

        when(heavenService.getHeavenListSearchResult("ALL", "제목", 10,20)).thenReturn(cursorPaginationResponse);

        /* when & then */
        mockMvc.perform(get("/heavenLetters/search")
                        .param("type", "ALL")
                        .param("keyWord", "제목")
                        .param("cursor", "10")
                        .param("size", "20"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("검색을 통한 게시물 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].letterSeq").value(1))
                .andExpect(jsonPath("$.data.nextCursor", nullValue()))
                .andExpect(jsonPath("$.data.hasNext").value(hasNext))
                .andExpect(jsonPath("$.data.totalCount").value(totalCount));
    }

    @Test
    @DisplayName("게시물 상세 조회 테스트")
    public void getHeavenDetailTest() throws Exception {
        /* given */
        Integer letterSeq = 1;
        String letterTitle = "사랑하는 가족에게";
        String letterWriter = "작성자";
        String heavenAnonymityFlag = "N";
        Integer readCount = 5;
        String letterContents = "이 편지는 하늘로 보냅니다.";
        LocalDateTime writeTime = LocalDateTime.now();

        boolean commentHasNext = false;
        long commentCount = 2L;

        List<HeavenCommentResponse> heavenCommentResponseList = new ArrayList<>();
        for (int i = 1; i <= commentCount; i++) {
            heavenCommentResponseList.add(new HeavenCommentResponse(i, "댓글 작성자"+i, "댓글 내용"+i, writeTime));
        }
        CursorCommentPaginationResponse<HeavenCommentResponse, Integer> cursorCommentPaginationResponse = CursorCommentPaginationResponse.<HeavenCommentResponse, Integer>builder()
                .content(heavenCommentResponseList)
                .commentNextCursor(null)
                .commentHasNext(commentHasNext)
                .build();

        HeavenDto heavenDto = HeavenDto.builder()
                .letterSeq(letterSeq)
                .letterTitle(letterTitle)
                .letterWriter(letterWriter)
                .heavenAnonymityFlag(heavenAnonymityFlag)
                .readCount(readCount)
                .letterContents(letterContents)
                .writeTime(writeTime)
                .build();


        HeavenDetailResponse heavenDetailResponse = HeavenDetailResponse.builder()
                .heavenDto(heavenDto)
                .cursorCommentPaginationResponse(cursorCommentPaginationResponse)
                .build();

        when(heavenService.getHeavenDetail(letterSeq)).thenReturn(heavenDetailResponse);

        /* when & then */
        mockMvc.perform(get("/heavenLetters/{letterSeq}", letterSeq))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시물 조회 성공"))
                .andExpect(jsonPath("$.data.letterSeq").value(1))
                .andExpect(jsonPath("$.data.cursorCommentPaginationResponse.content[0].commentSeq").value(1))
                .andExpect(jsonPath("$.data.cursorCommentPaginationResponse.commentNextCursor", nullValue()))
                .andExpect(jsonPath("$.data.cursorCommentPaginationResponse.commentHasNext").value(false));
    }
}