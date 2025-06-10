package kodanect.domain.remembrance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.config.EgovConfigCommon;
import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.dto.MemorialReplyDeleteRequest;
import kodanect.domain.remembrance.dto.MemorialReplyResponse;
import kodanect.domain.remembrance.dto.MemorialReplyUpdateRequest;
import kodanect.domain.remembrance.service.MemorialReplyService;
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


@WebMvcTest(MemorialReplyController.class)
@Import(EgovConfigCommon.class)
class MemorialReplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemorialReplyService memorialReplyService;

    @Autowired
    private ObjectMapper objectMapper;

    private int donateSeq = 1;
    private int replySeq = 1;

    private MemorialReplyCreateRequest replyCreateDto;
    private MemorialReplyUpdateRequest replyUpdateDto;
    private MemorialReplyDeleteRequest replyDeleteDto;

    @BeforeEach
    void setupCreate() {
        this.replyCreateDto = MemorialReplyCreateRequest.builder()
                .replyWriter("홍길동")
                .replyContents("내용")
                .replyPassword("1234asdf")
                .build();

        this.replyUpdateDto = MemorialReplyUpdateRequest.builder()
                .replyWriter("홍길동")
                .replyPassword("1234asdf")
                .replyContents("내용")
                .build();

        this.replyDeleteDto = MemorialReplyDeleteRequest.builder()
                .replyPassword("1234asdf")
                .build();
    }

    @Test
    @DisplayName("추모관 댓글 더보기")
    void 추모관_댓글_더보기() throws Exception {

        /* 게시글 댓글 리스트 */
        List<MemorialReplyResponse> content = List.of(
                MemorialReplyResponse.builder()
                        .replySeq(1)
                        .replyWriter("홍길동")
                        .replyContents("안녕하세요")
                        .replyWriteTime(LocalDateTime.of(2024,1,1,12,0,0))
                        .build(),
                MemorialReplyResponse.builder()
                        .replySeq(2)
                        .replyWriter("김길동")
                        .replyContents("잘가세요")
                        .replyWriteTime(LocalDateTime.of(2022,1,1,12,0,0))
                        .build()
        );

        CursorReplyPaginationResponse<MemorialReplyResponse, Integer> page =
                CursorReplyPaginationResponse.<MemorialReplyResponse, Integer>builder()
                        .content(content)
                        .replyNextCursor(null)
                        .replyHasNext(false)
                        .build();

        given(memorialReplyService.getMoreReplyList(1, 1, 3)).willReturn(page);

        mockMvc.perform(get("/remembrance/1/replies")
                        .param("cursor", "1")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("댓글 조회 성공"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].replySeq").value(1))
                .andExpect(jsonPath("$.data.content[0].replyWriter").value("홍길동"))
                .andExpect(jsonPath("$.data.content[1].replySeq").value(2))
                .andExpect(jsonPath("$.data.content[1].replyWriter").value("김길동"))
                .andExpect(jsonPath("$.data.replyNextCursor").doesNotExist()) // null인 경우
                .andExpect(jsonPath("$.data.replyHasNext").value(false));


    }

    @Test
    @DisplayName("추모관 댓글 생성")
    void 추모관_댓글_생성() throws Exception {
        doNothing().when(memorialReplyService).createReply(donateSeq, replyCreateDto);

        mockMvc.perform(post("/remembrance/1/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("댓글 등록 성공"));
    }

    @Test
    @DisplayName("추모관 댓글 수정")
    void 추모관_댓글_수정() throws Exception {
        doNothing().when(memorialReplyService).updateReply(donateSeq, replySeq, replyUpdateDto);

        mockMvc.perform(put("/remembrance/1/replies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("댓글 수정 성공"));
    }

    @Test
    @DisplayName("추모관 댓글 삭제")
    void 추모관_댓글_삭제() throws Exception {
        doNothing().when(memorialReplyService).deleteReply(donateSeq, replySeq, replyDeleteDto);

        mockMvc.perform(delete("/remembrance/1/replies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyDeleteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("댓글 삭제 성공"));
    }
}