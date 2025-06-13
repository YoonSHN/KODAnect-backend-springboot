package kodanect.domain.remembrance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.config.EgovConfigCommon;
import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.domain.remembrance.dto.MemorialCommentCreateRequest;
import kodanect.domain.remembrance.dto.MemorialCommentPasswordRequest;
import kodanect.domain.remembrance.dto.MemorialCommentResponse;
import kodanect.domain.remembrance.dto.MemorialCommentUpdateRequest;
import kodanect.domain.remembrance.service.MemorialCommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(MemorialCommentController.class)
@Import(EgovConfigCommon.class)
class MemorialCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemorialCommentService memorialCommentService;

    @Autowired
    private ObjectMapper objectMapper;

    private int donateSeq = 1;
    private int commentSeq = 1;

    private MemorialCommentCreateRequest commentCreateDto;
    private MemorialCommentUpdateRequest commentUpdateDto;
    private MemorialCommentPasswordRequest commentPasswordDto;

    @BeforeEach
    void setupCreate() {
        this.commentCreateDto = MemorialCommentCreateRequest.builder()
                .commentWriter("홍길동")
                .contents("내용")
                .commentPasscode("1234asdf")
                .build();

        this.commentUpdateDto = MemorialCommentUpdateRequest.builder()
                .commentWriter("홍길동")
                .contents("내용")
                .build();

        this.commentPasswordDto = MemorialCommentPasswordRequest.builder()
                .commentPasscode("1234asdf")
                .build();
    }

    @Test
    @DisplayName("추모관 댓글 더보기")
    void 추모관_댓글_더보기() throws Exception {

        /* 게시글 댓글 리스트 */
        List<MemorialCommentResponse> content = List.of(
                MemorialCommentResponse.builder()
                        .commentSeq(1)
                        .commentWriter("홍길동")
                        .contents("안녕하세요")
                        .writeTime(LocalDateTime.of(2024,1,1,12,0,0))
                        .build(),
                MemorialCommentResponse.builder()
                        .commentSeq(2)
                        .commentWriter("김길동")
                        .contents("잘가세요")
                        .writeTime(LocalDateTime.of(2022,1,1,12,0,0))
                        .build()
        );

        CursorCommentPaginationResponse<MemorialCommentResponse, Integer> page =
                CursorCommentPaginationResponse.<MemorialCommentResponse, Integer>builder()
                        .content(content)
                        .commentNextCursor(null)
                        .commentHasNext(false)
                        .build();

        given(memorialCommentService.getMoreCommentList(1, 1, 3)).willReturn(page);

        mockMvc.perform(get("/remembrance/1/comment")
                        .param("cursor", "1")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("댓글 조회 성공"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].commentSeq").value(1))
                .andExpect(jsonPath("$.data.content[0].commentWriter").value("홍길동"))
                .andExpect(jsonPath("$.data.content[1].commentSeq").value(2))
                .andExpect(jsonPath("$.data.content[1].commentWriter").value("김길동"))
                .andExpect(jsonPath("$.data.commentNextCursor").doesNotExist()) // null인 경우
                .andExpect(jsonPath("$.data.commentHasNext").value(false));


    }

    @Test
    @DisplayName("추모관 댓글 생성")
    void 추모관_댓글_생성() throws Exception {
        doNothing().when(memorialCommentService).createComment(donateSeq, commentCreateDto);

        mockMvc.perform(post("/remembrance/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("댓글 등록 성공"));
    }

    @Test
    @DisplayName("추모관 댓글 수정")
    void 추모관_댓글_수정() throws Exception {
        doNothing().when(memorialCommentService).updateComment(donateSeq, commentSeq, commentUpdateDto);

        mockMvc.perform(put("/remembrance/1/comment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("댓글 수정 성공"));
    }

    @Test
    @DisplayName("추모관 댓글 삭제")
    void 추모관_댓글_삭제() throws Exception {
        doNothing().when(memorialCommentService).deleteComment(donateSeq, commentSeq, commentPasswordDto);

        mockMvc.perform(delete("/remembrance/1/comment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentPasswordDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("댓글 삭제 성공"));
    }

    @Test
    @DisplayName("추모관 댓글 비밀번호 검증")
    void 추모관_댓글_비밀번호_검증() throws Exception {
        doNothing().when(memorialCommentService).varifyComment(donateSeq, commentSeq, commentPasswordDto);

        mockMvc.perform(post("/remembrance/1/comment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentPasswordDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("비밀번호 인증 성공"));
    }
}