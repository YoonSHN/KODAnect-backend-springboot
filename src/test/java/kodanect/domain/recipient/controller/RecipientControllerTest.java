package kodanect.domain.recipient.controller;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.recipient.dto.*;
import kodanect.domain.recipient.service.RecipientCommentService;
import kodanect.domain.recipient.service.RecipientService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class RecipientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecipientService recipientService;

    @Mock
    private RecipientCommentService recipientCommentService;

    @InjectMocks
    private RecipientController recipientController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(recipientController)
                .addFilters(new HiddenHttpMethodFilter())  // 필터 등록 꼭 해주기
                .build();
    }

    //    GET /recipientLetters - 게시물 목록 조회 테스트
    @Test
    public void testGetRecipientList_Success() throws Exception {
        RecipientListResponseDto dto = new RecipientListResponseDto();
        dto.setLetterSeq(1);                       // id 값 세팅
        dto.setLetterTitle("테스트 제목");           // 필요한 필드들 세팅

        CursorPaginationResponse<RecipientListResponseDto, Integer> pageResponse =
                new CursorPaginationResponse<>(List.of(dto), 1, true);

        when(recipientService.selectRecipientList(any(), any(), anyInt()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/recipientLetters")
                        .param("lastId", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시물 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].letterSeq").value(1))
                .andExpect(jsonPath("$.data.content[0].letterTitle").value("테스트 제목"));

        verify(recipientService, times(1)).selectRecipientList(any(), any(), anyInt());
    }

    //    POST /recipientLetters - 게시물 등록 테스트 (multipart/form-data)
    @Test
    public void testWrite_Success() throws Exception {
        RecipientDetailResponseDto responseDto = new RecipientDetailResponseDto();
        // 필요한 필드 세팅

        when(recipientService.insertRecipient(any(RecipientRequestDto.class)))
                .thenReturn(responseDto);

        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "test.jpg",
                MediaType.IMAGE_JPEG_VALUE, "dummy image content".getBytes());

        mockMvc.perform(multipart("/recipientLetters")
                        .file(imageFile)
                        .param("letterTitle", "테스트 제목")
                        .param("letterWriter", "테스트작성자")        // 필수값 추가
                        .param("letterPasscode", "abc12345")          // 패턴에 맞는 값으로 변경
                        .param("letterContents", "테스트 내용")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("게시물이 성공적으로 등록되었습니다."));

    }

    //    GET /recipientLetters/{letterSeq} - 특정 게시물 조회
    @Test
    public void testView_Success() throws Exception {
        int letterSeq = 1;
        RecipientDetailResponseDto dto = new RecipientDetailResponseDto();
        // dto 세팅

        when(recipientService.selectRecipient(letterSeq)).thenReturn(dto);

        mockMvc.perform(get("/recipientLetters/{letterSeq}", letterSeq))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시물 조회 성공"))
                .andExpect(jsonPath("$.data").exists());

        verify(recipientService, times(1)).selectRecipient(letterSeq);
    }

    //    POST /recipientLetters/{letterSeq}/verifyPwd - 비밀번호 인증
    @Test
    public void testVerifyPassword_Success() throws Exception {
        int letterSeq = 1;
        String passcode = "1234";

        when(recipientService.verifyLetterPassword(eq(letterSeq), eq(passcode))).thenReturn(true);

        String requestBody = "{\"letterPasscode\":\"1234\"}";

        mockMvc.perform(post("/recipientLetters/{letterSeq}/verifyPwd", letterSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("비밀번호 확인 결과"))
                .andExpect(jsonPath("$.data").value(true));

        verify(recipientService, times(1)).verifyLetterPassword(letterSeq, passcode);
    }

    //    PATCH /recipientLetters/{letterSeq} - 게시물 수정 (multipart/form-data)
    @Test
    public void testEdit_Success() throws Exception {
        int letterSeq = 1;
        RecipientDetailResponseDto updatedDto = new RecipientDetailResponseDto();
        // updatedDto 세팅

        when(recipientService.updateRecipient(anyInt(), anyString(), any(RecipientRequestDto.class)))
                .thenReturn(updatedDto);

        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "update.jpg",
                MediaType.IMAGE_JPEG_VALUE, "update content".getBytes());

        mockMvc.perform(multipart("/recipientLetters/{letterSeq}", letterSeq)
                        .file(imageFile)
                        .param("letterWriter", "테스트작성자")    // 필수값 추가
                        .param("letterPasscode", "abc12345")     // 패턴 맞게 수정
                        .param("letterTitle", "수정 제목")
                        .param("letterContents", "수정 내용")
                        .param("_method", "PATCH")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시물이 성공적으로 수정되었습니다."));

        verify(recipientService, times(1))
                .updateRecipient(eq(letterSeq), anyString(), any(RecipientRequestDto.class));
    }

    //    DELETE /recipientLetters/{letterSeq} - 게시물 삭제
    @Test
    public void testDelete_Success() throws Exception {
        int letterSeq = 1;
        String passcode = "1234";

        RecipientDeleteRequestDto requestDto = new RecipientDeleteRequestDto();
        requestDto.setLetterPasscode(passcode);

        doNothing().when(recipientService).deleteRecipient(letterSeq, passcode);

        String json = "{\"letterPasscode\":\"1234\"}";

        mockMvc.perform(delete("/recipientLetters/{letterSeq}", letterSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시물이 성공적으로 삭제되었습니다."));

        verify(recipientService, times(1)).deleteRecipient(letterSeq, passcode);
    }
}