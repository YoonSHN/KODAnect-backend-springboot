//package kodanect.domain.remembrance.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import kodanect.common.config.EgovConfigCommon;
//import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
//import kodanect.domain.remembrance.dto.MemorialReplyDeleteRequest;
//import kodanect.domain.remembrance.dto.MemorialReplyUpdateRequest;
//import kodanect.domain.remembrance.service.MemorialReplyService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.http.MediaType;
//
//import static org.mockito.Mockito.doNothing;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//
//@WebMvcTest(MemorialReplyController.class)
//@Import(EgovConfigCommon.class)
//class MemorialReplyControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private MemorialReplyService memorialReplyService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private int donateSeq = 1;
//    private int replySeq = 1;
//
//    private MemorialReplyCreateRequest replyCreateDto;
//    private MemorialReplyUpdateRequest replyUpdateDto;
//    private MemorialReplyDeleteRequest replyDeleteDto;
//
//    @BeforeEach
//    void setupCreate() {
//        this.replyCreateDto = MemorialReplyCreateRequest.builder()
//                .donateSeq(1)
//                .replyWriter("홍길동")
//                .replyContents("내용")
//                .build();
//
//        this.replyUpdateDto = MemorialReplyUpdateRequest.builder()
//                .donateSeq(1)
//                .replySeq(1)
//                .replyPassword("1234")
//                .replyContents("내용")
//                .build();
//
//        this.replyDeleteDto = MemorialReplyDeleteRequest.builder()
//                .donateSeq(1)
//                .replySeq(1)
//                .replyPassword("1234")
//                .build();
//    }
//
//    @Test
//    @DisplayName("댓글 생성 성공")
//    void createMemorialReply() throws Exception {
//        doNothing().when(memorialReplyService).createReply(donateSeq, replyCreateDto);
//
//        mockMvc.perform(post("/remembrance/1/replies")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(replyCreateDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.message").value("댓글 등록 성공"));
//    }
//
//    @Test
//    @DisplayName("댓글 수정 성공")
//    void updateMemorialReply() throws Exception {
//        doNothing().when(memorialReplyService).updateReply(donateSeq, replySeq, replyUpdateDto);
//
//        mockMvc.perform(put("/remembrance/1/replies/1")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(replyUpdateDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.message").value("댓글 수정 성공"));
//    }
//
//    @Test
//    @DisplayName("댓글 삭제 성공")
//    void deleteMemorialReply() throws Exception {
//        doNothing().when(memorialReplyService).deleteReply(donateSeq, replySeq, replyDeleteDto);
//
//        mockMvc.perform(delete("/remembrance/1/replies/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(replyDeleteDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.message").value("댓글 삭제 성공"));
//    }
//}