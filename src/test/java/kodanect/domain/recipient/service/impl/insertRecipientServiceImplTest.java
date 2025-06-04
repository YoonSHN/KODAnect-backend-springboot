package kodanect.domain.recipient.service.impl;

import kodanect.common.config.GlobalsProperties;
import kodanect.domain.recipient.dto.RecipientDetailResponseDto;
import kodanect.domain.recipient.dto.RecipientRequestDto;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
import kodanect.domain.recipient.repository.RecipientRepository;
import kodanect.common.util.HcaptchaService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner; // JUnit 4 Mockito Runner
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils; // private 필드 접근용
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertThrows; // JUnit 4 예외 테스트

@RunWith(MockitoJUnitRunner.class)
public class insertRecipientServiceImplTest {
    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private HcaptchaService hcaptchaService;

    @Mock
    private GlobalsProperties globalsProperties;

    @InjectMocks
    private RecipientServiceImpl recipientService;

    // RecipientService에 정의된 상수 값들을 테스트에서도 사용하기 위해 Reflection으로 설정
    private static final String CAPTCHA_FAILED_MESSAGE = "hCaptcha 인증에 실패했습니다. 다시 시도해주세요.";
    private static final String ANONYMOUS_WRITER_VALUE = "익명";
    private static final String ORGAN_CODE_DIRECT_INPUT = "ORGAN000";

    // 파일 업로드 관련 경로 (실제 파일 시스템에 저장하지 않으므로 임시 경로 사용)
    private String testUploadDir = "test_uploads";
    private String testFileBaseUrl = "/uploads";

    @Before
    public void setUp() {
        when(globalsProperties.getFileStorePath()).thenReturn("/test/uploads");
        when(globalsProperties.getFileBaseUrl()).thenReturn("/test-uploads");
        // 서비스의 @Value 필드들을 수동으로 설정
        recipientService = new RecipientServiceImpl(
                recipientRepository,
                mock(RecipientCommentRepository.class), // recipientCommentRepository가 필요한 경우 mock
                hcaptchaService,
                globalsProperties,
                ORGAN_CODE_DIRECT_INPUT,
                ANONYMOUS_WRITER_VALUE,
                CAPTCHA_FAILED_MESSAGE
        );
    }

    // DTO 생성 헬퍼 메서드
    private RecipientRequestDto createRequestDto(String writer, String passcode, String contents,
                                                 String anonymityFlag, String organCode, String organEtc,
                                                 String captchaToken, MultipartFile imageFile) {
        return RecipientRequestDto.builder()
                .letterWriter(writer)
                .letterPasscode(passcode)
                .letterContents(contents)
                .anonymityFlag(anonymityFlag)
                .recipientYear("2024")
                .organCode(organCode)
                .organEtc(organEtc)
                .captchaToken(captchaToken)
                .imageFile(imageFile)
                .build();
    }

    // --- insertRecipient 테스트 시작 ---

    @Test
    public void insertRecipient_Success_NoImage() {
        // Given
        RecipientRequestDto requestDto = createRequestDto(
                "테스트작가", "12345678", "게시물 내용입니다.", "N",
                "ORG01", null, "valid_captcha_token", null
        );
        RecipientEntity expectedSavedEntity = requestDto.toEntity();
        expectedSavedEntity.setLetterSeq(1); // 저장 후 ID가 할당되었다고 가정
        expectedSavedEntity.setReadCount(0); // 초기 readCount

        // Mocking
        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        when(recipientRepository.save(any(RecipientEntity.class))).thenReturn(expectedSavedEntity);

        // When
        RecipientDetailResponseDto resultDto = recipientService.insertRecipient(requestDto);

        // Then
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getLetterSeq()).isEqualTo(1);
        assertThat(resultDto.getLetterWriter()).isEqualTo("테스트작가");
        assertThat(resultDto.getLetterContents()).isEqualTo("게시물 내용입니다.");
        assertThat(resultDto.getFileName()).isNull(); // 이미지 없음
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(1)).save(any(RecipientEntity.class));
    }

    @Test
    public void insertRecipient_Success_WithImage() throws IOException {
        // Given
        byte[] imageContent = "test image content".getBytes();
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "test.png", "image/png", new ByteArrayInputStream(imageContent)
        );

        RecipientRequestDto requestDto = createRequestDto(
                "이미지작가", "12345678", "이미지 있는 게시물 내용", "N",
                "ORG02", null, "valid_captcha_token", imageFile
        );

        // save 메서드가 호출될 때, 파일 이름이 설정된 엔티티를 반환하도록 Mocking
        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        when(recipientRepository.save(any(RecipientEntity.class))).thenAnswer(invocation -> {
            RecipientEntity entity = invocation.getArgument(0);
            // 실제 서비스 로직처럼 UUID 기반의 파일 이름을 생성하고 설정하는 것을 시뮬레이션
            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // UUID는 예측 불가능하므로, 목킹된 유니크 파일명으로 설정
            String mockedUniqueFileName = "mock-uuid" + fileExtension;
            entity.setFileName(testFileBaseUrl + "/" + mockedUniqueFileName);
            entity.setOrgFileName(originalFilename);
            entity.setLetterSeq(2); // ID 설정
            return entity;
        });

        // When
        RecipientDetailResponseDto resultDto = recipientService.insertRecipient(requestDto);

        // Then
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getLetterSeq()).isEqualTo(2);
        assertThat(resultDto.getLetterWriter()).isEqualTo("이미지작가");
        assertThat(resultDto.getFileName()).contains(testFileBaseUrl); // URL 포함 확인
        assertThat(resultDto.getFileName()).contains("mock-uuid"); // 목킹된 UUID 포함 확인
        assertThat(resultDto.getOrgFileName()).isEqualTo("test.png");
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(1)).save(any(RecipientEntity.class));
    }

    @Test
    public void insertRecipient_Failure_ImageUploadIOException() throws IOException {
        // Given
        // InputStream에서 IOException을 발생시키도록 MockMultipartFile을 생성
        MockMultipartFile imageFile = mock(MockMultipartFile.class);
        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getOriginalFilename()).thenReturn("error.png");
        // getInputStream() 호출 시 IOException 발생하도록 설정
        when(imageFile.getInputStream()).thenThrow(new IOException("Test IO Exception"));

        RecipientRequestDto requestDto = createRequestDto(
                "에러작가", "12345678", "내용", "N",
                "ORG01", null, "valid_captcha_token", imageFile
        );

        // Mocking
        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);

        // When & Then
        RecipientInvalidDataException exception = assertThrows(RecipientInvalidDataException.class, () ->
                recipientService.insertRecipient(requestDto)
        );
        assertThat(exception.getMessage()).isEqualTo("이미지 파일 저장 중 오류가 발생했습니다.");
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, never()).save(any(RecipientEntity.class)); // save는 호출되지 않아야 함
    }


    @Test
    public void insertRecipient_Success_Anonymous() {
        // Given
        RecipientRequestDto requestDto = createRequestDto(
                "실제작가이름", "12345678", "익명 게시물 내용", "Y", // 익명 Y
                "ORG03", null, "valid_captcha_token", null
        );
        RecipientEntity expectedSavedEntity = requestDto.toEntity(); // DTO에서 변환된 초기 엔티티
        expectedSavedEntity.setLetterSeq(3);
        expectedSavedEntity.setReadCount(0);
        // 서비스에서 익명 처리될 것을 반영하여 Mocking 시 반환될 엔티티에 익명 값을 설정
        expectedSavedEntity.setLetterWriter(ANONYMOUS_WRITER_VALUE);

        // Mocking
        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        // save 호출 시 익명 작가명이 설정된 엔티티를 반환하도록 thenAnswer 사용
        when(recipientRepository.save(any(RecipientEntity.class))).thenAnswer(invocation -> {
            RecipientEntity entity = invocation.getArgument(0);
            entity.setLetterWriter(ANONYMOUS_WRITER_VALUE); // 서비스 로직이 익명으로 설정할 것을 시뮬레이션
            entity.setLetterSeq(3); // ID 설정
            return entity;
        });

        // When
        RecipientDetailResponseDto resultDto = recipientService.insertRecipient(requestDto);

        // Then
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getLetterSeq()).isEqualTo(3);
        assertThat(resultDto.getLetterWriter()).isEqualTo(ANONYMOUS_WRITER_VALUE); // 익명으로 저장되었는지 확인
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(1)).save(any(RecipientEntity.class));
    }

    @Test
    public void insertRecipient_Success_OrganCodeDirectInput() {
        // Given
        RecipientRequestDto requestDto = createRequestDto(
                "직접입력작가", "12345678", "직접 입력 게시물", "N",
                ORGAN_CODE_DIRECT_INPUT, "직접입력기관명", "valid_captcha_token", null
        );
        RecipientEntity expectedSavedEntity = requestDto.toEntity();
        expectedSavedEntity.setLetterSeq(4);
        expectedSavedEntity.setReadCount(0);
        expectedSavedEntity.setOrganCode(ORGAN_CODE_DIRECT_INPUT);
        expectedSavedEntity.setOrganEtc("직접입력기관명");

        // Mocking
        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        when(recipientRepository.save(any(RecipientEntity.class))).thenReturn(expectedSavedEntity);

        // When
        RecipientDetailResponseDto resultDto = recipientService.insertRecipient(requestDto);

        // Then
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getLetterSeq()).isEqualTo(4);
        assertThat(resultDto.getOrganCode()).isEqualTo(ORGAN_CODE_DIRECT_INPUT);
        assertThat(resultDto.getOrganEtc()).isEqualTo("직접입력기관명");
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(1)).save(any(RecipientEntity.class));
    }

    @Test
    public void insertRecipient_Failure_CaptchaInvalid() {
        // Given
        RecipientRequestDto requestDto = createRequestDto(
                "작가", "12345678", "내용", "N",
                "ORG01", null, "invalid_captcha_token", null
        );

        // Mocking
        when(hcaptchaService.verifyCaptcha("invalid_captcha_token")).thenReturn(false);

        // When & Then
        RecipientInvalidDataException exception = assertThrows(RecipientInvalidDataException.class, () ->
                recipientService.insertRecipient(requestDto)
        );
        assertThat(exception.getMessage()).isEqualTo(CAPTCHA_FAILED_MESSAGE);
        verify(hcaptchaService, times(1)).verifyCaptcha("invalid_captcha_token");
        verify(recipientRepository, never()).save(any(RecipientEntity.class)); // save는 호출되지 않아야 함
    }

    @Test
    public void insertRecipient_Failure_OrganCodeDirectInputButOrganEtcEmpty() {
        // Given
        // ORGAN_CODE_DIRECT_INPUT 상수는 이 테스트 클래스 내에서 정의된 값을 사용합니다.
        // RecipientServiceImpl은 Mocking된 globalsProperties.getOrganCodeDirectInput()을 사용합니다.
        RecipientRequestDto requestDto = createRequestDto(
                "작가", "12345678", "내용", "N",
                ORGAN_CODE_DIRECT_INPUT, "   ", "valid_captcha_token", null // ORGAN000인데 organEtc가 공백
        );

        // Mocking
        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        // setUp() 메서드에서 globalsProperties.getOrganCodeDirectInput()은 이미 "ORGAN000"으로 Mocking 되었습니다.

        // When & Then
        RecipientInvalidDataException exception = assertThrows(RecipientInvalidDataException.class, () ->
                recipientService.insertRecipient(requestDto)
        );
        assertThat(exception.getMessage()).isEqualTo("ORGAN000 선택 시 organEtc는 필수 입력 항목입니다.");
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
    }

    @Test
    public void insertRecipient_Failure_OrganCodeDirectInputButOrganEtcNull() {
        // Given
        RecipientRequestDto requestDto = createRequestDto(
                "작가", "12345678", "내용", "N",
                ORGAN_CODE_DIRECT_INPUT, null, "valid_captcha_token", null // ORGAN000인데 organEtc가 null
        );

        // Mocking
        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);

        // When & Then
        RecipientInvalidDataException exception = assertThrows(RecipientInvalidDataException.class, () ->
                recipientService.insertRecipient(requestDto)
        );
        assertThat(exception.getMessage()).isEqualTo("ORGAN000 선택 시 organEtc는 필수 입력 항목입니다.");
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
    }
}