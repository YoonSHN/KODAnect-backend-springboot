package kodanect.domain.recipient.service.impl;

import kodanect.common.exception.InvalidRecipientDataException;
import kodanect.common.exception.InvalidPasscodeException;
import kodanect.domain.recipient.dto.RecipientResponseDto;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.repository.RecipientRepository;
import kodanect.domain.recipient.service.RecipientService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest // 전체 Spring Boot 애플리케이션 컨텍스트를 로드합니다.
@Transactional // 각 테스트 메서드가 끝난 후 변경사항을 롤백합니다.
public class insertRecipientServiceImplTest {

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private RecipientRepository recipientRepository;

    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
    private static final int TITLE_MAX_LENGTH_BYTES = 50;
    private static final String ORGAN_CODE_REGEX = "^ORGAN(000|0(0[1-9]|1[0-4]))$";
    private static final String ORGAN_CODE_DIRECT_INPUT = "ORGAN000";
    private static final int ORGAN_ETC_MAX_LENGTH_BYTES = 90;
    private static final String ANONYMOUS_WRITER_VALUE = "익명";
    private static final int RECIPIENT_YEAR_MIN = 1995;
    private static final int RECIPIENT_YEAR_MAX = 2030;
    private static final int FILE_NAME_MAX_LENGTH_BYTES = 600;

    // 테스트용 RecipientEntity 생성 헬퍼 메서드
    private RecipientEntity createValidRecipientEntity() {
        return RecipientEntity.builder()
                .letterWriter("테스트")
                .letterTitle("테스트 제목입니다.")
                .letterContents("테스트 내용입니다. <p>일반적인 내용을 작성합니다.</p>") // relaxed safelist에 맞게 수정
                .letterPasscode("testpass123")
                .anonymityFlag("N")
                .organCode("ORGAN001")
                .recipientYear("2020")
                .fileName("test_file.jpg")
                .orgFileName("original_test_file.jpg")
                .writeTime(LocalDateTime.now())
                .build();
    }

    @Before
    public void setup() {
        // 각 테스트 전에 필요한 초기화 작업
    }

    // --- 성공 케이스 테스트 ---

    @Test
    public void testInsertRecipient_Success_NormalCase() {
        // Given
        RecipientEntity validRecipient = createValidRecipientEntity();
        validRecipient.setLetterContents("테스트 내용입니다. <script>alert('xss');</script><p>안녕</p>"); // Jsoup 필터링 테스트용 (스크립트 제거, p 태그 유지)

        // When
        RecipientResponseDto result = recipientService.insertRecipient(validRecipient);

        // Then
        Assert.assertNotNull("응답 DTO는 null이 아니어야 합니다.", result);
        Assert.assertNotNull("게시물 번호가 생성되어야 합니다.", result.getLetterSeq());
        Assert.assertEquals("테스트작성자가 일치해야 합니다.", validRecipient.getLetterWriter(), result.getLetterWriter());
        Assert.assertEquals("제목이 일치해야 합니다.", validRecipient.getLetterTitle(), result.getLetterTitle());
        // Jsoup relaxed safelist는 script 태그 제거, p 태그 허용
        String expectedCleanedContents = "테스트 내용입니다. \n<p>안녕</p>"; // <-- 이 값은 서비스 로직의 Jsoup 결과와 동일해야 함
        Assert.assertEquals("HTML 태그가 필터링된 내용이 일치해야 합니다.", expectedCleanedContents, result.getLetterContents());
        Assert.assertEquals("익명 플래그가 일치해야 합니다.", validRecipient.getAnonymityFlag(), result.getAnonymityFlag());
        Assert.assertEquals("장기 코드가 일치해야 합니다.", validRecipient.getOrganCode(), result.getOrganCode());
        Assert.assertNull("organEtc는 null이어야 합니다 (ORGAN001의 경우).", result.getOrganEtc());
        Assert.assertEquals("기증받은 년도가 일치해야 합니다.", validRecipient.getRecipientYear(), result.getRecipientYear());
        Assert.assertEquals("파일명이 일치해야 합니다.", validRecipient.getFileName(), result.getFileName());
        Assert.assertEquals("원본 파일명이 일치해야 합니다.", validRecipient.getOrgFileName(), result.getOrgFileName());
        Assert.assertNotNull("작성 시간이 설정되어야 합니다.", result.getWriteTime());
        Assert.assertEquals("조회수가 0이어야 합니다.", 0, result.getReadCount());
        Assert.assertEquals("삭제 플래그가 'N'이어야 합니다.", "N", result.getDelFlag());

        /// DB에서 직접 조회하여 확인
        Optional<RecipientEntity> savedInDb = recipientRepository.findById(result.getLetterSeq());
        Assert.assertTrue("DB에 저장된 엔티티를 찾을 수 있어야 합니다.", savedInDb.isPresent());
        // DB에서 조회한 내용도 필터링된 내용과 동일한 줄바꿈을 포함해야 함
        Assert.assertEquals("DB 저장된 내용도 필터링되어야 합니다.", expectedCleanedContents, savedInDb.get().getLetterContents());
        Assert.assertEquals("DB 저장된 비밀번호가 일치해야 합니다.", validRecipient.getLetterPasscode(), savedInDb.get().getLetterPasscode());
    }

    @Test
    public void testInsertRecipient_Success_AnonymousCase() {
        // Given
        RecipientEntity anonymousRecipient = createValidRecipientEntity();
        anonymousRecipient.setAnonymityFlag("Y");
        anonymousRecipient.setLetterWriter("실제작성자이름"); // 익명 선택 시 작성자는 "익명"으로 변경될 예정

        // When
        RecipientResponseDto result = recipientService.insertRecipient(anonymousRecipient);

        // Then
        Assert.assertEquals("익명 처리 시 작성자는 '익명'으로 변경되어야 합니다.", ANONYMOUS_WRITER_VALUE, result.getLetterWriter());
        Assert.assertEquals("익명 플래그가 'Y'여야 합니다.", "Y", result.getAnonymityFlag());
        // DB 검증 추가
        Optional<RecipientEntity> savedInDb = recipientRepository.findById(result.getLetterSeq());
        Assert.assertTrue("DB에 저장된 엔티티를 찾을 수 있어야 합니다.", savedInDb.isPresent());
        Assert.assertEquals("DB에 저장된 작성자도 '익명'이어야 합니다.", ANONYMOUS_WRITER_VALUE, savedInDb.get().getLetterWriter());
    }

    @Test
    public void testInsertRecipient_Success_Organ000WithOrganEtc() {
        // Given
        RecipientEntity recipientWithOrgan000 = createValidRecipientEntity();
        recipientWithOrgan000.setOrganCode(ORGAN_CODE_DIRECT_INPUT); // ORGAN000 (직접입력)
        recipientWithOrgan000.setOrganEtc("직접 입력한 장기");

        // When
        RecipientResponseDto result = recipientService.insertRecipient(recipientWithOrgan000);

        // Then
        Assert.assertEquals("장기 코드가 ORGAN000이어야 합니다.", ORGAN_CODE_DIRECT_INPUT, result.getOrganCode());
        Assert.assertEquals("organEtc가 일치해야 합니다.", "직접 입력한 장기", result.getOrganEtc());
        // DB 검증 추가
        Optional<RecipientEntity> savedInDb = recipientRepository.findById(result.getLetterSeq());
        Assert.assertTrue("DB에 저장된 엔티티를 찾을 수 있어야 합니다.", savedInDb.isPresent());
        Assert.assertEquals("DB에 저장된 organEtc도 일치해야 합니다.", "직접 입력한 장기", savedInDb.get().getOrganEtc());
    }

    @Test
    public void testInsertRecipient_Success_NoFileName() {
        // Given
        RecipientEntity recipient = createValidRecipientEntity();
        recipient.setFileName(null);
        recipient.setOrgFileName(null);

        // When
        RecipientResponseDto result = recipientService.insertRecipient(recipient);

        // Then
        Assert.assertNull("파일 이름이 null이어야 합니다.", result.getFileName());
        Assert.assertNull("원본 파일 이름이 null이어야 합니다.", result.getOrgFileName());
        // DB 검증 추가
        Optional<RecipientEntity> savedInDb = recipientRepository.findById(result.getLetterSeq());
        Assert.assertTrue("DB에 저장된 엔티티를 찾을 수 있어야 합니다.", savedInDb.isPresent());
        Assert.assertNull("DB에 저장된 파일 이름은 null이어야 합니다.", savedInDb.get().getFileName());
        Assert.assertNull("DB에 저장된 원본 파일 이름은 null이어야 합니다.", savedInDb.get().getOrgFileName());
    }


    // --- 실패 케이스 테스트 (예외 발생 확인) ---

    @Test(expected = ConstraintViolationException.class) // 비밀번호 유효성 검사 실패 (서비스 내부 로직)
    public void testInsertRecipient_Fail_InvalidPassword() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setLetterPasscode("short"); // 8자 미만
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: InvalidPasscodeException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // 제목 길이 초과 (@Valid에 의해 처리 예상)
    public void testInsertRecipient_Fail_TitleTooLong() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        byte[] longTitleBytes = new byte[TITLE_MAX_LENGTH_BYTES + 1]; // 최대 길이 초과
        for (int i = 0; i < longTitleBytes.length; i++) {
            longTitleBytes[i] = 'a';
        }
        invalidRecipient.setLetterTitle(new String(longTitleBytes, StandardCharsets.UTF_8));
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    @Test(expected = InvalidRecipientDataException.class) // 내용 null (서비스 내부 로직)
    public void testInsertRecipient_Fail_ContentsNull() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setLetterContents(null);
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: InvalidRecipientDataException이 발생해야 함
    }

    @Test(expected = InvalidRecipientDataException.class) // 내용 공백 (서비스 내부 로직)
    public void testInsertRecipient_Fail_ContentsEmpty() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setLetterContents("   ");
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: InvalidRecipientDataException이 발생해야 함
    }

    @Test(expected = InvalidRecipientDataException.class) // Jsoup 필터링 후 내용 비어있음 (서비스 내부 로직)
    public void testInsertRecipient_Fail_ContentsEmptyAfterFiltering() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        // relaxed safelist가 모든 태그를 걸러내고 순수 텍스트가 남지 않는 경우
        invalidRecipient.setLetterContents("<script>alert('xss');</script>"); // 스크립트만 있고 유효한 텍스트가 없는 경우
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: InvalidRecipientDataException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // 장기 코드 널 (@Valid에 의해 처리 예상)
    public void testInsertRecipient_Fail_OrganCodeNull() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setOrganCode(null);
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // 장기 코드 유효하지 않음 (@Valid에 의해 처리 예상)
    public void testInsertRecipient_Fail_InvalidOrganCode() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setOrganCode("INVALIDCODE");
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    @Test(expected = InvalidRecipientDataException.class) // ORGAN000인데 organEtc 널 (서비스 내부 로직)
    public void testInsertRecipient_Fail_Organ000WithoutOrganEtc() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setOrganCode(ORGAN_CODE_DIRECT_INPUT);
        invalidRecipient.setOrganEtc(null); // ORGAN000이지만 organEtc가 null
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: InvalidRecipientDataException이 발생해야 함
    }

    @Test(expected = InvalidRecipientDataException.class) // ORGAN000인데 organEtc 공백 (@Valid에 의해 처리 예상)
    public void testInsertRecipient_Fail_Organ000WithEmptyOrganEtc() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setOrganCode(ORGAN_CODE_DIRECT_INPUT);
        invalidRecipient.setOrganEtc("   "); // ORGAN000이지만 organEtc가 공백
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: InvalidRecipientDataException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // ORGAN000인데 organEtc 길이 초과 (@Valid에 의해 처리 예상)
    public void testInsertRecipient_Fail_Organ000WithTooLongOrganEtc() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setOrganCode(ORGAN_CODE_DIRECT_INPUT);
        // 30글자를 초과하는 31글자 한글 생성 (93바이트)
        StringBuilder longOrganEtcBuilder = new StringBuilder();
        for (int i = 0; i < 31; i++) { // 30글자를 초과하는 31글자 한글 생성
            longOrganEtcBuilder.append('가');
        }
        String longOrganEtcString = longOrganEtcBuilder.toString();
        invalidRecipient.setOrganEtc(longOrganEtcString);
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    // 작성자 관련 테스트 (익명 아닌 경우)
    @Test(expected = ConstraintViolationException.class) // 익명 아닌데 작성자 널 (@Valid에 의해 처리 예상)
    public void testInsertRecipient_Fail_WriterNullWhenNotAnonymous() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setAnonymityFlag("N");
        invalidRecipient.setLetterWriter(null);
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // 익명 아닌데 작성자 공백 (@Valid에 의해 처리 예상)
    public void testInsertRecipient_Fail_WriterEmptyWhenNotAnonymous() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setAnonymityFlag("N");
        invalidRecipient.setLetterWriter("   ");
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // 익명 아닌데 작성자 길이 초과 (@Valid에 의해 처리 예상)
    public void testInsertRecipient_Fail_WriterTooLongWhenNotAnonymous() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setAnonymityFlag("N");
        // 한글 10자를 초과하는 문자열 생성 (예: '가' 문자를 11개 반복)
        StringBuilder longWriterBuilder = new StringBuilder();
        for (int i = 0; i < 11; i++) { // 안전하게 10바이트를 넘기기 위함
            longWriterBuilder.append('가');
        }
        String longWriterString = longWriterBuilder.toString();
        invalidRecipient.setLetterWriter(longWriterString);

        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // 기증받은 년도 널 (@Valid에 의해 처리 예상)
    public void testInsertRecipient_Fail_RecipientYearNull() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setRecipientYear(null);
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // 기증받은 년도 공백 (@Valid에 의해 처리 예상)
    public void testInsertRecipient_Fail_RecipientYearEmpty() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setRecipientYear("   ");
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // 기증받은 년도 숫자 아님 (서비스 내부 로직)
    public void testInsertRecipient_Fail_RecipientYearNotNumber() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setRecipientYear("ABCD");
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // 기증받은 년도 범위 미달 (이전: InvalidRecipientDataException -> 변경)
    public void testInsertRecipient_Fail_RecipientYearTooLow() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setRecipientYear(String.valueOf(RECIPIENT_YEAR_MIN - 1));
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // 기증받은 년도 범위 초과 (이전: InvalidRecipientDataException -> 변경)
    public void testInsertRecipient_Fail_RecipientYearTooHigh() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setRecipientYear(String.valueOf(RECIPIENT_YEAR_MAX + 1));
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // 파일 이름 길이 초과 (@Valid에 의해 처리 예상)
    public void testInsertRecipient_Fail_FileNameTooLong() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        byte[] longFileNameBytes = new byte[FILE_NAME_MAX_LENGTH_BYTES + 1];
        for (int i = 0; i < longFileNameBytes.length; i++) {
            longFileNameBytes[i] = 'a';
        }
        invalidRecipient.setFileName(new String(longFileNameBytes, StandardCharsets.UTF_8));
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    @Test(expected = ConstraintViolationException.class) // 원본 파일 이름 길이 초과 (@Valid에 의해 처리 예상)
    public void testInsertRecipient_Fail_OrgFileNameTooLong() {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        byte[] longOrgFileNameBytes = new byte[FILE_NAME_MAX_LENGTH_BYTES + 1];
        for (int i = 0; i < longOrgFileNameBytes.length; i++) {
            longOrgFileNameBytes[i] = 'a';
        }
        invalidRecipient.setOrgFileName(new String(longOrgFileNameBytes, StandardCharsets.UTF_8));
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: ConstraintViolationException이 발생해야 함
    }

    // 파일 이름이 null/empty일 때 유효성 검사 통과 확인
    @Test
    public void testInsertRecipient_Success_FileNameAndOrgFileNameNull() {
        // Given
        RecipientEntity recipient = createValidRecipientEntity();
        recipient.setFileName(null);
        recipient.setOrgFileName(null);

        // When
        RecipientResponseDto result = recipientService.insertRecipient(recipient);

        // Then
        Assert.assertNotNull("결과 DTO는 null이 아니어야 합니다.", result);
        Assert.assertNull("파일 이름은 null이어야 합니다.", result.getFileName());
        Assert.assertNull("원본 파일 이름은 null이어야 합니다.", result.getOrgFileName());
    }

    @Test
    public void testInsertRecipient_Success_FileNameAndOrgFileNameEmpty() {
        // Given
        RecipientEntity recipient = createValidRecipientEntity();
        recipient.setFileName("");
        recipient.setOrgFileName("");

        // When
        RecipientResponseDto result = recipientService.insertRecipient(recipient);

        // Then
        Assert.assertNotNull("결과 DTO는 null이 아니어야 합니다.", result);
        Assert.assertEquals("파일 이름은 빈 문자열이어야 합니다.", "", result.getFileName());
        Assert.assertEquals("원본 파일 이름은 빈 문자열이어야 합니다.", "", result.getOrgFileName());
    }

}