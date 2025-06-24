package kodanect.domain.heaven.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.response.CursorCommentCountPaginationResponse;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.heaven.dto.HeavenDto;
import kodanect.domain.heaven.dto.request.HeavenCreateRequest;
import kodanect.domain.heaven.dto.request.HeavenUpdateRequest;
import kodanect.domain.heaven.dto.request.HeavenVerifyRequest;
import kodanect.domain.heaven.dto.response.HeavenCommentResponse;
import kodanect.domain.heaven.dto.response.HeavenDetailResponse;
import kodanect.domain.heaven.dto.response.HeavenResponse;
import kodanect.domain.heaven.service.HeavenService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@WebMvcTest(HeavenController.class)
public class HeavenControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HeavenService heavenService;
    @MockBean
    private MessageSourceAccessor messageSourceAccessor;

    @Before
    public void before() {
        given(messageSourceAccessor.getMessage("board.read.success")).willReturn("게시글 조회 성공");
        given(messageSourceAccessor.getMessage("board.list.read.success")).willReturn("게시글 리스트 조회 성공");
        given(messageSourceAccessor.getMessage("board.search.read.success")).willReturn("게시글 검색 조회 성공");
        given(messageSourceAccessor.getMessage("board.create.success")).willReturn("게시글 등록 성공");
        given(messageSourceAccessor.getMessage("board.update.success")).willReturn("게시글 수정 성공");
        given(messageSourceAccessor.getMessage("board.delete.success")).willReturn("게시글 삭제 성공");
    }

    @Test
    @DisplayName("게시물 전체 조회 테스트")
    public void getHeavenListTest() throws Exception {
        /* given */
        // heavenService.getHeavenList() 결과 설정
        int letterSize = 52;
        String letterTitle = "편지 제목";
        String donorName = "기****";
        String memorialAnonymityFlag = "Y";
        String letterWriter = "편지 작성자";
        String heavenAnonymityFlag = "N";
        int readCount = 2;
        LocalDateTime writeTime = LocalDateTime.now();

        List<HeavenResponse> heavenResponseList = new ArrayList<>();
        for (int i = letterSize; i >= 1; i--) {
            heavenResponseList.add(new HeavenResponse(i, letterTitle, donorName, memorialAnonymityFlag, letterWriter, heavenAnonymityFlag, readCount, writeTime));
        }
        CursorPaginationResponse<HeavenResponse, Integer> cursorPaginationResponse = CursorPaginationResponse.<HeavenResponse, Integer>builder()
                .content(heavenResponseList)
                .nextCursor(null)
                .hasNext(false)
                .totalCount((long) letterSize)
                .build();

        given(heavenService.getHeavenList(null, 20)).willReturn(cursorPaginationResponse);

        /* when & then */
        mockMvc.perform(get("/heavenLetters")
                        .param("size", "20"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시글 리스트 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].letterSeq").value(letterSize))
                .andExpect(jsonPath("$.data.content[0].letterTitle").value(letterTitle))
                .andExpect(jsonPath("$.data.content[0].donorName").value(donorName))
                .andExpect(jsonPath("$.data.content[0].letterWriter").value(letterWriter))
                .andExpect(jsonPath("$.data.content[0].readCount").value(readCount))
                .andExpect(jsonPath("$.data.content[0].writeTime").value(writeTime.toLocalDate().toString()))
                .andExpect(jsonPath("$.data.nextCursor").doesNotExist())
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.totalCount").value(letterSize));
    }

    @Test
    @DisplayName("검색을 통한 게시물 전체 조회 테스트")
    public void searchHeavenListTest() throws Exception {
        /* given */
        // heavenService.getHeavenList() 결과 설정
        int letterSize = 18;
        int size = 20;
        String letterTitle = "편지 제목";
        String donorName = "기증자 명";
        String memorialAnonymityFlag = "N";
        String letterWriter = "편지 작성자";
        String heavenAnonymityFlag = "N";
        int readCount = 2;
        LocalDateTime writeTime = LocalDateTime.now();

        int nextCursor = letterSize - size + 1;

        List<HeavenResponse> heavenResponseList = new ArrayList<>();
        for (int i = letterSize; i >= 1; i--) {
            heavenResponseList.add(new HeavenResponse(i, letterTitle, donorName, memorialAnonymityFlag, letterWriter, heavenAnonymityFlag, readCount, writeTime));
        }
        CursorPaginationResponse<HeavenResponse, Integer> cursorPaginationResponse = CursorPaginationResponse.<HeavenResponse, Integer>builder()
                .content(heavenResponseList)
                .nextCursor(nextCursor)
                .hasNext(true)
                .totalCount((long) size)
                .build();

        given(heavenService.getHeavenListSearchResult("TITLE", "제목", null, size)).willReturn(cursorPaginationResponse);

        /* when & then */
        mockMvc.perform(get("/heavenLetters/search")
                        .param("type", "TITLE")
                        .param("keyWord", "제목")
                        .param("size", "20"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시글 검색 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].letterSeq").value(letterSize))
                .andExpect(jsonPath("$.data.content[0].letterTitle").value(letterTitle))
                .andExpect(jsonPath("$.data.content[0].donorName").value(donorName))
                .andExpect(jsonPath("$.data.content[0].letterWriter").value(letterWriter))
                .andExpect(jsonPath("$.data.content[0].readCount").value(readCount))
                .andExpect(jsonPath("$.data.content[0].writeTime").value(writeTime.toLocalDate().toString()))
                .andExpect(jsonPath("$.data.nextCursor").value(nextCursor))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(size));
    }

    @Test
    @DisplayName("게시물 상세 조회 테스트")
    public void getHeavenDetailTest() throws Exception {
        /* given */
        // 매개변수 값 설정
        int letterSeq = 14;
        String clientIp = "11.22.33.44";

        // heavenService.getHeavenDetail() 결과 설정
        int donateSeq = 29;
        String letterTitle = "편지 제목";
        String donorName = "기증자 명";
        String memorialAnonymityFlag = "N";
        String letterWriter = "편지 작성자";
        String heavenAnonymityFlag = "N";
        int readCount = 2;
        String letterContents = "편지 내용";
        LocalDateTime writeTime = LocalDateTime.now();
        HeavenDto heavenDto = HeavenDto.builder()
                .letterSeq(letterSeq)
                .donateSeq(donateSeq)
                .letterTitle(letterTitle)
                .donorName(donorName)
                .memorialAnonymityFlag(memorialAnonymityFlag)
                .letterWriter(letterWriter)
                .heavenAnonymityFlag(heavenAnonymityFlag)
                .readCount(readCount)
                .letterContents(letterContents)
                .writeTime(writeTime)
                .build();

        String commentWriter = "작성자 이름";
        String contents = "댓글 내용";
        int commentSize = 5;
        int size = 3;
        List<HeavenCommentResponse> heavenCommentResponseList = new ArrayList<>();
        for (int i = commentSize; i >= 1; i--) {
            heavenCommentResponseList.add(new HeavenCommentResponse(i, commentWriter, contents, writeTime));
        }

        int commentNextCursor = commentSize - size + 1;
        CursorCommentCountPaginationResponse<HeavenCommentResponse, Integer> cursorCommentCountPaginationResponse = CursorCommentCountPaginationResponse.<HeavenCommentResponse, Integer>builder()
                .content(heavenCommentResponseList)
                .commentNextCursor(commentNextCursor)
                .commentHasNext(true)
                .totalCommentCount((long) commentSize)
                .build();

        HeavenDetailResponse heavenDetailResponse = HeavenDetailResponse.builder()
                .heavenDto(heavenDto)
                .cursorCommentPaginationResponse(cursorCommentCountPaginationResponse)
                .build();

        given(heavenService.getHeavenDetail(letterSeq, clientIp)).willReturn(heavenDetailResponse);

        /* when & then */
        mockMvc.perform(get("/heavenLetters/{letterSeq}", letterSeq)
                        .header("X-Forwarded-For", clientIp))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시글 조회 성공"))
                .andExpect(jsonPath("$.data.letterSeq").value(letterSeq))
                .andExpect(jsonPath("$.data.donateSeq").value(donateSeq))
                .andExpect(jsonPath("$.data.letterTitle").value(letterTitle))
                .andExpect(jsonPath("$.data.donorName").value(donorName))
                .andExpect(jsonPath("$.data.letterWriter").value(letterWriter))
                .andExpect(jsonPath("$.data.readCount").value(readCount))
                .andExpect(jsonPath("$.data.letterContents").value(letterContents))
                .andExpect(jsonPath("$.data.writeTime").value(writeTime.toLocalDate().toString()))
                .andExpect(jsonPath("$.data.cursorCommentPaginationResponse.content[0].commentSeq").value(commentSize))
                .andExpect(jsonPath("$.data.cursorCommentPaginationResponse.content[0].commentWriter").value(commentWriter))
                .andExpect(jsonPath("$.data.cursorCommentPaginationResponse.content[0].contents").value(contents))
                .andExpect(jsonPath("$.data.cursorCommentPaginationResponse.content[0].writeTime").value(writeTime.toLocalDate().toString()))
                .andExpect(jsonPath("$.data.cursorCommentPaginationResponse.commentNextCursor").value(commentNextCursor))
                .andExpect(jsonPath("$.data.cursorCommentPaginationResponse.commentHasNext").value(true))
                .andExpect(jsonPath("$.data.cursorCommentPaginationResponse.totalCommentCount").value(commentSize));
    }

    @Test
    @DisplayName("게시글 등록 테스트")
    public void createHeavenTest() throws Exception {
        /* given */
        // 매개변수 값 설정
        String letterWriter = "편지 작성자";
        String anonymityFlag = "N";
        String letterPasscode = "qwer1234";
        String donorName = "기증자 명";
        int donateSeq = 29;
        String letterTitle = "편지 제목";
        String letterContents = "편지 내용";

        ArgumentCaptor<HeavenCreateRequest> captor = ArgumentCaptor.forClass(HeavenCreateRequest.class);

        /* when & then */
        mockMvc.perform(post("/heavenLetters")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .param("letterWriter", "편지 작성자")
                        .param("anonymityFlag", "N")
                        .param("letterPasscode", "qwer1234")
                        .param("donorName", "기증자 명")
                        .param("donateSeq", String.valueOf(29))
                        .param("letterTitle", "편지 제목")
                        .param("letterContents", "편지 내용"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("게시글 등록 성공"))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(heavenService).should(times(1)).createHeaven(captor.capture());
        HeavenCreateRequest captureRequest = captor.getValue();

        assertEquals(letterWriter, captureRequest.getLetterWriter());
        assertEquals(anonymityFlag, captureRequest.getAnonymityFlag());
        assertEquals(letterPasscode, captureRequest.getLetterPasscode());
        assertEquals(donorName, captureRequest.getDonorName());
        assertEquals(donateSeq, captureRequest.getDonateSeq());
        assertEquals(letterTitle, captureRequest.getLetterTitle());
        assertEquals(letterContents, captureRequest.getLetterContents());
    }

    @Test
    @DisplayName("게시물 수정 테스트")
    public void updateHeavenTest() throws Exception {
        /* given */
        // 매개변수 값 설정
        int letterSeq = 14;
        String letterWriter = "편지 작성자";
        String anonymityFlag = "N";
        String donorName = "기증자 명";
        int donateSeq = 29;
        String letterTitle = "편지 제목";
        String letterContents = "편지 내용";

        ArgumentCaptor<HeavenUpdateRequest> captor = ArgumentCaptor.forClass(HeavenUpdateRequest.class);

        /* when & then */
        mockMvc.perform(patch("/heavenLetters/{letterSeq}", letterSeq)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .param("letterWriter", "편지 작성자")
                        .param("anonymityFlag", "N")
                        .param("donorName", "기증자 명")
                        .param("donateSeq", String.valueOf(29))
                        .param("letterTitle", "편지 제목")
                        .param("letterContents", "편지 내용"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시글 수정 성공"))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(heavenService).should(times(1)).updateHeaven(eq(letterSeq), captor.capture());
        HeavenUpdateRequest captureRequest = captor.getValue();

        assertEquals(letterWriter, captureRequest.getLetterWriter());
        assertEquals(anonymityFlag, captureRequest.getAnonymityFlag());
        assertEquals(donorName, captureRequest.getDonorName());
        assertEquals(donateSeq, captureRequest.getDonateSeq());
        assertEquals(letterTitle, captureRequest.getLetterTitle());
        assertEquals(letterContents, captureRequest.getLetterContents());
    }

    @Test
    @DisplayName("게시물 삭제 테스트")
    public void deleteHeavenTest() throws Exception {
        /* given */
        // 매개변수 값 설정
        int letterSeq = 14;
        String letterPasscode = "qwer1234";
        HeavenVerifyRequest heavenVerifyRequest = HeavenVerifyRequest.builder()
                .letterPasscode(letterPasscode)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(heavenVerifyRequest);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        /* when & then */
        mockMvc.perform(delete("/heavenLetters/{letterSeq}", letterSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시글 삭제 성공"))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(heavenService).should(times(1)).deleteHeaven(eq(letterSeq), captor.capture());
        String requestPasscode = captor.getValue();

        assertEquals(letterPasscode, requestPasscode);
    }
}