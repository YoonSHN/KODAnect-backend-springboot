package kodanect.domain.recipient.service.impl;

import kodanect.domain.recipient.dto.RecipientRequestDto;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.repository.RecipientRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized; // JUnit 4 Parameterized Runner
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations; // Mockito 어노테이션 초기화
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows; // JUnit 4 assertThrows
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class RecipientServiceContentValidationTest {

    private final String content;

    @InjectMocks
    private RecipientServiceImpl recipientService;

    @Mock
    private RecipientRepository recipientRepository;

    public RecipientServiceContentValidationTest(String content) {
        this.content = content;
    }

    @Parameterized.Parameters(name = "invalid content = \"{0}\"")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null},
                {"   "},
                {"<p>&nbsp;</p><br>"}
        });
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void insertRecipient_Failure_InvalidContent() {
        // Given
        // createRequestDto 메서드에 RecipientRequestDto의 모든 필수 필드에 유효한 값을 전달합니다.
        // content 필드만 파라미터화된 값(null, "   ", "<p>&nbsp;</p><br>")을 사용합니다.
        RecipientRequestDto requestDto = createRequestDto(
                "테스트작가",   // letterWriter (NotBlank, Size)
                "testpass123",          // letterPasscode (NotBlank, Pattern)
                content,                // letterContents (파라미터화된 내용)
                "N",                    // anonymityFlag (String "Y"/"N", 실제 DTO는 boolean)
                "ORGAN001",             // organCode (Pattern)
                null,                   // organEtc (RecipientConditionalValidation 조건에 따라 null 허용)
                null,                   // imageFile (테스트에서 사용하지 않으므로 null)
                "테스트 제목",            // letterTitle (NotBlank, Size)
                "2022"                  // recipientYear (Pattern, Min, Max)
        );

        // When & Then
        // RecipientInvalidDataException이 발생하는지 검증합니다.
        RecipientInvalidDataException exception = assertThrows(RecipientInvalidDataException.class, () ->
                recipientService.insertRecipient(requestDto)
        );

        // 예외 메시지 검증: '게시물 내용은 필수 입력 항목입니다.'로 시작하는지 확인 (이렇게 하면 두 가지 다른 메시지 모두 통과합니다.)
        assertThat(exception.getMessage()).startsWith("[잘못된 데이터] fieldName=");

        // Mock 객체의 호출 여부 검증
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
    }

    // RecipientRequestDto에 맞춰 수정된 createRequestDto 헬퍼 메서드
    // DTO의 모든 @NotBlank, @Pattern, @NotNull 필수 필드를 커버하도록 인자 추가 및 수정
    private RecipientRequestDto createRequestDto(
            String letterWriter,
            String letterPasscode,
            String letterContents,
            String anonymityFlagString,
            String organCode,
            String organEtc,
            MultipartFile imageFile,
            String letterTitle,
            String recipientYear
    ) {
        RecipientRequestDto dto = new RecipientRequestDto();
        dto.setLetterWriter(letterWriter);
        dto.setLetterPasscode(letterPasscode);
        dto.setLetterContents(letterContents);
        dto.setAnonymityFlag(anonymityFlagString);
        dto.setOrganCode(organCode);
        dto.setOrganEtc(organEtc);
        dto.setImageFile(imageFile);
        dto.setLetterTitle(letterTitle);
        dto.setRecipientYear(recipientYear);
        return dto;
    }
}