package kodanect.domain.recipient.controller;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.recipient.dto.*;
import kodanect.domain.recipient.service.RecipientCommentService;
import kodanect.domain.recipient.service.RecipientService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import java.time.LocalDateTime;
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

        // CursorPaginationResponse의 목(Mock) 객체를 생성합니다.
        // 이렇게 하면 실제 클래스의 생성자 접근 권한에 구애받지 않습니다.
        // Mockito.mock()을 사용하면 됩니다.
        @SuppressWarnings("unchecked") // 타입 안전성 경고를 무시합니다.
        CursorPaginationResponse<RecipientListResponseDto, Integer> mockPageResponse =
                Mockito.mock(CursorPaginationResponse.class);

        // 목 객체의 getter 메서드들이 우리가 원하는 값을 반환하도록 설정합니다.
        // Mockito.when()을 사용합니다.
        when(mockPageResponse.getContent()).thenReturn(List.of(dto));
        when(mockPageResponse.getNextCursor()).thenReturn(1);
        when(mockPageResponse.isHasNext()).thenReturn(true);
        when(mockPageResponse.getTotalCount()).thenReturn(1L); // totalCount도 설정

        // recipientService.selectRecipientList가 이 목 객체를 반환하도록 설정합니다.
        when(recipientService.selectRecipientList(any(), any(), anyInt()))
                .thenReturn(mockPageResponse); // 생성된 목 객체를 반환하도록 변경

        mockMvc.perform(get("/recipientLetters")
                        .param("lastId", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("게시물 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].letterSeq").value(1))
                .andExpect(jsonPath("$.data.content[0].letterTitle").value("테스트 제목"))
                .andExpect(jsonPath("$.data.totalCount").value(1));

        verify(recipientService, times(1)).selectRecipientList(any(), any(), anyInt());
    }

    //    POST /recipientLetters - 게시물 등록 테스트 (multipart/form-data)
    @Test
    public void testWrite_Success() throws Exception {
        Integer letterSeq = 1;
        RecipientDetailResponseDto responseDto = new RecipientDetailResponseDto();
        responseDto.setLetterSeq(letterSeq);
        responseDto.setLetterTitle("테스트 게시물 제목");
        responseDto.setLetterContents("테스트 게시물 내용입니다.");
        responseDto.setWriteTime(LocalDateTime.now());
        responseDto.setModifyTime(LocalDateTime.now());
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
        Integer letterSeq = 1;
        // dto 세팅
        RecipientDetailResponseDto dto = new RecipientDetailResponseDto();
        dto.setLetterSeq(letterSeq);
        dto.setLetterTitle("테스트 게시물 제목");
        dto.setLetterContents("테스트 게시물 내용입니다.");
        dto.setWriteTime(LocalDateTime.now());
        dto.setModifyTime(LocalDateTime.now());

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
    @DisplayName("게시물 비밀번호 확인 성공 테스트")
    public void testVerifyPassword_Success() throws Exception {
        Integer letterSeq = 1;
        String passcode = "abc12345";

        // verifyLetterPassword가 void를 반환하므로 doNothing()을 사용
        doNothing().when(recipientService).verifyLetterPassword(letterSeq, passcode);
        String requestBody = "{\"letterPasscode\":\"" + passcode + "\"}";

        mockMvc.perform(post("/recipientLetters/{letterSeq}/verifyPwd", letterSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("비밀번호 확인"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(recipientService, times(1)).verifyLetterPassword(letterSeq, passcode);
    }


    // PATCH /recipientLetters/{letterSeq} - 게시물 수정 (multipart/form-data)
    @Test
    @DisplayName("게시물 수정 성공 테스트") // DisplayName 추가
    public void testEdit_Success() throws Exception {
        Integer letterSeq = 1;

        // Mock Service 응답 DTO 생성
        RecipientDetailResponseDto updatedDto = new RecipientDetailResponseDto();
        updatedDto.setLetterSeq(letterSeq);
        updatedDto.setOrganCode("ORGAN001");
        updatedDto.setLetterTitle("수정 제목");
        updatedDto.setRecipientYear("2023");
        updatedDto.setLetterWriter("수정 작성자");
        updatedDto.setAnonymityFlag("N");
        updatedDto.setReadCount(1);
        updatedDto.setLetterContents("수정 내용");
        updatedDto.setFileName("updated_file.jpg"); // 이미지 파일명도 설정
        updatedDto.setOrgFileName("updated_original.jpg");
        updatedDto.setWriteTime(LocalDateTime.now().minusDays(1));
        updatedDto.setModifierId("modifier");
        updatedDto.setModifyTime(LocalDateTime.now());
        updatedDto.setDelFlag("N");
        updatedDto.setCommentCount(0);
        updatedDto.setHasMoreComments(false);
        updatedDto.setImageUrl("/uploads/updated_file.jpg");


        // recipientService.updateRecipient 호출 시 updatedDto 반환하도록 스터빙
        when(recipientService.updateRecipient(anyInt(), any(RecipientRequestDto.class)))
                .thenReturn(updatedDto);

        mockMvc.perform(multipart("/recipientLetters/{letterSeq}", letterSeq)
                        // .file(imageFile) // 이제 imageFile 직접 전송하지 않음
                        .param("organCode", "ORGAN001") // 필수값 추가
                        .param("letterTitle", "수정 제목")
                        .param("recipientYear", "2023") // 필수값 추가
                        .param("letterWriter", "테스트작성자")
                        .param("anonymityFlag", "N") // 필수값 추가
                        .param("letterContents", "수정 내용")
                        .param("letterPasscode", "newPass1234") // ⭐ 누락된 비밀번호 필드 추가 (영숫자 8자 이상)
                        .param("fileName", "new_image_file.jpg") // CKEditor API로 업로드된 파일명
                        .param("orgFileName", "original_image_name.jpg") // CKEditor API로 업로드된 원본 파일명
                        .param("_method", "PATCH") // HTTP PATCH 요청을 시뮬레이션하기 위한 HiddenHttpMethodFilter 설정
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk()) // 200 OK 상태 검증
                .andExpect(jsonPath("$.success").value(true)) // success 필드 검증
                .andExpect(jsonPath("$.message").value("게시물이 성공적으로 수정되었습니다.")) // 메시지 검증
                .andExpect(jsonPath("$.data.letterSeq").value(letterSeq)) // 반환된 DTO 데이터 검증
                .andExpect(jsonPath("$.data.letterTitle").value("수정 제목"))
                .andExpect(jsonPath("$.data.letterContents").value("수정 내용"))
                .andExpect(jsonPath("$.data.fileName").value("updated_file.jpg")) // 업데이트된 파일명 검증
                .andExpect(jsonPath("$.data.imageUrl").value("/uploads/updated_file.jpg")); // 이미지 URL 검증

        // recipientService.updateRecipient가 올바른 인자로 1번 호출되었는지 검증
        verify(recipientService, times(1))
                .updateRecipient(eq(letterSeq), any(RecipientRequestDto.class));
    }

    //    DELETE /recipientLetters/{letterSeq} - 게시물 삭제
    @Test
    public void testDelete_Success() throws Exception {
        Integer letterSeq = 1;
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