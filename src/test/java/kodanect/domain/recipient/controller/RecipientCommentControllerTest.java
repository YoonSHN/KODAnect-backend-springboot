//package kodanect.domain.recipient.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
//import kodanect.domain.recipient.entity.RecipientCommentEntity;
//import kodanect.domain.recipient.entity.RecipientEntity;
//import kodanect.domain.recipient.service.RecipientCommentService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//import java.util.NoSuchElementException;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//
//@WebMvcTest(RecipientCommentController.class)
//public class RecipientCommentControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private RecipientCommentService recipientCommentService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private RecipientEntity testParentLetter;
//    private RecipientCommentEntity testCommentEntity;
//    private RecipientCommentResponseDto testCommentDto;
//
//    @BeforeEach
//    void setUp() {
//        // 부모 게시물 엔티티 (Mocking을 위해 필요)
//        testParentLetter = RecipientEntity.builder()
//                .letterSeq(100)
//                .letterTitle("테스트 게시물 제목")
//                .letterWriter("테스트 게시물 작성자")
//                .letterPasscode("boardpass1")
//                .letterContents("테스트 게시물 내용")
//                .delFlag("N")
//                .writeTime(LocalDateTime.now())
//                .build();
//
//        // 테스트 댓글 엔티티 (요청 본문에 사용)
//        testCommentEntity = RecipientCommentEntity.builder()
//                .commentSeq(1)
//                .letter(testParentLetter) // 부모 게시물 참조
//                .commentWriter("댓글작성자")
//                .contents("테스트 댓글 내용")
//                .commentPasscode("comment1234")
//                .delFlag("N")
//                .writeTime(LocalDateTime.now())
//                .build();
//
//        // 테스트 댓글 응답 DTO (서비스에서 반환할 객체)
//        testCommentDto = RecipientCommentResponseDto.builder()
//                .commentSeq(1)
//                .letterSeq(testParentLetter.getLetterSeq())
//                .commentWriter("댓글작성자")
//                .contents("테스트 댓글 내용")
//                .delFlag("N")
//                .writeTime(LocalDateTime.now())
//                // 비밀번호는 DTO에 포함시키지 않음
//                .build();
//    }
//
//    // --- POST /{letterSeq}/comments (댓글 작성) 테스트 ---
//
//    @Test
//    void writeComment_Success() throws Exception {
//        // Given
//        // 서비스가 성공적으로 댓글을 저장하고 DTO를 반환할 것으로 Mocking
//        given(recipientCommentService.insertComment(any(RecipientCommentEntity.class)))
//                .willReturn(testCommentDto);
//
//        // When & Then
//        mockMvc.perform(post("/recipientLetters/{letterSeq}/comments", testParentLetter.getLetterSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(testCommentEntity))) // 요청 본문은 testCommentEntity
//                .andExpect(status().isCreated()) // 201 Created
//                .andExpect(jsonPath("$.commentSeq").value(testCommentDto.getCommentSeq()))
//                .andExpect(jsonPath("$.contents").value(testCommentDto.getContents()));
//
//        // 서비스 메서드가 올바른 인자로 호출되었는지 검증
//        verify(recipientCommentService, times(1)).insertComment(any(RecipientCommentEntity.class));
//    }
//
//    @Test
//    void writeComment_BadRequest_IllegalArgumentException() throws Exception {
//        // Given
//        // 서비스가 IllegalArgumentException을 던질 것으로 Mocking (예: 비밀번호 유효성 실패, 삭제된 게시물)
//        given(recipientCommentService.insertComment(any(RecipientCommentEntity.class)))
//                .willThrow(new IllegalArgumentException("비밀번호는 영문 숫자 8자 이상 이어야 합니다."));
//
//        // When & Then
//        mockMvc.perform(post("/recipientLetters/{letterSeq}/comments", testParentLetter.getLetterSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(testCommentEntity)))
//                .andExpect(status().isBadRequest()) // 400 Bad Request
//                .andExpect(content().string("")); // 응답 바디가 비어있어야 함
//
//        verify(recipientCommentService, times(1)).insertComment(any(RecipientCommentEntity.class));
//    }
//
//    @Test
//    void writeComment_NotFound_NoSuchElementException() throws Exception {
//        // Given
//        // 서비스가 NoSuchElementException을 던질 것으로 Mocking (예: 부모 게시물이 존재하지 않음)
//        given(recipientCommentService.insertComment(any(RecipientCommentEntity.class)))
//                .willThrow(new NoSuchElementException("게시물을 찾을 수 없습니다: 999"));
//
//        // When & Then
//        mockMvc.perform(post("/recipientLetters/{letterSeq}/comments", 999) // 존재하지 않는 게시물 시퀀스
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(testCommentEntity)))
//                .andExpect(status().isNotFound()) // 404 Not Found
//                .andExpect(content().string(""));
//
//        verify(recipientCommentService, times(1)).insertComment(any(RecipientCommentEntity.class));
//    }
//
//    @Test
//    void writeComment_InternalServerError() throws Exception {
//        // Given
//        // 서비스가 일반 Exception을 던질 것으로 Mocking
//        given(recipientCommentService.insertComment(any(RecipientCommentEntity.class)))
//                .willThrow(new RuntimeException("데이터베이스 오류 발생"));
//
//        // When & Then
//        mockMvc.perform(post("/recipientLetters/{letterSeq}/comments", testParentLetter.getLetterSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(testCommentEntity)))
//                .andExpect(status().isInternalServerError()) // 500 Internal Server Error
//                .andExpect(content().string(""));
//
//        verify(recipientCommentService, times(1)).insertComment(any(RecipientCommentEntity.class));
//    }
//
//
//    // --- PUT /{letterSeq}/comments/{commentSeq} (댓글 수정) 테스트 ---
//
//    @Test
//    void updateComment_Success() throws Exception {
//        // Given
//        RecipientCommentEntity updateRequest = RecipientCommentEntity.builder()
//                .contents("수정된 댓글 내용입니다.")
//                .commentPasscode("comment1234") // 비밀번호 포함
//                .build();
//
//        RecipientCommentResponseDto updatedDto = RecipientCommentResponseDto.builder()
//                .commentSeq(testCommentDto.getCommentSeq())
//                .letterSeq(testCommentDto.getLetterSeq())
//                .commentWriter(testCommentDto.getCommentWriter())
//                .contents("수정된 댓글 내용입니다.")
//                .delFlag("N")
//                .writeTime(testCommentDto.getWriteTime())
//                .build();
//
//        given(recipientCommentService.updateComment(any(RecipientCommentEntity.class), anyString()))
//                .willReturn(updatedDto);
//
//        // When & Then
//        mockMvc.perform(put("/recipientLetters/{letterSeq}/comments/{commentSeq}",
//                        testParentLetter.getLetterSeq(), testCommentEntity.getCommentSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isOk()) // 200 OK
//                .andExpect(jsonPath("$.commentSeq").value(updatedDto.getCommentSeq()))
//                .andExpect(jsonPath("$.contents").value(updatedDto.getContents()));
//
//        verify(recipientCommentService, times(1)).updateComment(any(RecipientCommentEntity.class), eq(updateRequest.getCommentPasscode()));
//    }
//
//    @Test
//    void updateComment_BadRequest_MissingFields() throws Exception {
//        // Given
//        // 내용 또는 비밀번호가 누락된 요청
//        RecipientCommentEntity invalidRequest = RecipientCommentEntity.builder()
//                .contents(null) // 내용 누락
//                .commentPasscode("comment1234")
//                .build();
//
//        // When & Then
//        mockMvc.perform(put("/recipientLetters/{letterSeq}/comments/{commentSeq}",
//                        testParentLetter.getLetterSeq(), testCommentEntity.getCommentSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidRequest)))
//                .andExpect(status().isBadRequest()) // 400 Bad Request
//                .andExpect(content().string(""));
//
//        // 서비스 메서드는 호출되지 않아야 함 (컨트롤러 단에서 걸러짐)
//        verify(recipientCommentService, never()).updateComment(any(RecipientCommentEntity.class), anyString());
//    }
//
//
//    @Test
//    void updateComment_Forbidden_PasswordMismatch() throws Exception {
//        // Given
//        RecipientCommentEntity updateRequest = RecipientCommentEntity.builder()
//                .contents("수정 내용")
//                .commentPasscode("wrongpass")
//                .build();
//
//        // 서비스가 비밀번호 불일치로 IllegalArgumentException 던질 것으로 Mocking
//        given(recipientCommentService.updateComment(any(RecipientCommentEntity.class), anyString()))
//                .willThrow(new IllegalArgumentException("비밀번호가 일치하지 않습니다."));
//
//        // When & Then
//        mockMvc.perform(put("/recipientLetters/{letterSeq}/comments/{commentSeq}",
//                        testParentLetter.getLetterSeq(), testCommentEntity.getCommentSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isForbidden()) // 403 Forbidden
//                .andExpect(content().string(""));
//
//        verify(recipientCommentService, times(1)).updateComment(any(RecipientCommentEntity.class), eq("wrongpass"));
//    }
//
//    @Test
//    void updateComment_NotFound_CommentNotFound() throws Exception {
//        // Given
//        RecipientCommentEntity updateRequest = RecipientCommentEntity.builder()
//                .contents("수정 내용")
//                .commentPasscode("comment1234")
//                .build();
//
//        // 서비스가 댓글을 찾을 수 없거나 삭제되었을 때 IllegalArgumentException 던질 것으로 Mocking
//        given(recipientCommentService.updateComment(any(RecipientCommentEntity.class), anyString()))
//                .willThrow(new IllegalArgumentException("댓글을 찾을 수 없거나 이미 삭제되었습니다."));
//
//        // When & Then
//        mockMvc.perform(put("/recipientLetters/{letterSeq}/comments/{commentSeq}",
//                        testParentLetter.getLetterSeq(), 999) // 존재하지 않는 commentSeq
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isNotFound()) // 404 Not Found
//                .andExpect(content().string(""));
//
//        verify(recipientCommentService, times(1)).updateComment(any(RecipientCommentEntity.class), eq("comment1234"));
//    }
//
//    @Test
//    void updateComment_BadRequest_GenericIllegalArgument() throws Exception {
//        // Given
//        RecipientCommentEntity updateRequest = RecipientCommentEntity.builder()
//                .contents("수정 내용")
//                .commentPasscode("comment1234")
//                .build();
//
//        // 서비스가 다른 일반적인 IllegalArgumentException 던질 것으로 Mocking
//        given(recipientCommentService.updateComment(any(RecipientCommentEntity.class), anyString()))
//                .willThrow(new IllegalArgumentException("유효하지 않은 댓글 내용입니다."));
//
//        // When & Then
//        mockMvc.perform(put("/recipientLetters/{letterSeq}/comments/{commentSeq}",
//                        testParentLetter.getLetterSeq(), testCommentEntity.getCommentSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isBadRequest()) // 400 Bad Request
//                .andExpect(content().string(""));
//
//        verify(recipientCommentService, times(1)).updateComment(any(RecipientCommentEntity.class), eq("comment1234"));
//    }
//
//    @Test
//    void updateComment_InternalServerError() throws Exception {
//        // Given
//        RecipientCommentEntity updateRequest = RecipientCommentEntity.builder()
//                .contents("수정 내용")
//                .commentPasscode("comment1234")
//                .build();
//
//        // 서비스가 일반 Exception 던질 것으로 Mocking
//        given(recipientCommentService.updateComment(any(RecipientCommentEntity.class), anyString()))
//                .willThrow(new RuntimeException("데이터베이스 연결 오류"));
//
//        // When & Then
//        mockMvc.perform(put("/recipientLetters/{letterSeq}/comments/{commentSeq}",
//                        testParentLetter.getLetterSeq(), testCommentEntity.getCommentSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isInternalServerError()) // 500 Internal Server Error
//                .andExpect(content().string(""));
//
//        verify(recipientCommentService, times(1)).updateComment(any(RecipientCommentEntity.class), eq("comment1234"));
//    }
//
//
//    // --- DELETE /{letterSeq}/comments/{commentSeq} (댓글 삭제) 테스트 ---
//
//    @Test
//    void deleteComment_Success() throws Exception {
//        // Given
//        RecipientCommentEntity deleteRequest = RecipientCommentEntity.builder()
//                .commentPasscode("comment1234") // 삭제 비밀번호
//                .build();
//
//        // 서비스의 deleteComment가 성공적으로 실행되도록 Mocking
//        doNothing().when(recipientCommentService).deleteComment(anyInt(), anyString());
//
//        // When & Then
//        mockMvc.perform(delete("/recipientLetters/{letterSeq}/comments/{commentSeq}",
//                        testParentLetter.getLetterSeq(), testCommentEntity.getCommentSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(deleteRequest)))
//                .andExpect(status().isNoContent()) // 204 No Content
//                .andExpect(content().string(""));
//
//        verify(recipientCommentService, times(1)).deleteComment(testCommentEntity.getCommentSeq(), deleteRequest.getCommentPasscode());
//    }
//
//    @Test
//    void deleteComment_BadRequest_MissingPasscode() throws Exception {
//        // Given
//        RecipientCommentEntity deleteRequest = RecipientCommentEntity.builder()
//                .commentPasscode(null) // 비밀번호 누락
//                .build();
//
//        // When & Then
//        mockMvc.perform(delete("/recipientLetters/{letterSeq}/comments/{commentSeq}",
//                        testParentLetter.getLetterSeq(), testCommentEntity.getCommentSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(deleteRequest)))
//                .andExpect(status().isBadRequest()) // 400 Bad Request
//                .andExpect(content().string(""));
//
//        // 서비스 메서드는 호출되지 않아야 함
//        verify(recipientCommentService, never()).deleteComment(anyInt(), anyString());
//    }
//
//    @Test
//    void deleteComment_Forbidden_PasswordMismatch() throws Exception {
//        // Given
//        RecipientCommentEntity deleteRequest = RecipientCommentEntity.builder()
//                .commentPasscode("wrongpass")
//                .build();
//
//        // 서비스가 비밀번호 불일치로 IllegalArgumentException 던질 것으로 Mocking
//        doThrow(new IllegalArgumentException("비밀번호가 일치하지 않습니다.")).when(recipientCommentService).deleteComment(anyInt(), anyString());
//
//        // When & Then
//        mockMvc.perform(delete("/recipientLetters/{letterSeq}/comments/{commentSeq}",
//                        testParentLetter.getLetterSeq(), testCommentEntity.getCommentSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(deleteRequest)))
//                .andExpect(status().isForbidden()) // 403 Forbidden
//                .andExpect(content().string(""));
//
//        verify(recipientCommentService, times(1)).deleteComment(testCommentEntity.getCommentSeq(), "wrongpass");
//    }
//
//    @Test
//    void deleteComment_NotFound_CommentNotFound() throws Exception {
//        // Given
//        RecipientCommentEntity deleteRequest = RecipientCommentEntity.builder()
//                .commentPasscode("comment1234")
//                .build();
//
//        // 서비스가 댓글을 찾을 수 없거나 삭제되었을 때 IllegalArgumentException 던질 것으로 Mocking
//        doThrow(new IllegalArgumentException("댓글을 찾을 수 없거나 이미 삭제되었습니다.")).when(recipientCommentService).deleteComment(anyInt(), anyString());
//
//        // When & Then
//        mockMvc.perform(delete("/recipientLetters/{letterSeq}/comments/{commentSeq}",
//                        testParentLetter.getLetterSeq(), 999) // 존재하지 않는 commentSeq
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(deleteRequest)))
//                .andExpect(status().isNotFound()) // 404 Not Found
//                .andExpect(content().string(""));
//
//        verify(recipientCommentService, times(1)).deleteComment(999, "comment1234");
//    }
//
//    @Test
//    void deleteComment_BadRequest_GenericIllegalArgument() throws Exception {
//        // Given
//        RecipientCommentEntity deleteRequest = RecipientCommentEntity.builder()
//                .commentPasscode("comment1234")
//                .build();
//
//        // 서비스가 다른 일반적인 IllegalArgumentException 던질 것으로 Mocking
//        doThrow(new IllegalArgumentException("기타 유효성 오류")).when(recipientCommentService).deleteComment(anyInt(), anyString());
//
//        // When & Then
//        mockMvc.perform(delete("/recipientLetters/{letterSeq}/comments/{commentSeq}",
//                        testParentLetter.getLetterSeq(), testCommentEntity.getCommentSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(deleteRequest)))
//                .andExpect(status().isBadRequest()) // 400 Bad Request
//                .andExpect(content().string(""));
//
//        verify(recipientCommentService, times(1)).deleteComment(testCommentEntity.getCommentSeq(), "comment1234");
//    }
//
//    @Test
//    void deleteComment_InternalServerError() throws Exception {
//        // Given
//        RecipientCommentEntity deleteRequest = RecipientCommentEntity.builder()
//                .commentPasscode("comment1234")
//                .build();
//
//        // 서비스가 일반 Exception 던질 것으로 Mocking
//        doThrow(new RuntimeException("예상치 못한 서버 오류")).when(recipientCommentService).deleteComment(anyInt(), anyString());
//
//        // When & Then
//        mockMvc.perform(delete("/recipientLetters/{letterSeq}/comments/{commentSeq}",
//                        testParentLetter.getLetterSeq(), testCommentEntity.getCommentSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(deleteRequest)))
//                .andExpect(status().isInternalServerError()) // 500 Internal Server Error
//                .andExpect(content().string(""));
//
//        verify(recipientCommentService, times(1)).deleteComment(testCommentEntity.getCommentSeq(), "comment1234");
//    }
//}