//package kodanect.domain.recipient.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import kodanect.domain.recipient.dto.RecipientResponseDto;
//import kodanect.domain.recipient.entity.RecipientEntity;
//import kodanect.domain.recipient.service.RecipientService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.NoSuchElementException;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//
//@WebMvcTest(RecipientController.class)
//public class RecipientControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc; // HTTP 요청을 시뮬레이션하는 객체
//
//    @MockBean // RecipientService를 Mock 객체로 주입합니다. 실제 서비스 빈이 아닌 Mock 객체가 사용됩니다.
//    private RecipientService recipientService;
//
//    @Autowired
//    private ObjectMapper objectMapper; // JSON 직렬화/역직렬화를 위한 유틸리티
//
//    private RecipientEntity testRecipientEntity;
//    private RecipientResponseDto testRecipientDto;
//
//    @BeforeEach
//    void setUp() {
//        // 테스트에 사용할 게시물 엔티티 및 DTO 초기화
//        testRecipientEntity = RecipientEntity.builder()
//                .letterSeq(1)
//                .letterTitle("테스트 제목")
//                .letterWriter("테스트 작성자")
//                .letterPasscode("pass1234")
//                .letterContents("테스트 내용")
//                .anonymityFlag("N")
//                .organCode("TEST001")
//                .recipientYear("2024")
//                .readCount(0)
//                .delFlag("N")
//                .writeTime(LocalDateTime.now())
//                .build();
//
//        testRecipientDto = RecipientResponseDto.builder()
//                .letterSeq(1)
//                .letterTitle("테스트 제목")
//                .letterWriter("테스트 작성자")
//                .letterContents("테스트 내용")
//                .anonymityFlag("N")
//                .organCode("TEST001")
//                .recipientYear("2024")
//                .readCount(0)
//                .commentCount(2) // 댓글 수 예시
//                .delFlag("N")
//                .writeTime(LocalDateTime.now())
//                .build();
//    }
//
//    // --- GET /recipientLetters (게시물 목록 조회) 테스트 ---
//    @Test
//    void search_Success() throws Exception {
//        // Given
//        Pageable pageable = PageRequest.of(0, 20); // 기본 페이지 요청
//        List<RecipientResponseDto> content = Arrays.asList(testRecipientDto);
//        Page<RecipientResponseDto> mockPage = new PageImpl<>(content, pageable, 1);
//
//        given(recipientService.selectRecipientListPaged(any(RecipientEntity.class), any(Pageable.class)))
//                .willReturn(mockPage);
//
//        // When & Then
//        mockMvc.perform(get("/recipientLetters")
//                        .param("page", "0")
//                        .param("size", "20")
//                        .param("sort", "writeTime,desc")
//                        .param("letterTitle", "테스트") // 검색 조건 추가
//                )
//                .andExpect(status().isOk()) // HTTP 200 OK
//                .andExpect(jsonPath("$.content[0].letterSeq").value(testRecipientDto.getLetterSeq()))
//                .andExpect(jsonPath("$.content[0].letterTitle").value(testRecipientDto.getLetterTitle()))
//                .andExpect(jsonPath("$.totalElements").value(1));
//
//        // 서비스 메서드가 올바른 인자로 호출되었는지 검증
//        verify(recipientService, times(1)).selectRecipientListPaged(any(RecipientEntity.class), any(Pageable.class));
//    }
//
//    @Test
//    void search_InternalServerError() throws Exception {
//        // Given
//        given(recipientService.selectRecipientListPaged(any(RecipientEntity.class), any(Pageable.class)))
//                .willThrow(new RuntimeException("데이터베이스 연결 오류")); // 서비스에서 런타임 예외 발생 시뮬레이션
//
//        // When & Then
//        mockMvc.perform(get("/recipientLetters"))
//                .andExpect(status().isInternalServerError()) // HTTP 500 Internal Server Error
//                .andExpect(content().string("")); // 응답 바디가 비어있어야 함
//
//        verify(recipientService, times(1)).selectRecipientListPaged(any(RecipientEntity.class), any(Pageable.class));
//    }
//
//    // --- GET /recipientLetters/new (게시물 등록 페이지 요청) 테스트 ---
//    @Test
//    void writeForm_Success() throws Exception {
//        // When & Then
//        mockMvc.perform(get("/recipientLetters/new"))
//                .andExpect(status().isOk()) // HTTP 200 OK
//                .andExpect(content().string("")); // 응답 바디가 비어있어야 함 (Void)
//    }
//
//    // --- POST /recipientLetters (게시물 등록) 테스트 ---
//    @Test
//    void write_Success() throws Exception {
//        // Given
//        // 실제 요청 본문에 들어갈 Entity (ID는 서비스에서 부여되므로 0 또는 null)
//        RecipientEntity requestEntity = RecipientEntity.builder()
//                .letterTitle("새로운 게시물")
//                .letterWriter("새 작성자")
//                .letterPasscode("newpass123")
//                .letterContents("새로운 내용입니다.")
//                .anonymityFlag("N")
//                .organCode("TEST001")
//                .recipientYear("2024")
//                .build();
//
//        // 서비스가 반환할 DTO (ID가 부여된 상태)
//        RecipientResponseDto createdDto = RecipientResponseDto.builder()
//                .letterSeq(2) // 서비스에서 부여될 시퀀스
//                .letterTitle("새로운 게시물")
//                .letterWriter("새 작성자")
//                .letterContents("새로운 내용입니다.")
//                .anonymityFlag("N")
//                .organCode("TEST001")
//                .recipientYear("2024")
//                .readCount(0)
//                .commentCount(0)
//                .delFlag("N")
//                .writeTime(LocalDateTime.now())
//                .build();
//
//        given(recipientService.insertRecipient(any(RecipientEntity.class)))
//                .willReturn(createdDto);
//
//        // When & Then
//        mockMvc.perform(post("/recipientLetters")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestEntity))) // 요청 바디를 JSON으로 변환
//                .andExpect(status().isCreated()) // HTTP 201 Created
//                .andExpect(jsonPath("$.letterSeq").value(createdDto.getLetterSeq()))
//                .andExpect(jsonPath("$.letterTitle").value(createdDto.getLetterTitle()));
//
//        // 서비스 메서드가 올바른 인자로 호출되었는지 검증
//        verify(recipientService, times(1)).insertRecipient(any(RecipientEntity.class));
//    }
//
//    @Test
//    void write_InternalServerError() throws Exception {
//        // Given
//        RecipientEntity requestEntity = RecipientEntity.builder()
//                .letterTitle("오류 발생 게시물")
//                .letterWriter("오류 발생자")
//                .letterPasscode("errorpass")
//                .letterContents("오류 유발 내용.")
//                .build();
//
//        given(recipientService.insertRecipient(any(RecipientEntity.class)))
//                .willThrow(new Exception("게시물 등록 유효성 검사 실패")); // 서비스에서 일반 예외 발생 시뮬레이션
//
//        // When & Then
//        mockMvc.perform(post("/recipientLetters")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestEntity)))
//                .andExpect(status().isInternalServerError()) // HTTP 500 Internal Server Error
//                .andExpect(content().string("")); // 응답 바디가 비어있어야 함
//
//        verify(recipientService, times(1)).insertRecipient(any(RecipientEntity.class));
//    }
//
//    // --- GET /recipientLetters/{letterSeq} (특정 게시물 조회) 테스트 ---
//    @Test
//    void view_Success() throws Exception {
//        // Given
//        given(recipientService.selectRecipient(anyInt()))
//                .willReturn(testRecipientDto);
//
//        // When & Then
//        mockMvc.perform(get("/recipientLetters/{letterSeq}", testRecipientDto.getLetterSeq()))
//                .andExpect(status().isOk()) // HTTP 200 OK
//                .andExpect(jsonPath("$.letterSeq").value(testRecipientDto.getLetterSeq()))
//                .andExpect(jsonPath("$.letterTitle").value(testRecipientDto.getLetterTitle()));
//
//        verify(recipientService, times(1)).selectRecipient(testRecipientDto.getLetterSeq());
//    }
//
//    @Test
//    void view_NotFound() throws Exception {
//        // Given
//        given(recipientService.selectRecipient(anyInt()))
//                .willThrow(new NoSuchElementException("게시물을 찾을 수 없습니다.")); // 서비스에서 NoSuchElementException 발생 시뮬레이션
//
//        // When & Then
//        mockMvc.perform(get("/recipientLetters/{letterSeq}", 9999))
//                .andExpect(status().isNotFound()) // HTTP 404 Not Found
//                .andExpect(content().string("")); // 응답 바디가 비어있어야 함
//
//        verify(recipientService, times(1)).selectRecipient(9999);
//    }
//
//    @Test
//    void view_InternalServerError() throws Exception {
//        // Given
//        given(recipientService.selectRecipient(anyInt()))
//                .willThrow(new RuntimeException("데이터베이스 오류")); // 서비스에서 런타임 예외 발생 시뮬레이션
//
//        // When & Then
//        mockMvc.perform(get("/recipientLetters/{letterSeq}", 1))
//                .andExpect(status().isInternalServerError()) // HTTP 500 Internal Server Error
//                .andExpect(content().string("")); // 응답 바디가 비어있어야 함
//
//        verify(recipientService, times(1)).selectRecipient(1);
//    }
//
//    // --- POST /recipientLetters/{letterSeq}/verifyPwd (비밀번호 인증) 테스트 ---
//    @Test
//    void verifyPassword_Success() throws Exception {
//        // Given
//        given(recipientService.verifyLetterPassword(anyInt(), anyString()))
//                .willReturn(true);
//
//        // When & Then
//        mockMvc.perform(post("/recipientLetters/{letterSeq}/verifyPwd", testRecipientEntity.getLetterSeq())
//                        .param("letterPasscode", testRecipientEntity.getLetterPasscode()))
//                .andExpect(status().isOk()) // HTTP 200 OK
//                .andExpect(content().string("")); // 응답 바디가 비어있어야 함
//
//        verify(recipientService, times(1)).verifyLetterPassword(testRecipientEntity.getLetterSeq(), testRecipientEntity.getLetterPasscode());
//    }
//
//    @Test
//    void verifyPassword_Unauthorized() throws Exception {
//        // Given
//        given(recipientService.verifyLetterPassword(anyInt(), anyString()))
//                .willReturn(false); // 비밀번호 불일치 시뮬레이션
//
//        // When & Then
//        mockMvc.perform(post("/recipientLetters/{letterSeq}/verifyPwd", testRecipientEntity.getLetterSeq())
//                        .param("letterPasscode", "wrongpass"))
//                .andExpect(status().isUnauthorized()) // HTTP 401 Unauthorized
//                .andExpect(content().string(""));
//
//        verify(recipientService, times(1)).verifyLetterPassword(testRecipientEntity.getLetterSeq(), "wrongpass");
//    }
//
//    @Test
//    void verifyPassword_NotFound() throws Exception {
//        // Given
//        given(recipientService.verifyLetterPassword(anyInt(), anyString()))
//                .willThrow(new NoSuchElementException("게시물을 찾을 수 없습니다.")); // 게시물 없음 시뮬레이션
//
//        // When & Then
//        mockMvc.perform(post("/recipientLetters/{letterSeq}/verifyPwd", 9999)
//                        .param("letterPasscode", "anypass"))
//                .andExpect(status().isNotFound()) // HTTP 404 Not Found
//                .andExpect(content().string(""));
//
//        verify(recipientService, times(1)).verifyLetterPassword(9999, "anypass");
//    }
//
//    @Test
//    void verifyPassword_InternalServerError() throws Exception {
//        // Given
//        given(recipientService.verifyLetterPassword(anyInt(), anyString()))
//                .willThrow(new RuntimeException("서비스 오류")); // 서비스에서 런타임 예외 발생 시뮬레이션
//
//        // When & Then
//        mockMvc.perform(post("/recipientLetters/{letterSeq}/verifyPwd", 1)
//                        .param("letterPasscode", "anypass"))
//                .andExpect(status().isInternalServerError()) // HTTP 500 Internal Server Error
//                .andExpect(content().string(""));
//
//        verify(recipientService, times(1)).verifyLetterPassword(1, "anypass");
//    }
//
//
//    // --- PATCH /recipientLetters/{letterSeq} (게시물 수정) 테스트 ---
//    @Test
//    void edit_Success() throws Exception {
//        // Given
//        RecipientEntity updateRequest = RecipientEntity.builder()
//                .letterSeq(testRecipientEntity.getLetterSeq())
//                .letterTitle("수정된 제목")
//                .letterContents("수정된 내용")
//                .letterPasscode(testRecipientEntity.getLetterPasscode()) // 비밀번호 포함
//                .build();
//
//        RecipientResponseDto updatedDto = RecipientResponseDto.builder()
//                .letterSeq(testRecipientDto.getLetterSeq())
//                .letterTitle("수정된 제목")
//                .letterWriter(testRecipientDto.getLetterWriter())
//                .letterContents("수정된 내용")
//                .anonymityFlag("N")
//                .organCode("TEST001")
//                .recipientYear("2024")
//                .readCount(0)
//                .commentCount(testRecipientDto.getCommentCount())
//                .delFlag("N")
//                .writeTime(testRecipientDto.getWriteTime())
//                .build();
//
//        given(recipientService.updateRecipient(any(RecipientEntity.class), eq(testRecipientEntity.getLetterSeq()), anyString()))
//                .willReturn(updatedDto);
//
//        // When & Then
//        mockMvc.perform(patch("/recipientLetters/{letterSeq}", testRecipientEntity.getLetterSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isOk()) // HTTP 200 OK
//                .andExpect(jsonPath("$.letterTitle").value("수정된 제목"))
//                .andExpect(jsonPath("$.letterContents").value("수정된 내용"));
//
//        verify(recipientService, times(1)).updateRecipient(any(RecipientEntity.class), eq(testRecipientEntity.getLetterSeq()), eq(testRecipientEntity.getLetterPasscode()));
//    }
//
//    @Test
//    void edit_NotFound() throws Exception {
//        // Given
//        RecipientEntity updateRequest = RecipientEntity.builder()
//                .letterSeq(9999)
//                .letterTitle("수정")
//                .letterContents("수정")
//                .letterPasscode("pass")
//                .build();
//
//        given(recipientService.updateRecipient(any(RecipientEntity.class), eq(9999), anyString()))
//                .willThrow(new NoSuchElementException("게시물을 찾을 수 없습니다."));
//
//        // When & Then
//        mockMvc.perform(patch("/recipientLetters/{letterSeq}", 9999)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isNotFound()) // HTTP 404 Not Found
//                .andExpect(content().string(""));
//
//        verify(recipientService, times(1)).updateRecipient(any(RecipientEntity.class), eq(9999), anyString());
//    }
//
//    @Test
//    void edit_BadRequest() throws Exception {
//        // Given
//        RecipientEntity updateRequest = RecipientEntity.builder()
//                .letterSeq(testRecipientEntity.getLetterSeq())
//                .letterTitle("수정")
//                .letterContents("수정")
//                .letterPasscode("wrongpass") // 잘못된 비밀번호
//                .build();
//
//        given(recipientService.updateRecipient(any(RecipientEntity.class), eq(testRecipientEntity.getLetterSeq()), anyString()))
//                .willThrow(new IllegalArgumentException("비밀번호가 일치하지 않습니다.")); // 서비스에서 IllegalArgumentException 발생 시뮬레이션
//
//        // When & Then
//        mockMvc.perform(patch("/recipientLetters/{letterSeq}", testRecipientEntity.getLetterSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isBadRequest()) // HTTP 400 Bad Request (컨트롤러에서 IllegalArgumentException을 400으로 매핑)
//                .andExpect(content().string(""));
//
//        verify(recipientService, times(1)).updateRecipient(any(RecipientEntity.class), eq(testRecipientEntity.getLetterSeq()), anyString());
//    }
//
//    @Test
//    void edit_InternalServerError() throws Exception {
//        // Given
//        RecipientEntity updateRequest = RecipientEntity.builder()
//                .letterSeq(testRecipientEntity.getLetterSeq())
//                .letterTitle("수정")
//                .letterContents("수정")
//                .letterPasscode(testRecipientEntity.getLetterPasscode())
//                .build();
//
//        given(recipientService.updateRecipient(any(RecipientEntity.class), eq(testRecipientEntity.getLetterSeq()), anyString()))
//                .willThrow(new Exception("서비스 오류"));
//
//        // When & Then
//        mockMvc.perform(patch("/recipientLetters/{letterSeq}", testRecipientEntity.getLetterSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateRequest)))
//                .andExpect(status().isInternalServerError()) // HTTP 500 Internal Server Error
//                .andExpect(content().string(""));
//
//        verify(recipientService, times(1)).updateRecipient(any(RecipientEntity.class), eq(testRecipientEntity.getLetterSeq()), anyString());
//    }
//
//    // --- DELETE /recipientLetters/{letterSeq} (게시물 삭제) 테스트 ---
//    @Test
//    void delete_Success() throws Exception {
//        // Given
//        RecipientEntity deleteRequest = RecipientEntity.builder()
//                .letterPasscode(testRecipientEntity.getLetterPasscode()) // 비밀번호 포함
//                .build();
//
//        doNothing().when(recipientService).deleteRecipient(anyInt(), anyString()); // deleteRecipient는 void 반환
//
//        // When & Then
//        mockMvc.perform(delete("/recipientLetters/{letterSeq}", testRecipientEntity.getLetterSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(deleteRequest)))
//                .andExpect(status().isNoContent()) // HTTP 204 No Content
//                .andExpect(content().string(""));
//
//        verify(recipientService, times(1)).deleteRecipient(testRecipientEntity.getLetterSeq(), testRecipientEntity.getLetterPasscode());
//    }
//
//    @Test
//    void delete_NotFound() throws Exception {
//        // Given
//        RecipientEntity deleteRequest = RecipientEntity.builder()
//                .letterPasscode("anypass")
//                .build();
//
//        doThrow(new NoSuchElementException("게시물을 찾을 수 없습니다.")).when(recipientService).deleteRecipient(anyInt(), anyString());
//
//        // When & Then
//        mockMvc.perform(delete("/recipientLetters/{letterSeq}", 9999)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(deleteRequest)))
//                .andExpect(status().isNotFound()) // HTTP 404 Not Found
//                .andExpect(content().string(""));
//
//        verify(recipientService, times(1)).deleteRecipient(9999, "anypass");
//    }
//
//    @Test
//    void delete_Unauthorized() throws Exception {
//        // Given
//        RecipientEntity deleteRequest = RecipientEntity.builder()
//                .letterPasscode("wrongpass")
//                .build();
//
//        doThrow(new IllegalArgumentException("비밀번호가 일치하지 않습니다.")).when(recipientService).deleteRecipient(anyInt(), anyString());
//
//        // When & Then
//        mockMvc.perform(delete("/recipientLetters/{letterSeq}", testRecipientEntity.getLetterSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(deleteRequest)))
//                .andExpect(status().isUnauthorized()) // HTTP 401 Unauthorized
//                .andExpect(content().string(""));
//
//        verify(recipientService, times(1)).deleteRecipient(testRecipientEntity.getLetterSeq(), "wrongpass");
//    }
//
//    @Test
//    void delete_InternalServerError() throws Exception {
//        // Given
//        RecipientEntity deleteRequest = RecipientEntity.builder()
//                .letterPasscode(testRecipientEntity.getLetterPasscode())
//                .build();
//
//        doThrow(new Exception("서비스 오류")).when(recipientService).deleteRecipient(anyInt(), anyString());
//
//        // When & Then
//        mockMvc.perform(delete("/recipientLetters/{letterSeq}", testRecipientEntity.getLetterSeq())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(deleteRequest)))
//                .andExpect(status().isInternalServerError()) // HTTP 500 Internal Server Error
//                .andExpect(content().string(""));
//
//        verify(recipientService, times(1)).deleteRecipient(testRecipientEntity.getLetterSeq(), testRecipientEntity.getLetterPasscode());
//    }
//}