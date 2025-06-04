package kodanect.domain.recipient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.exception.custom.RecipientExceptionHandler;
import kodanect.domain.recipient.dto.*;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
import kodanect.domain.recipient.repository.RecipientRepository;
import kodanect.domain.recipient.service.RecipientService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(
        controllers = {RecipientController.class, RecipientExceptionHandler.class},
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class}
        )
public class RecipientControllerTest {
    @Autowired
    private MockMvc mockMvc; // HTTP 요청을 시뮬레이션하는 객체

    @Autowired
    private ObjectMapper objectMapper; // JSON 직렬화/역직렬화를 위한 객체

    @MockBean
    private RecipientService recipientService;

    @MockBean
    private MessageSourceAccessor messageSourceAccessor; // MessageSourceAccessor Mocking

    @MockBean
    private RecipientRepository recipientRepository;

    @MockBean
    private RecipientCommentRepository recipientCommentRepository;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;


    // @Before 어노테이션은 각 테스트 메서드 실행 전에 초기화 작업을 수행합니다.
    @Before
    public void setup() {
    }

    // --- 게시물 목록 조회 테스트 ---
    @Test
    public void getRecipientList_success() throws Exception {
        // given
        LocalDateTime testTime = LocalDateTime.now();

        RecipientListResponseDto dto1 = RecipientListResponseDto.builder()
                .letterSeq(1)
                .letterTitle("Test Title 1")
                .letterWriter("Writer A")
                .writeTime(testTime)
                .build();

        RecipientListResponseDto dto2 = RecipientListResponseDto.builder()
                .letterSeq(2)
                .letterTitle("Test Title 2")
                .letterWriter("Writer B")
                .writeTime(testTime)
                .build();

        List<RecipientListResponseDto> mockList = Arrays.asList(dto1, dto2);

        // 서비스에 전달될 RecipientSearchCondition 객체를 명시적으로 생성
        // 이 클래스에 @EqualsAndHashCode 어노테이션이 적용되어 있어야 합니다.
        RecipientSearchCondition searchCondition = new RecipientSearchCondition();
        searchCondition.setSearchType(SearchType.TITLE);
        searchCondition.setSearchKeyword("Test");
        ArgumentCaptor<RecipientSearchCondition> captor = ArgumentCaptor.forClass(RecipientSearchCondition.class);

        // Mocking 조건에 생성한 searchCondition 객체를 eq()로 사용
        when(recipientService.selectRecipientList(
                eq(searchCondition),
                eq(null),    // 이렇게 직접 null 처리해도 됨
                eq(10))
        ).thenReturn(mockList);

        // when
        ResultActions actions = mockMvc.perform(get("/recipientLetters")
                .param("searchType", "TITLE")
                .param("searchKeyword", "Test")
                .param("size", "10"));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("게시물 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].letterSeq").value(1))
                .andExpect(jsonPath("$.data[1].letterSeq").value(2))
                .andDo(print());

        // 서비스 메서드 호출 검증
        verify(recipientService).selectRecipientList(eq(searchCondition), eq(null), eq(10));
    }

    @Test
    public void getRecipientList_noContent() throws Exception {
        // given
        when(recipientService.selectRecipientList(any(RecipientSearchCondition.class), any(), anyInt()))
                .thenReturn(Collections.emptyList());

        // when
        ResultActions actions = mockMvc.perform(get("/recipientLetters"));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("게시물 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andDo(print());
        verify(recipientService).selectRecipientList(any(RecipientSearchCondition.class), any(), anyInt());
    }

    // --- 게시판 등록 페이지 요청 테스트 ---
    @Test
    public void writeForm_success() throws Exception {
        // given (서비스 호출 없음)

        // when
        ResultActions actions = mockMvc.perform(get("/recipientLetters/new"));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("게시물 작성 페이지 접근 성공"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    // --- 게시판 등록 테스트 ---
    @Test
    public void write_success() throws Exception {
        // given
        // RecipientRequestDto의 모든 유효성 검사 조건을 만족하도록 필드 값 설정
        RecipientRequestDto requestDto = RecipientRequestDto.builder()
                .organCode("ORGAN001") // @Pattern 만족
                .organEtc(null) // ORGAN000이 아니므로 null 가능
                .letterTitle("새로운 제목") // @NotBlank, @Size 만족
                .recipientYear("2023") // @Pattern, @Min, @Max 만족
                .letterPasscode("Passcode123") // @NotBlank, @Pattern 만족 (영문 숫자 8자 이상)
                .letterWriter("작성자1") // @Size, @NotBlank 만족 (10자 한글 이내)
                .anonymityFlag("N") // @Pattern 만족
                .letterContents("새로운 게시물 내용입니다. 길게 작성합니다.") // @NotBlank 만족
                .captchaToken("validCaptchaToken123") // @NotBlank 만족
                // .imageFile(null) // 파일이 필수가 아니라면 null로 두거나 주석 처리
                .build();

        RecipientDetailResponseDto responseDto = RecipientDetailResponseDto.builder()
                .letterSeq(1)
                .organCode("ORGAN001")
                .letterTitle("새로운 제목")
                .letterContents("새로운 게시물 내용입니다. 길게 작성합니다.")
                .letterWriter("작성자1")
                .recipientYear("2023")
                .anonymityFlag("N")
                .writeTime(LocalDateTime.now()) // DTO 필드명과 타입에 맞춰 설정
                .readCount(0)
                .build();

        // 서비스 Mocking
        when(recipientService.insertRecipient(any(RecipientRequestDto.class))).thenReturn(responseDto);

        // when
        // MockMvc.multipart()를 사용하여 요청을 빌드합니다.
        // 일반 필드는 .param()으로, 파일 필드만 .file()로 보냅니다.
        ResultActions actions = mockMvc.perform(multipart("/recipientLetters")
                // HTTP Method는 POST가 기본이므로 명시할 필요 없음
                // .with(request -> { request.setMethod("POST"); return request; })
                // 일반 폼 필드: .param() 사용
                .param("organCode", requestDto.getOrganCode())
                .param("letterTitle", requestDto.getLetterTitle())
                .param("recipientYear", requestDto.getRecipientYear())
                .param("letterPasscode", requestDto.getLetterPasscode())
                .param("letterWriter", requestDto.getLetterWriter())
                .param("anonymityFlag", requestDto.getAnonymityFlag())
                .param("letterContents", requestDto.getLetterContents())
                .param("captchaToken", requestDto.getCaptchaToken())
                // MultipartFile 타입 필드: .file() 사용
                // imageFile 필드가 있다면 여기 추가. 없다면 불필요
                // .file(new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "dummy image content".getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA) // Content-Type은 multipart()가 자동으로 설정할 수 있지만, 명시적으로 지정
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.message").value("게시물이 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data.letterSeq").value(1))
                .andExpect(jsonPath("$.data.letterTitle").value("새로운 제목")) // responseDto의 값과 일치하도록
                .andExpect(jsonPath("$.data.letterContents").value("새로운 게시물 내용입니다. 길게 작성합니다.")) // 추가
                .andExpect(jsonPath("$.data.letterWriter").value("작성자1")) // 추가
                .andDo(print());

        // 서비스 메서드 호출 검증
        verify(recipientService).insertRecipient(any(RecipientRequestDto.class));
    }

    @Test
    public void write_validationFailure() throws Exception {
        // given (제목이 빈 경우)
        MockMultipartFile titlePart = new MockMultipartFile("letterTitle", "", "text/plain", "".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile contentPart = new MockMultipartFile("letterContents", "", "text/plain", "Content".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile writerPart = new MockMultipartFile("letterWriter", "", "text/plain", "Writer".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile passcodePart = new MockMultipartFile("letterPasscode", "", "text/plain", "passcode1234".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile captchaPart = new MockMultipartFile("captchaToken", "", "text/plain", "captchaResponse".getBytes(StandardCharsets.UTF_8));

        // when
        ResultActions actions = mockMvc.perform(fileUpload("/recipientLetters")
                .file(titlePart)
                .file(contentPart)
                .file(writerPart)
                .file(passcodePart)
                .file(captchaPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
        verifyNoInteractions(recipientService);
    }

    @Test
    public void write_serviceException() throws Exception {
        // given
        // RecipientRequestDto의 모든 유효성 검사 조건을 만족하도록 필드 값 설정
        RecipientRequestDto requestDto = RecipientRequestDto.builder()
                .organCode("ORGAN001")
                .organEtc(null)
                .letterTitle("예외 테스트 제목") // @Size(max=50) 등 유효성 검사 통과하도록
                .recipientYear("2024") // 유효한 연도
                .letterPasscode("1234asdf") // 유효한 비밀번호
                .letterWriter("글쓴이") // @Size(max=10) 등 유효성 검사 통과하도록
                .anonymityFlag("N")
                .letterContents("예외 발생을 위한 게시물 내용입니다.") // @NotBlank 통과하도록
                .captchaToken("dummyCaptchaToken") // 유효성 검사 통과하도록
                .build();

        // 서비스 Mocking: RecipientInvalidDataException 발생
        when(recipientService.insertRecipient(any(RecipientRequestDto.class)))
                .thenThrow(new RecipientInvalidDataException("캡차 인증에 실패했습니다."));

        // when
        ResultActions actions = mockMvc.perform(multipart("/recipientLetters")
                // HTTP Method는 POST가 기본이므로 명시할 필요 없음
                // .with(request -> { request.setMethod("POST"); return request; })
                // 일반 폼 필드: .param() 사용
                .param("organCode", requestDto.getOrganCode())
                .param("letterTitle", requestDto.getLetterTitle())
                .param("recipientYear", requestDto.getRecipientYear())
                .param("letterPasscode", requestDto.getLetterPasscode())
                .param("letterWriter", requestDto.getLetterWriter())
                .param("anonymityFlag", requestDto.getAnonymityFlag())
                .param("letterContents", requestDto.getLetterContents())
                .param("captchaToken", requestDto.getCaptchaToken())
                // MultipartFile 타입 필드: .file() 사용 (필요한 경우)
                // .file(new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", "dummy image content".getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON));

        // then
        // Expected: 400 Bad Request (RecipientInvalidDataException이 400으로 매핑된다고 가정)
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("캡차 인증에 실패했습니다."))
                .andDo(print()); // 응답 전체 출력

        // 서비스 메서드 호출 검증
        verify(recipientService).insertRecipient(any(RecipientRequestDto.class));
    }


    // --- 특정 게시판 조회 테스트 ---
    @Test
    public void view_success() throws Exception {
        // given
        // RecipientDetailResponseDto도 @Builder 패턴으로 변경
        RecipientDetailResponseDto responseDto = RecipientDetailResponseDto.builder()
                .letterSeq(1)
                .letterTitle("View Title")
                .letterContents("View Content") // 필드명 추가
                .letterWriter("View Writer")
                .writeTime(LocalDateTime.now()) // 필드명 변경
                .build();

        when(recipientService.selectRecipient(anyInt())).thenReturn(responseDto);

        // when
        ResultActions actions = mockMvc.perform(get("/recipientLetters/{letterSeq}", 1));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("게시물 조회 성공"))
                .andExpect(jsonPath("$.data.letterSeq").value(1))
                .andExpect(jsonPath("$.data.letterTitle").value("View Title"))
                .andDo(print());
        verify(recipientService).selectRecipient(eq(1));
    }

    @Test
    public void view_notFound() throws Exception {
        // given
        when(recipientService.selectRecipient(anyInt()))
                .thenThrow(new RecipientNotFoundException("게시물을 찾을 수 없습니다."));

        // when
        ResultActions actions = mockMvc.perform(get("/recipientLetters/{letterSeq}", 999));

        // then
        actions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("게시물을 찾을 수 없습니다."))
                .andDo(print());
        verify(recipientService).selectRecipient(eq(999));
    }

    // --- 게시물 수정을 위한 비밀번호 인증 테스트 ---
    @Test
    public void verifyPassword_success() throws Exception {
        // given
        Map<String, String> requestBody = Collections.singletonMap("letterPasscode", "correctPasscode");
        when(recipientService.verifyLetterPassword(anyInt(), anyString())).thenReturn(true);

        // when
        ResultActions actions = mockMvc.perform(post("/recipientLetters/{letterSeq}/verifyPwd", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("비밀번호 확인 결과"))
                .andExpect(jsonPath("$.data").value(true))
                .andDo(print());
        verify(recipientService).verifyLetterPassword(eq(1), eq("correctPasscode"));
    }

    @Test
    public void verifyPassword_failure() throws Exception {
        // given
        Map<String, String> requestBody = Collections.singletonMap("letterPasscode", "wrongPasscode");
        when(recipientService.verifyLetterPassword(anyInt(), anyString())).thenReturn(false);

        // when
        ResultActions actions = mockMvc.perform(post("/recipientLetters/{letterSeq}/verifyPwd", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("비밀번호 확인 결과"))
                .andExpect(jsonPath("$.data").value(false))
                .andDo(print());
        verify(recipientService).verifyLetterPassword(eq(1), eq("wrongPasscode"));
    }

    @Test
    public void verifyPassword_recipientNotFound() throws Exception {
        // given
        Map<String, String> requestBody = Collections.singletonMap("letterPasscode", "anyPasscode");
        when(recipientService.verifyLetterPassword(anyInt(), anyString()))
                .thenThrow(new RecipientNotFoundException("게시물을 찾을 수 없습니다."));

        // when
        ResultActions actions = mockMvc.perform(post("/recipientLetters/{letterSeq}/verifyPwd", 999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));

        // then
        actions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("게시물을 찾을 수 없습니다."))
                .andDo(print());
        verify(recipientService).verifyLetterPassword(eq(999), anyString());
    }

    // --- 게시물 수정 테스트 ---
    @Test
    public void edit_success() throws Exception {
        // given
        Integer letterSeq = 1;
        // RecipientRequestDto의 모든 유효성 검사 조건을 만족하도록 필드 값 설정
        RecipientRequestDto requestDto = RecipientRequestDto.builder()
                .organCode("ORGAN002") // @Pattern 만족 (예: 신장)
                .organEtc(null)       // ORGAN000이 아니므로 null 가능
                .letterTitle("수정된 제목입니다.") // @NotBlank, @Size 만족
                .recipientYear("2021") // @Pattern, @Min, @Max 만족
                .letterPasscode("UpdateP@ss123") // @NotBlank, @Pattern 만족 (영문 숫자 8자 이상)
                .letterWriter("수정자1")     // @Size, @NotBlank 만족 (10자 한글 이내)
                .anonymityFlag("Y")               // @Pattern 만족
                .letterContents("수정된 게시물 내용입니다. 이전 내용을 변경합니다.") // @NotBlank 만족
                .captchaToken("updatedCaptchaToken456") // @NotBlank 만족
                // .imageFile(null) // 파일이 필수가 아니라면 null로 두거나 주석 처리
                .build();

        RecipientDetailResponseDto responseDto = RecipientDetailResponseDto.builder()
                .letterSeq(letterSeq)
                .organCode("ORGAN002")
                .letterTitle("수정된 제목입니다.")
                .letterContents("수정된 게시물 내용입니다. 이전 내용을 변경합니다.")
                .letterWriter("수정자1")
                .recipientYear("2021")
                .anonymityFlag("Y")
                .writeTime(LocalDateTime.now().minusDays(1)) // 생성 시간은 이전으로 가정
                .modifyTime(LocalDateTime.now()) // 수정 시간 추가
                .readCount(10) // 조회수도 적절히 설정
                .build();

        // 서비스 Mocking
        // `updateRecipient` 메서드의 두 번째 인자가 `letterPasscode` (문자열)임을 컨트롤러에서 확인
        when(recipientService.updateRecipient(eq(letterSeq), eq(requestDto.getLetterPasscode()), any(RecipientRequestDto.class)))
                .thenReturn(responseDto);

        // when
        // MockMvc.multipart()를 사용하여 요청을 빌드합니다.
        // 일반 필드는 .param()으로, 파일 필드만 .file()로 보냅니다.
        ResultActions actions = mockMvc.perform(multipart("/recipientLetters/{letterSeq}", letterSeq)
                .with(request -> {
                    request.setMethod("PATCH"); // HTTP 메서드를 PATCH로 설정
                    return request;
                })
                // 일반 폼 필드: .param() 사용
                .param("organCode", requestDto.getOrganCode())
                // .param("organEtc", requestDto.getOrganEtc()) // organCode가 ORGAN000이 아니면 필요 없음.
                .param("letterTitle", requestDto.getLetterTitle())
                .param("recipientYear", requestDto.getRecipientYear())
                .param("letterPasscode", requestDto.getLetterPasscode())
                .param("letterWriter", requestDto.getLetterWriter())
                .param("anonymityFlag", requestDto.getAnonymityFlag())
                .param("letterContents", requestDto.getLetterContents())
                .param("captchaToken", requestDto.getCaptchaToken())
                // MultipartFile 타입 필드: .file() 사용
                // imageFile이 필수라면 실제 MockMultipartFile을 생성하여 추가
                // .file(new MockMultipartFile("imageFile", "updated.jpg", "image/jpeg", "updated image content".getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isOk()) // 200 OK 기대
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("게시물이 성공적으로 수정되었습니다."))
                .andExpect(jsonPath("$.data.letterSeq").value(letterSeq))
                .andExpect(jsonPath("$.data.letterTitle").value("수정된 제목입니다.")) // responseDto의 값과 일치하도록
                .andExpect(jsonPath("$.data.letterContents").value("수정된 게시물 내용입니다. 이전 내용을 변경합니다.")) // 추가 검증
                .andExpect(jsonPath("$.data.letterWriter").value("수정자1")) // 추가 검증
                .andExpect(jsonPath("$.data.recipientYear").value("2021")) // 추가 검증
                .andExpect(jsonPath("$.data.anonymityFlag").value("Y")) // 추가 검증
                .andDo(print()); // 응답 전체 출력 (디버깅에 유용)

        // 서비스 메서드 호출 검증
        verify(recipientService).updateRecipient(eq(letterSeq), eq(requestDto.getLetterPasscode()), any(RecipientRequestDto.class));
    }

    @Test
    public void edit_validationFailure() throws Exception {
        // given (내용이 빈 경우)
        Integer letterSeq = 1;
        MockMultipartFile titlePart = new MockMultipartFile("letterTitle", "", "text/plain", "Title".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile contentPart = new MockMultipartFile("letterContents", "", "text/plain", "".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile writerPart = new MockMultipartFile("letterWriter", "", "text/plain", "Writer".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile passcodePart = new MockMultipartFile("letterPasscode", "", "text/plain", "passcode1234".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile captchaPart = new MockMultipartFile("captchaToken", "", "text/plain", "captchaResponse".getBytes(StandardCharsets.UTF_8));

        // when
        ResultActions actions = mockMvc.perform(fileUpload("/recipientLetters/{letterSeq}", letterSeq)
                .file(titlePart)
                .file(contentPart)
                .file(writerPart)
                .file(passcodePart)
                .file(captchaPart)
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                })
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
        verifyNoInteractions(recipientService);
    }

    @Test
    public void edit_invalidPasscode() throws Exception {
        // given
        Integer letterSeq = 1;

        // RecipientRequestDto의 모든 유효성 검사 조건을 만족하도록 값 설정
        // 특히 'letterWriter'를 10자 이내의 한글 또는 짧은 영문으로 수정
        RecipientRequestDto requestDto = RecipientRequestDto.builder()
                .organCode("ORGAN001")
                .organEtc(null)
                .letterTitle("제목입니다") // 50자 이내 한글
                .recipientYear("2020")
                .letterPasscode("Passcode123") // 영문 숫자 8자 이상
                .letterWriter("홍길동")     // 10자(한글) 이내로 수정 (예: "작성자", "홍길동", "ValidUser")
                .anonymityFlag("N")
                .letterContents("테스트 내용입니다. 길게 작성해 봅니다.")
                .captchaToken("validCaptchaResponseABC")
                .build();

        // 서비스 Mocking
        when(recipientService.updateRecipient(eq(letterSeq), eq(requestDto.getLetterPasscode()), any(RecipientRequestDto.class)))
                .thenThrow(new RecipientInvalidPasscodeException("비밀번호가 일치하지 않습니다."));

        // when
        ResultActions actions = mockMvc.perform(multipart("/recipientLetters/{letterSeq}", letterSeq)
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                })
                // 일반 폼 필드: .param() 사용
                .param("organCode", requestDto.getOrganCode())
                .param("letterTitle", requestDto.getLetterTitle())
                .param("recipientYear", requestDto.getRecipientYear())
                .param("letterPasscode", requestDto.getLetterPasscode())
                .param("letterWriter", requestDto.getLetterWriter()) // 수정된 부분
                .param("anonymityFlag", requestDto.getAnonymityFlag())
                .param("letterContents", requestDto.getLetterContents())
                .param("captchaToken", requestDto.getCaptchaToken())
                // imageFile은 필요에 따라 추가
                // .file(new MockMultipartFile("imageFile", "test.txt", "text/plain", "image content".getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
                .andDo(print());

        // 서비스 메서드 호출 검증
        verify(recipientService).updateRecipient(eq(letterSeq), eq(requestDto.getLetterPasscode()), any(RecipientRequestDto.class));
    }

    // --- 게시물 삭제 테스트 ---
    @Test
    public void delete_success() throws Exception {
        // given
        Integer letterSeq = 1;
        RecipientDeleteRequestDto requestDto = new RecipientDeleteRequestDto("correctPasscode", "captchaResponse");

        doNothing().when(recipientService).deleteRecipient(anyInt(), anyString(), anyString());

        // when
        ResultActions actions = mockMvc.perform(delete("/recipientLetters/{letterSeq}", letterSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("게시물이 성공적으로 삭제되었습니다."))
                .andDo(print());
        verify(recipientService).deleteRecipient(eq(letterSeq), eq(requestDto.getLetterPasscode()), eq(requestDto.getCaptchaToken()));
    }

    @Test
    public void delete_invalidPasscode() throws Exception {
        // given
        Integer letterSeq = 1;
        RecipientDeleteRequestDto requestDto = new RecipientDeleteRequestDto("wrongPasscode", "captchaResponse");

        doThrow(new RecipientInvalidPasscodeException("비밀번호가 일치하지 않습니다."))
                .when(recipientService).deleteRecipient(anyInt(), anyString(), anyString());

        // when
        ResultActions actions = mockMvc.perform(delete("/recipientLetters/{letterSeq}", letterSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        actions.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
                .andDo(print());
        verify(recipientService).deleteRecipient(eq(letterSeq), eq(requestDto.getLetterPasscode()), eq(requestDto.getCaptchaToken()));
    }

    @Test
    public void delete_recipientNotFound() throws Exception {
        // given
        Integer letterSeq = 999;
        RecipientDeleteRequestDto requestDto = new RecipientDeleteRequestDto("anyPasscode", "captchaResponse");

        doThrow(new RecipientNotFoundException("게시물을 찾을 수 없습니다."))
                .when(recipientService).deleteRecipient(anyInt(), anyString(), anyString());

        // when
        ResultActions actions = mockMvc.perform(delete("/recipientLetters/{letterSeq}", letterSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        actions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("게시물을 찾을 수 없습니다."))
                .andDo(print());
        verify(recipientService).deleteRecipient(eq(letterSeq), eq(requestDto.getLetterPasscode()), eq(requestDto.getCaptchaToken()));
    }

    // --- 특정 게시물의 "더보기" 댓글 조회 API 테스트 ---
    @Test
    public void getPaginatedCommentsForRecipient_success() throws Exception {
        // given
        // letterSeq는 컨트롤러의 @PathVariable로 1이 넘어옴.
        Integer letterSeqValue = 1; // 서비스 메서드 호출 시 이 값이 사용될 것임.

        // lastCommentId는 요청에서 생략되었으므로, 컨트롤러 메서드로 null이 전달될 것임.
        Integer lastCommentIdValue = null;

        // size는 요청 파라미터로 "3"이 전달되므로, 컨트롤러 메서드로 3이 전달될 것임.
        int sizeValue = 3;

        RecipientCommentResponseDto comment1 = RecipientCommentResponseDto.builder()
                .commentSeq(101)
                .letterSeq(1)
                .commentContents("Comment A")
                .commentWriter("Writer X")
                .writeTime(LocalDateTime.now())
                .build();
        RecipientCommentResponseDto comment2 = RecipientCommentResponseDto.builder()
                .commentSeq(102)
                .letterSeq(1)
                .commentContents("Comment B")
                .commentWriter("Writer Y")
                .writeTime(LocalDateTime.now())
                .build();
        List<RecipientCommentResponseDto> mockComments = Arrays.asList(comment1, comment2);

        // 서비스 Mocking
        // letterSeq: 1 (int), lastCommentId: null (Integer), size: 3 (int)
        // Mockito는 모든 인자에 매처를 사용해야 합니다.
        // eq(null)은 Integer 타입의 null에 정확히 매칭됩니다.
        when(recipientService.selectPaginatedCommentsForRecipient(
                eq(letterSeqValue),    // letterSeq: int -> eq(1)
                eq(lastCommentIdValue),// lastCommentId: Integer -> eq(null)
                eq(sizeValue)          // size: int -> eq(3)
        )).thenReturn(mockComments);

        // when
        ResultActions actions = mockMvc.perform(get("/recipientLetters/{letterSeq}/comments", letterSeqValue)
                .param("size", String.valueOf(sizeValue))); // int 값을 String으로 변환

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("댓글 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].commentSeq").value(101))
                .andDo(print());

        // 서비스 메서드 호출 검증
        // Mocking 조건과 동일하게 정확한 인자를 eq()로 검증합니다.
        verify(recipientService).selectPaginatedCommentsForRecipient(
                eq(letterSeqValue),
                eq(lastCommentIdValue),
                eq(sizeValue)
        );
    }

    @Test
    public void getPaginatedCommentsForRecipient_noContent() throws Exception {
        // given
        Integer letterSeq = 1;
        when(recipientService.selectPaginatedCommentsForRecipient(anyInt(), any(), anyInt()))
                .thenReturn(Collections.emptyList());

        // when
        ResultActions actions = mockMvc.perform(get("/recipientLetters/{letterSeq}/comments", letterSeq));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("댓글 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andDo(print());
        verify(recipientService).selectPaginatedCommentsForRecipient(eq(letterSeq), any(), anyInt());
    }

    @Test
    public void getPaginatedCommentsForRecipient_recipientNotFound() throws Exception {
        // given
        Integer letterSeq = 999;
        when(recipientService.selectPaginatedCommentsForRecipient(anyInt(), any(), anyInt()))
                .thenThrow(new RecipientNotFoundException("게시물을 찾을 수 없거나 이미 삭제된 게시물입니다."));

        // when
        ResultActions actions = mockMvc.perform(get("/recipientLetters/{letterSeq}/comments", letterSeq));

        // then
        actions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("게시물을 찾을 수 없거나 이미 삭제된 게시물입니다."))
                .andDo(print());
        verify(recipientService).selectPaginatedCommentsForRecipient(eq(letterSeq), any(), anyInt());
    }
}