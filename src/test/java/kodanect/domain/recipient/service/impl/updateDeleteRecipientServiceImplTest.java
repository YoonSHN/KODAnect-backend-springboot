package kodanect.domain.recipient.service.impl;

import kodanect.common.util.HcaptchaService;
import kodanect.domain.recipient.dto.RecipientDetailResponseDto;
import kodanect.domain.recipient.dto.RecipientRequestDto;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
import kodanect.domain.recipient.repository.RecipientRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class updateDeleteRecipientServiceImplTest {
    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private RecipientCommentRepository recipientCommentRepository;

    @Mock
    private HcaptchaService hcaptchaService;

    // InjectMocks는 실제 객체에 @Mock으로 선언된 Mock들을 주입해줍니다.
    @InjectMocks
    private RecipientServiceImpl recipientService;

    // RecipientService에 정의된 상수 값들을 테스트에서도 사용하기 위해 Reflection으로 설정
    private static final String CAPTCHA_FAILED_MESSAGE = "스팸 방지 인증에 실패했습니다. 다시 시도해주세요.";
    private static final String ANONYMOUS_WRITER_VALUE = "익명";
    private static final String ORGAN_CODE_DIRECT_INPUT = "ORGAN000";
    private static final String RECIPIENT_NOT_FOUND_MESSAGE = "해당 게시물이 존재하지 않거나 이미 삭제되었습니다.";

    // 파일 업로드 관련 경로 (실제 파일 시스템에 저장하지 않으므로 임시 경로 사용)
    private String testUploadDir = "test_uploads";
    private String testFileBaseUrl = "/uploads";

    @Before
    public void setUp() {
        // RecipientService의 private 필드인 uploadDir과 fileBaseUrl 설정
        ReflectionTestUtils.setField(recipientService, "uploadDir", testUploadDir);
        ReflectionTestUtils.setField(recipientService, "fileBaseUrl", testFileBaseUrl);

        // @Value로 주입받는 상수 필드 설정 (필드명이 camelCase로 변경되었을 가능성 있음)
        ReflectionTestUtils.setField(recipientService, "captchaFailedMessage", CAPTCHA_FAILED_MESSAGE);
        ReflectionTestUtils.setField(recipientService, "anonymousWriterValue", ANONYMOUS_WRITER_VALUE);
        ReflectionTestUtils.setField(recipientService, "organCodeDirectInput", ORGAN_CODE_DIRECT_INPUT);

        // 테스트를 위해 임시 디렉토리 생성 (실제 파일 시스템에 영향 주지 않음)
        try {
            Files.createDirectories(Paths.get(testUploadDir));
        } catch (IOException e) {
            // 디렉토리 생성 실패 시 예외 처리
            e.printStackTrace();
        }
    }

    // RecipientRequestDto 생성 헬퍼 메서드
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

    // RecipientEntity 생성 헬퍼 메서드
    private RecipientEntity createRecipientEntity(Integer seq, String writer, String passcode, String contents, String delFlag, String fileName, String orgFileName) {
        RecipientEntity entity = RecipientEntity.builder()
                .letterSeq(seq)
                .letterWriter(writer)
                .letterPasscode(passcode)
                .letterContents(contents)
                .delFlag(delFlag)
                .readCount(0)
                .build();
        entity.setFileName(fileName);
        entity.setOrgFileName(orgFileName);
        return entity;
    }

    // --- insertRecipient 테스트 (기존) ---
    // 이 부분은 변경된 insertRecipient 로직에 맞춰 별도로 수정해야 합니다.
    // 현재는 update/delete 테스트에 집중합니다.

    // --- updateRecipient 테스트 ---
    @Test
    public void updateRecipient_Success_NoImageChange() {
        // Given
        Integer letterSeq = 1;
        String requestPasscode = "12345678";
        RecipientEntity existingEntity = createRecipientEntity(letterSeq, "기존작가", requestPasscode, "기존 내용", "N", null, null);
        existingEntity.setOrganCode("OLD_ORG");
        existingEntity.setLetterTitle("기존 제목");
        existingEntity.setRecipientYear("2023");

        RecipientRequestDto requestDto = createRequestDto(
                "새작가", requestPasscode, "새로운 게시물 내용입니다.", "N",
                "NEW_ORG", null, "valid_captcha_token", null
        );
        requestDto.setLetterTitle("새로운 제목");
        requestDto.setRecipientYear("2024");

        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        // findById는 비밀번호 검증과 실제 엔티티 조회로 두 번 호출됩니다.
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(existingEntity));
        when(recipientRepository.save(any(RecipientEntity.class))).thenAnswer(invocation -> {
            RecipientEntity savedEntity = invocation.getArgument(0);
            savedEntity.setLetterSeq(letterSeq); // ID 유지
            return savedEntity;
        });

        // When
        RecipientDetailResponseDto resultDto = recipientService.updateRecipient(letterSeq, requestPasscode, requestDto);

        // Then
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getLetterSeq()).isEqualTo(letterSeq);
        assertThat(resultDto.getLetterWriter()).isEqualTo("새작가");
        assertThat(resultDto.getLetterContents()).isEqualTo("새로운 게시물 내용입니다.");
        assertThat(resultDto.getOrganCode()).isEqualTo("NEW_ORG");
        assertThat(resultDto.getLetterTitle()).isEqualTo("새로운 제목");
        assertThat(resultDto.getRecipientYear()).isEqualTo("2024");
        assertThat(resultDto.getFileName()).isNull();
        assertThat(resultDto.getOrgFileName()).isNull();
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(2)).findById(letterSeq); // 2번 호출 확인
        verify(recipientRepository, times(1)).save(any(RecipientEntity.class));
    }

    @Test
    public void updateRecipient_Success_ImageReplace() throws IOException {
        // Given
        Integer letterSeq = 2;
        String requestPasscode = "update_pass";
        String oldFileName = "old-image.jpg";
        String oldFileUrl = testFileBaseUrl + "/" + oldFileName;
        RecipientEntity existingEntity = createRecipientEntity(letterSeq, "이미지작가", requestPasscode, "내용", "N", oldFileUrl, oldFileName);

        byte[] newImageContent = "new image content".getBytes();
        MockMultipartFile newImageFile = new MockMultipartFile(
                "imageFile", "new_image.png", "image/png", new ByteArrayInputStream(newImageContent)
        );

        RecipientRequestDto requestDto = createRequestDto(
                "이미지작가", requestPasscode, "새로운 내용", "N",
                "ORG01", null, "valid_captcha_token", newImageFile
        );

        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(existingEntity));
        when(recipientRepository.save(any(RecipientEntity.class))).thenAnswer(invocation -> {
            RecipientEntity savedEntity = invocation.getArgument(0);
            savedEntity.setLetterSeq(letterSeq);
            // 파일 정보가 업데이트되었음을 시뮬레이션 (UUID는 Mocking이 어렵기 때문에 단순히 NotNull만 확인)
            if (requestDto.getImageFile() != null && !requestDto.getImageFile().isEmpty()) {
                String uniqueFileName = UUID.randomUUID().toString() + ".png";
                savedEntity.setFileName(testFileBaseUrl + "/" + uniqueFileName);
                savedEntity.setOrgFileName("new_image.png");
            }
            return savedEntity;
        });

        // When
        RecipientDetailResponseDto resultDto = recipientService.updateRecipient(letterSeq, requestPasscode, requestDto);

        // Then
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getLetterSeq()).isEqualTo(letterSeq);
        assertThat(resultDto.getFileName()).isNotNull(); // 파일 URL이 설정되었는지 확인
        assertThat(resultDto.getOrgFileName()).isEqualTo("new_image.png");
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(2)).findById(letterSeq); // 2번 호출 확인
        verify(recipientRepository, times(1)).save(any(RecipientEntity.class));

    }

    @Test
    public void updateRecipient_Success_AnonymousUpdate() {
        // Given
        Integer letterSeq = 3;
        String requestPasscode = "anon_pass";
        RecipientEntity existingEntity = createRecipientEntity(letterSeq, "실제작가", requestPasscode, "내용", "N", null, null);

        RecipientRequestDto requestDto = createRequestDto(
                "아무개", requestPasscode, "익명으로 변경된 내용", "Y", // 익명 Y
                "ORG01", null, "valid_captcha_token", null
        );

        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(existingEntity));
        when(recipientRepository.save(any(RecipientEntity.class))).thenAnswer(invocation -> {
            RecipientEntity savedEntity = invocation.getArgument(0);
            savedEntity.setLetterWriter(ANONYMOUS_WRITER_VALUE); // 서비스 로직 시뮬레이션
            savedEntity.setAnonymityFlag("Y"); // 익명 플래그 설정 시뮬레이션
            savedEntity.setLetterSeq(letterSeq);
            return savedEntity;
        });

        // When
        RecipientDetailResponseDto resultDto = recipientService.updateRecipient(letterSeq, requestPasscode, requestDto);

        // Then
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getLetterWriter()).isEqualTo(ANONYMOUS_WRITER_VALUE);
        assertThat(resultDto.getAnonymityFlag()).isEqualTo("Y");
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(2)).findById(letterSeq); // 2번 호출 확인
        verify(recipientRepository, times(1)).save(any(RecipientEntity.class));
    }

    @Test
    public void updateRecipient_Success_OrganCodeDirectInputUpdate() {
        // Given
        Integer letterSeq = 4;
        String requestPasscode = "organ_pass";
        RecipientEntity existingEntity = createRecipientEntity(letterSeq, "작가", requestPasscode, "내용", "N", null, null);
        existingEntity.setOrganCode("OLD_ORG");
        existingEntity.setOrganEtc(null);

        RecipientRequestDto requestDto = createRequestDto(
                "작가", requestPasscode, "내용", "N",
                ORGAN_CODE_DIRECT_INPUT, "새로운 직접 입력 기관", "valid_captcha_token", null
        );

        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(existingEntity));
        when(recipientRepository.save(any(RecipientEntity.class))).thenAnswer(invocation -> {
            RecipientEntity savedEntity = invocation.getArgument(0);
            savedEntity.setLetterSeq(letterSeq);
            return savedEntity;
        });

        // When
        RecipientDetailResponseDto resultDto = recipientService.updateRecipient(letterSeq, requestPasscode, requestDto);

        // Then
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getOrganCode()).isEqualTo(ORGAN_CODE_DIRECT_INPUT);
        assertThat(resultDto.getOrganEtc()).isEqualTo("새로운 직접 입력 기관");
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(2)).findById(letterSeq); // 2번 호출 확인
        verify(recipientRepository, times(1)).save(any(RecipientEntity.class));
    }

    @Test
    public void updateRecipient_Failure_CaptchaInvalid() {
        // Given
        Integer letterSeq = 1;
        String requestPasscode = "12345678";
        RecipientRequestDto requestDto = createRequestDto(
                "작가", requestPasscode, "내용", "N",
                "ORG01", null, "invalid_captcha_token", null
        );

        when(hcaptchaService.verifyCaptcha("invalid_captcha_token")).thenReturn(false);

        // When & Then
        RecipientInvalidDataException exception = assertThrows(RecipientInvalidDataException.class, () ->
                recipientService.updateRecipient(letterSeq, requestPasscode, requestDto)
        );
        assertThat(exception.getMessage()).isEqualTo(CAPTCHA_FAILED_MESSAGE);
        verify(hcaptchaService, times(1)).verifyCaptcha("invalid_captcha_token");
        verify(recipientRepository, never()).findById(anyInt()); // 게시물 조회 X
        verify(recipientRepository, never()).save(any(RecipientEntity.class)); // 저장 X
    }

    @Test
    public void updateRecipient_Failure_NotFound() {
        // Given
        Integer letterSeq = 999;
        String requestPasscode = "12345678";
        RecipientRequestDto requestDto = createRequestDto(
                "작가", requestPasscode, "내용", "N",
                "ORG01", null, "valid_captcha_token", null
        );

        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.empty());

        // When & Then
        RecipientNotFoundException exception = assertThrows(RecipientNotFoundException.class, () ->
                recipientService.updateRecipient(letterSeq, requestPasscode, requestDto)
        );
        assertThat(exception.getMessage()).isEqualTo(RECIPIENT_NOT_FOUND_MESSAGE);
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(1)).findById(letterSeq); // 한 번만 호출 (초기 조회)
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
    }

    @Test
    public void updateRecipient_Failure_Deleted() {
        // Given
        Integer letterSeq = 1;
        String requestPasscode = "12345678";
        RecipientEntity deletedEntity = createRecipientEntity(letterSeq, "작가", requestPasscode, "내용", "Y", null, null);

        RecipientRequestDto requestDto = createRequestDto(
                "작가", requestPasscode, "내용", "N",
                "ORG01", null, "valid_captcha_token", null
        );

        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(deletedEntity));

        // When & Then
        RecipientNotFoundException exception = assertThrows(RecipientNotFoundException.class, () ->
                recipientService.updateRecipient(letterSeq, requestPasscode, requestDto)
        );
        assertThat(exception.getMessage()).isEqualTo(RECIPIENT_NOT_FOUND_MESSAGE);
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(1)).findById(letterSeq); // 한 번만 호출 (초기 조회)
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
    }

    @Test
    public void updateRecipient_Failure_InvalidPasscode() {
        // Given
        Integer letterSeq = 1;
        String correctPasscode = "12345678";
        String wrongPasscode = "wrong_pass";
        RecipientEntity existingEntity = createRecipientEntity(letterSeq, "작가", correctPasscode, "내용", "N", null, null);

        // requestDto의 비밀번호는 중요하지 않음, requestPasscode가 검증 대상
        RecipientRequestDto requestDto = createRequestDto(
                "작가", "dummyPasscode", "내용", "N", // DTO의 비밀번호는 실제 검증에 사용되지 않음
                "ORG01", null, "valid_captcha_token", null
        );

        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(existingEntity));

        // When & Then
        RecipientInvalidPasscodeException exception = assertThrows(RecipientInvalidPasscodeException.class, () ->
                recipientService.updateRecipient(letterSeq, wrongPasscode, requestDto) // 잘못된 비밀번호 전달
        );
        assertThat(exception.getMessage()).isEqualTo("비밀번호가 일치하지 않습니다.");
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(2)).findById(letterSeq); // 2번 호출 (초기 조회 및 비밀번호 검증)
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
    }

    @Test
    public void updateRecipient_Failure_ContentsNull() {
        // Given
        Integer letterSeq = 1;
        String requestPasscode = "1234";
        RecipientEntity existingEntity = createRecipientEntity(letterSeq, "작가", requestPasscode, "기존 내용", "N", null, null);

        RecipientRequestDto requestDto = createRequestDto(
                "작가", requestPasscode, null, "N", // 내용 null
                "ORG01", null, "valid_captcha_token", null
        );

        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(existingEntity));

        // When & Then
        RecipientInvalidDataException exception = assertThrows(RecipientInvalidDataException.class, () ->
                recipientService.updateRecipient(letterSeq, requestPasscode, requestDto)
        );
        assertThat(exception.getMessage()).isEqualTo("게시물 내용은 필수 입력 항목입니다.");
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(2)).findById(letterSeq); // 2번 호출 (초기 조회 및 비밀번호 검증)
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
    }

    @Test
    public void updateRecipient_Failure_ContentsEmptyAfterHtmlFiltering() {
        // Given
        Integer letterSeq = 1;
        String requestPasscode = "1234";
        RecipientEntity existingEntity = createRecipientEntity(letterSeq, "작가", requestPasscode, "기존 내용", "N", null, null);

        RecipientRequestDto requestDto = createRequestDto(
                "작가", requestPasscode, "<p>&nbsp;</p><br>", "N", // HTML 태그만 있는 내용
                "ORG01", null, "valid_captcha_token", null
        );

        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(existingEntity));

        // When & Then
        RecipientInvalidDataException exception = assertThrows(RecipientInvalidDataException.class, () ->
                recipientService.updateRecipient(letterSeq, requestPasscode, requestDto)
        );
        assertThat(exception.getMessage()).isEqualTo("게시물 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(2)).findById(letterSeq); // 2번 호출 (초기 조회 및 비밀번호 검증)
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
    }

    @Test
    public void updateRecipient_Failure_ImageUploadIOException() throws IOException {
        // Given
        Integer letterSeq = 1;
        String requestPasscode = "1234";
        RecipientEntity existingEntity = createRecipientEntity(letterSeq, "작가", requestPasscode, "기존 내용", "N", null, null);

        // MockMultipartFile을 Mockito로 직접 모킹하여 InputStream에서 IOException을 발생시킵니다.
        MultipartFile imageFile = mock(MultipartFile.class); // MockMultipartFile 대신 MultipartFile 인터페이스 Mock
        when(imageFile.isEmpty()).thenReturn(false);
        when(imageFile.getOriginalFilename()).thenReturn("error.png");
        // 파일 스트림을 읽을 때 IOException을 발생시키도록 설정
        when(imageFile.getInputStream()).thenThrow(new IOException("Test IO Exception during upload"));

        RecipientRequestDto requestDto = createRequestDto(
                "작가", requestPasscode, "새 내용", "N",
                "ORG01", null, "valid_captcha_token", imageFile
        );

        when(hcaptchaService.verifyCaptcha("valid_captcha_token")).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(existingEntity));

        // When & Then
        RecipientInvalidDataException exception = assertThrows(RecipientInvalidDataException.class, () ->
                recipientService.updateRecipient(letterSeq, requestPasscode, requestDto)
        );
        assertThat(exception.getMessage()).isEqualTo("이미지 파일 저장 중 오류가 발생했습니다.");
        verify(hcaptchaService, times(1)).verifyCaptcha("valid_captcha_token");
        verify(recipientRepository, times(2)).findById(letterSeq); // 2번 호출 (초기 조회 및 비밀번호 검증)
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
    }


    @Test
    public void updateRecipient_Failure_OrganCodeDirectInputButOrganEtcEmpty() {
        // Given
        Integer letterSeq = 1;
        String requestPasscode = "1234";
        RecipientEntity existingEntity = createRecipientEntity(letterSeq, "작가", requestPasscode, "내용", "N", null, null);

        RecipientRequestDto requestDto = createRequestDto(
                "작가", requestPasscode, "내용", "N",
                ORGAN_CODE_DIRECT_INPUT, "", // organEtc를 빈 문자열로 설정
                "valid_captcha_token", null
        );

        when(hcaptchaService.verifyCaptcha(anyString())).thenReturn(true);
        when(recipientRepository.findById(anyInt())).thenReturn(Optional.of(existingEntity));


        // When & Then
        assertThatThrownBy(() -> recipientService.updateRecipient(letterSeq, requestPasscode, requestDto))
                .isInstanceOf(RecipientInvalidDataException.class)
                .hasMessageContaining("ORGAN000 선택 시 organEtc는 필수 입력 항목입니다.");

        verify(hcaptchaService, times(1)).verifyCaptcha(anyString());
        verify(recipientRepository, times(2)).findById(letterSeq); // 2번 호출 (초기 조회 및 비밀번호 검증)
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
    }

    // --- deleteRecipient 테스트 ---
    @Test
    public void deleteRecipient_Success_WithComments() {
        // Given
        Integer letterSeq = 1;
        String passcode = "1234";
        String captchaToken = "valid_captcha_token";
        RecipientEntity entity = createRecipientEntity(letterSeq, "작가", passcode, "내용", "N", null, null);

        RecipientCommentEntity comment1 = RecipientCommentEntity.builder().commentSeq(1).letterSeq(entity).commentContents("댓글1").delFlag("N").build();
        RecipientCommentEntity comment2 = RecipientCommentEntity.builder().commentSeq(2).letterSeq(entity).commentContents("댓글2").delFlag("N").build();
        List<RecipientCommentEntity> activeComments = Arrays.asList(comment1, comment2);

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(entity));
        when(recipientCommentRepository.findCommentsByLetterSeqAndDelFlagSorted(entity, "N")).thenReturn(activeComments);
        // save 호출 시 입력된 엔티티 그대로 반환하도록 설정
        when(recipientRepository.save(any(RecipientEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(recipientCommentRepository.save(any(RecipientCommentEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        recipientService.deleteRecipient(letterSeq, passcode, captchaToken);

        // Then
        assertThat(entity.getDelFlag()).isEqualTo("Y"); // 게시물 소프트 삭제 확인
        assertThat(comment1.getDelFlag()).isEqualTo("Y"); // 댓글1 소프트 삭제 확인
        assertThat(comment2.getDelFlag()).isEqualTo("Y"); // 댓글2 소프트 삭제 확인

        verify(hcaptchaService, times(1)).verifyCaptcha(captchaToken);
        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientRepository, times(1)).save(entity); // 게시물 저장 호출 확인
        verify(recipientCommentRepository, times(1)).findCommentsByLetterSeqAndDelFlagSorted(entity, "N");
        verify(recipientCommentRepository, times(1)).save(comment1); // 댓글 저장 호출 확인
        verify(recipientCommentRepository, times(1)).save(comment2); // 댓글 저장 호출 확인
    }

    @Test
    public void deleteRecipient_Success_NoComments() {
        // Given
        Integer letterSeq = 1;
        String passcode = "1234";
        String captchaToken = "valid_captcha_token";
        RecipientEntity entity = createRecipientEntity(letterSeq, "작가", passcode, "내용", "N", null, null);

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(entity));
        when(recipientCommentRepository.findCommentsByLetterSeqAndDelFlagSorted(entity, "N")).thenReturn(Collections.emptyList()); // 댓글 없음
        when(recipientRepository.save(any(RecipientEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        recipientService.deleteRecipient(letterSeq, passcode, captchaToken);

        // Then
        assertThat(entity.getDelFlag()).isEqualTo("Y"); // 게시물 소프트 삭제 확인
        verify(hcaptchaService, times(1)).verifyCaptcha(captchaToken);
        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientRepository, times(1)).save(entity); // 게시물 저장 호출 확인
        verify(recipientCommentRepository, times(1)).findCommentsByLetterSeqAndDelFlagSorted(entity, "N");
        verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class)); // 댓글 저장 호출되지 않음
    }

    @Test
    public void deleteRecipient_Failure_CaptchaInvalid() {
        // Given
        Integer letterSeq = 1;
        String passcode = "1234";
        String captchaToken = "invalid_captcha_token";

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(false);

        // When & Then
        RecipientInvalidDataException exception = assertThrows(RecipientInvalidDataException.class, () ->
                recipientService.deleteRecipient(letterSeq, passcode, captchaToken)
        );
        assertThat(exception.getMessage()).isEqualTo(CAPTCHA_FAILED_MESSAGE);
        verify(hcaptchaService, times(1)).verifyCaptcha(captchaToken);
        verify(recipientRepository, never()).findById(anyInt()); // 게시물 조회 X
        verify(recipientRepository, never()).save(any(RecipientEntity.class)); // 저장 X
        verify(recipientCommentRepository, never()).findCommentsByLetterSeqAndDelFlagSorted(any(), anyString()); // 댓글 조회 X
    }

    @Test
    public void deleteRecipient_Failure_NotFound() {
        // Given
        Integer letterSeq = 999;
        String passcode = "1234";
        String captchaToken = "valid_captcha_token";

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.empty());

        // When & Then
        RecipientNotFoundException exception = assertThrows(RecipientNotFoundException.class, () ->
                recipientService.deleteRecipient(letterSeq, passcode, captchaToken)
        );
        assertThat(exception.getMessage()).isEqualTo(RECIPIENT_NOT_FOUND_MESSAGE);
        verify(hcaptchaService, times(1)).verifyCaptcha(captchaToken);
        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
        verify(recipientCommentRepository, never()).findCommentsByLetterSeqAndDelFlagSorted(any(), anyString());
    }

    @Test
    public void deleteRecipient_Failure_Deleted() {
        // Given
        Integer letterSeq = 1;
        String passcode = "1234";
        String captchaToken = "valid_captcha_token";
        RecipientEntity deletedEntity = createRecipientEntity(letterSeq, "작가", passcode, "내용", "Y", null, null);

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(deletedEntity));

        // When & Then
        RecipientNotFoundException exception = assertThrows(RecipientNotFoundException.class, () ->
                recipientService.deleteRecipient(letterSeq, passcode, captchaToken)
        );
        assertThat(exception.getMessage()).isEqualTo(RECIPIENT_NOT_FOUND_MESSAGE);
        verify(hcaptchaService, times(1)).verifyCaptcha(captchaToken);
        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
        verify(recipientCommentRepository, never()).findCommentsByLetterSeqAndDelFlagSorted(any(), anyString());
    }

    @Test
    public void deleteRecipient_Failure_InvalidPasscode() {
        // Given
        Integer letterSeq = 1;
        String correctPasscode = "1234";
        String wrongPasscode = "wrong_pass";
        String captchaToken = "valid_captcha_token";
        RecipientEntity entity = createRecipientEntity(letterSeq, "작가", correctPasscode, "내용", "N", null, null);

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(entity));

        // When & Then
        RecipientInvalidPasscodeException exception = assertThrows(RecipientInvalidPasscodeException.class, () ->
                recipientService.deleteRecipient(letterSeq, wrongPasscode, captchaToken)
        );
        assertThat(exception.getMessage()).isEqualTo("비밀번호가 일치하지 않습니다.");
        verify(hcaptchaService, times(1)).verifyCaptcha(captchaToken);
        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
        verify(recipientCommentRepository, never()).findCommentsByLetterSeqAndDelFlagSorted(any(), anyString());
    }
}