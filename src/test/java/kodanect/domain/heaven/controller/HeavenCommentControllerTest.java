package kodanect.domain.heaven.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.domain.heaven.dto.request.HeavenCommentCreateRequest;
import kodanect.domain.heaven.dto.request.HeavenCommentUpdateRequest;
import kodanect.domain.heaven.dto.request.HeavenCommentVerifyRequest;
import kodanect.domain.heaven.dto.response.HeavenCommentResponse;
import kodanect.domain.heaven.service.HeavenCommentService;
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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@WebMvcTest(HeavenCommentController.class)
public class HeavenCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HeavenCommentService heavenCommentService;
    @MockBean
    private MessageSourceAccessor messageSourceAccessor;

    @Before
    public void before() {
        given(messageSourceAccessor.getMessage("board.comment.read.success")).willReturn("댓글 조회 성공");
        given(messageSourceAccessor.getMessage("board.comment.create.success")).willReturn("댓글 등록 성공");
        given(messageSourceAccessor.getMessage("board.comment.update.success")).willReturn("댓글 수정 성공");
        given(messageSourceAccessor.getMessage("board.comment.delete.success")).willReturn("댓글 삭제 성공");
    }

    @Test
    @DisplayName("댓글 더보기 테스트")
    public void getMoreCommentListTest() throws Exception {
        /* given */
        // 매개변수 값 설정
        int letterSeq = 14;

        // heavenCommentService.getMoreCommentList 결과 설정
        String commentWriter = "댓글 작성자";
        String contents = "댓글 내용";
        LocalDateTime writeTime = LocalDateTime.now();

        Integer commentNextCursor = 5;
        boolean commentHasNext = true;

        List<HeavenCommentResponse> heavenCommentResponseList = new ArrayList<>();
        for (int i = 7; i >= 1; i--) {
            heavenCommentResponseList.add(new HeavenCommentResponse(i, commentWriter, contents, writeTime));
        }
        CursorCommentPaginationResponse<HeavenCommentResponse, Integer> cursorCommentPaginationResponse = CursorCommentPaginationResponse.<HeavenCommentResponse, Integer>builder()
                .content(heavenCommentResponseList)
                .commentNextCursor(commentNextCursor)
                .commentHasNext(commentHasNext)
                .build();

        given(heavenCommentService.getMoreCommentList(letterSeq, 8, 3)).willReturn(cursorCommentPaginationResponse);

        /* when & then */
        mockMvc.perform(get("/heavenLetters/{letterSeq}/comments", letterSeq)
                        .param("cursor", "8")
                        .param("size", "3"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("댓글 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].commentSeq").value(7))
                .andExpect(jsonPath("$.data.content[0].commentWriter").value(commentWriter))
                .andExpect(jsonPath("$.data.content[0].contents").value(contents))
                .andExpect(jsonPath("$.data.content[0].writeTime").value(writeTime.toLocalDate().toString()))
                .andExpect(jsonPath("$.data.commentNextCursor").value(commentNextCursor))
                .andExpect(jsonPath("$.data.commentHasNext").value(commentHasNext));
    }

    @Test
    @DisplayName("댓글 등록 테스트")
    public void createHeavenCommentTest() throws Exception {
        /* given */
        int letterSeq = 14;
        String commentWriter = "댓글 작성자";
        String commentPasscode = "qwer1234";
        String contents = "댓글 내용";

        HeavenCommentCreateRequest heavenCommentCreateRequest = HeavenCommentCreateRequest.builder()
                .commentWriter(commentWriter)
                .commentPasscode(commentPasscode)
                .contents(contents)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(heavenCommentCreateRequest);
        ArgumentCaptor<HeavenCommentCreateRequest> captor = ArgumentCaptor.forClass(HeavenCommentCreateRequest.class);

        /* when & then */
        mockMvc.perform(post("/heavenLetters/{letterSeq}/comments", letterSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("댓글 등록 성공"))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(heavenCommentService).should(times(1)).createHeavenComment(eq(letterSeq), captor.capture());
        HeavenCommentCreateRequest captureRequest = captor.getValue();

        assertEquals(commentWriter, captureRequest.getCommentWriter());
        assertEquals(commentPasscode, captureRequest.getCommentPasscode());
        assertEquals(contents, captureRequest.getContents());
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    public void updateHeavenCommentTest() throws Exception {
        /* given */
        // 매개변수 값 설정
        int letterSeq = 14;
        int commentSeq = 29;
        String commentWriter = "댓글 작성자";
        String contents = "댓글 내용";

        HeavenCommentUpdateRequest heavenCommentUpdateRequest = HeavenCommentUpdateRequest.builder()
                .commentWriter(commentWriter)
                .contents(contents)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(heavenCommentUpdateRequest);
        ArgumentCaptor<HeavenCommentUpdateRequest> captor = ArgumentCaptor.forClass(HeavenCommentUpdateRequest.class);

        /* when & then */
        mockMvc.perform(put("/heavenLetters/{letterSeq}/comments/{commentSeq}", letterSeq, commentSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("댓글 수정 성공"))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(heavenCommentService).should(times(1)).updateHeavenComment(eq(letterSeq), eq(commentSeq), captor.capture());
        HeavenCommentUpdateRequest captureRequest = captor.getValue();

        assertEquals(commentWriter, captureRequest.getCommentWriter());
        assertEquals(contents, captureRequest.getContents());
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    public void deleteHeavenCommentTest() throws Exception {
        /* given */
        // 매개변수 값 설정
        int letterSeq = 14;
        int commentSeq = 29;
        String commentPasscode = "qwer1234";
        HeavenCommentVerifyRequest heavenCommentVerifyRequest = HeavenCommentVerifyRequest.builder()
                .commentPasscode(commentPasscode)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(heavenCommentVerifyRequest);
        ArgumentCaptor<HeavenCommentVerifyRequest> captor = ArgumentCaptor.forClass(HeavenCommentVerifyRequest.class);

        /* when & then */
        mockMvc.perform(delete("/heavenLetters/{letterSeq}/comments/{commentSeq}", letterSeq, commentSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("댓글 삭제 성공"))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(heavenCommentService).should(times(1)).deleteHeavenComment(eq(letterSeq), eq(commentSeq), captor.capture());
        HeavenCommentVerifyRequest captureRequest = captor.getValue();

        assertEquals(commentPasscode, captureRequest.getCommentPasscode());
    }
}