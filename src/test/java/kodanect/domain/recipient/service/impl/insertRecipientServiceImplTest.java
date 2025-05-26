package kodanect.domain.recipient.service.impl;

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

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest // 전체 Spring Boot 애플리케이션 컨텍스트를 로드합니다.
@Transactional // 각 테스트 메서드가 끝난 후 변경사항을 롤백합니다.
public class insertRecipientServiceImplTest {

    @Autowired
    private RecipientService recipientService; // 테스트 대상 서비스

    @Autowired
    private RecipientRepository recipientRepository; // 데이터 검증용 레포지토리

    // 서비스 임플리먼테이션에 정의된 상수들을 여기에 복사하거나
    // 해당 클래스에서 가져올 수 있도록 설정해야 합니다.
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
    private static final int TITLE_MAX_LENGTH_BYTES = 50; // UTF-8 기준
    private static final String ORGAN_CODE_REGEX = "^ORGAN(000|0(0[1-9]|1[0-4]))$";
    private static final String ORGAN_CODE_DIRECT_INPUT = "ORGAN000";
    private static final int ORGAN_ETC_MAX_LENGTH_BYTES = 30; // UTF-8 기준
    private static final String ANONYMOUS_WRITER_VALUE = "익명";
    private static final int WRITER_MAX_LENGTH_BYTES = 10; // UTF-8 기준
    private static final int RECIPIENT_YEAR_MIN = 1995;
    private static final int RECIPIENT_YEAR_MAX = 2030;
    private static final int FILE_NAME_MAX_LENGTH_BYTES = 600; // varchar(600) 기준

    // 테스트용 RecipientEntity 생성 헬퍼 메서드
    private RecipientEntity createValidRecipientEntity() {
        return RecipientEntity.builder()
                .letterWriter("테스트")
                .letterTitle("테스트 제목입니다.")
                .letterContents("테스트 내용입니다. <script>alert('xss');</script>") // XSS 필터링 테스트용
                .letterPasscode("testpass123")
                .anonymityFlag("N")
                .organCode("ORGAN001")
                .recipientYear("2020")
                .fileName("test_file.jpg")
                .orgFileName("original_test_file.jpg")
                .build();
    }

    @Before
    public void setup() {
        // 각 테스트 전에 필요한 초기화 작업 (예: 특정 데이터 삽입)
        // @Transactional이 있으므로, 여기서 데이터를 넣어도 테스트 종료 후 롤백됩니다.
    }

    // --- 성공 케이스 테스트 ---

    @Test
    public void testInsertRecipient_Success_NormalCase() throws Exception {
        // Given
        RecipientEntity validRecipient = createValidRecipientEntity();

        // When
        RecipientResponseDto result = recipientService.insertRecipient(validRecipient);

        // Then
        Assert.assertNotNull("응답 DTO는 null이 아니어야 합니다.", result);
        Assert.assertNotNull("게시물 번호가 생성되어야 합니다.", result.getLetterSeq());
        Assert.assertEquals("테스트작성자가 일치해야 합니다.", validRecipient.getLetterWriter(), result.getLetterWriter());
        Assert.assertEquals("제목이 일치해야 합니다.", validRecipient.getLetterTitle(), result.getLetterTitle());
        // Jsoup 필터링 후 내용 확인
        Assert.assertEquals("HTML 태그가 제거된 내용이 일치해야 합니다.", "테스트 내용입니다.", result.getLetterContents()); // <script> 태그 제거 확인
        Assert.assertEquals("익명 플래그가 일치해야 합니다.", validRecipient.getAnonymityFlag(), result.getAnonymityFlag());
        Assert.assertEquals("장기 코드가 일치해야 합니다.", validRecipient.getOrganCode(), result.getOrganCode());
        Assert.assertNull("organEtc는 null이어야 합니다 (ORGAN001의 경우).", result.getOrganEtc()); // ORGAN001이므로 null이어야 함
        Assert.assertEquals("기증받은 년도가 일치해야 합니다.", validRecipient.getRecipientYear(), result.getRecipientYear());
        Assert.assertEquals("파일명이 일치해야 합니다.", validRecipient.getFileName(), result.getFileName());
        Assert.assertEquals("원본 파일명이 일치해야 합니다.", validRecipient.getOrgFileName(), result.getOrgFileName());
        Assert.assertNotNull("작성 시간이 설정되어야 합니다.", result.getWriteTime());
        Assert.assertEquals("조회수가 0이어야 합니다.", 0, result.getReadCount());
        Assert.assertEquals("삭제 플래그가 'N'이어야 합니다.", "N", result.getDelFlag());

        // DB에서 직접 조회하여 확인 (선택 사항, 그러나 통합 테스트의 장점 활용)
        Optional<RecipientEntity> savedInDb = recipientRepository.findById(result.getLetterSeq());
        Assert.assertTrue("DB에 저장된 엔티티를 찾을 수 있어야 합니다.", savedInDb.isPresent());
        Assert.assertEquals("DB 저장된 내용도 필터링되어야 합니다.", "테스트 내용입니다.", savedInDb.get().getLetterContents());
        Assert.assertEquals("DB 저장된 비밀번호가 일치해야 합니다.", validRecipient.getLetterPasscode(), savedInDb.get().getLetterPasscode());
    }

    @Test
    public void testInsertRecipient_Success_AnonymousCase() throws Exception {
        // Given
        RecipientEntity anonymousRecipient = createValidRecipientEntity();
        anonymousRecipient.setAnonymityFlag("Y");
        anonymousRecipient.setLetterWriter("실제작성자이름"); // 익명 선택 시 작성자는 "익명"으로 변경될 예정

        // When
        RecipientResponseDto result = recipientService.insertRecipient(anonymousRecipient);

        // Then
        Assert.assertEquals("익명 처리 시 작성자는 '익명'으로 변경되어야 합니다.", ANONYMOUS_WRITER_VALUE, result.getLetterWriter());
        Assert.assertEquals("익명 플래그가 'Y'여야 합니다.", "Y", result.getAnonymityFlag());
    }

    @Test
    public void testInsertRecipient_Success_Organ000WithOrganEtc() throws Exception {
        // Given
        RecipientEntity recipientWithOrgan000 = createValidRecipientEntity();
        recipientWithOrgan000.setOrganCode(ORGAN_CODE_DIRECT_INPUT); // ORGAN000 (직접입력)
        recipientWithOrgan000.setOrganEtc("직접 입력한 장기");

        // When
        RecipientResponseDto result = recipientService.insertRecipient(recipientWithOrgan000);

        // Then
        Assert.assertEquals("장기 코드가 ORGAN000이어야 합니다.", ORGAN_CODE_DIRECT_INPUT, result.getOrganCode());
        Assert.assertEquals("organEtc가 일치해야 합니다.", "직접 입력한 장기", result.getOrganEtc());
    }

    @Test
    public void testInsertRecipient_Success_NoFileName() throws Exception {
        // Given
        RecipientEntity recipient = createValidRecipientEntity();
        recipient.setFileName(null);
        recipient.setOrgFileName(null);

        // When
        RecipientResponseDto result = recipientService.insertRecipient(recipient);

        // Then
        Assert.assertNull("파일 이름이 null이어야 합니다.", result.getFileName());
        Assert.assertNull("원본 파일 이름이 null이어야 합니다.", result.getOrgFileName());
    }


    // --- 실패 케이스 테스트 (예외 발생 확인) ---

    @Test(expected = Exception.class) // 비밀번호 유효성 검사 실패
    public void testInsertRecipient_Fail_InvalidPassword() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setLetterPasscode("short"); // 8자 미만
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 제목 길이 초과
    public void testInsertRecipient_Fail_TitleTooLong() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        byte[] longTitleBytes = new byte[TITLE_MAX_LENGTH_BYTES + 1]; // 최대 길이 초과
        for (int i = 0; i < longTitleBytes.length; i++) {
            longTitleBytes[i] = 'a';
        }
        invalidRecipient.setLetterTitle(new String(longTitleBytes, StandardCharsets.UTF_8));
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 내용 null
    public void testInsertRecipient_Fail_ContentsNull() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setLetterContents(null);
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 내용 공백
    public void testInsertRecipient_Fail_ContentsEmpty() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setLetterContents("   ");
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // Jsoup 필터링 후 내용 비어있음
    public void testInsertRecipient_Fail_ContentsEmptyAfterFiltering() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setLetterContents("<p><script>alert('xss');</script></p>"); // 필터링 후 비어있게 되는 내용
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 장기 코드 널
    public void testInsertRecipient_Fail_OrganCodeNull() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setOrganCode(null);
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 장기 코드 유효하지 않음
    public void testInsertRecipient_Fail_InvalidOrganCode() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setOrganCode("INVALIDCODE");
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // ORGAN000인데 organEtc 널
    public void testInsertRecipient_Fail_Organ000WithoutOrganEtc() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setOrganCode(ORGAN_CODE_DIRECT_INPUT);
        invalidRecipient.setOrganEtc(null); // ORGAN000이지만 organEtc가 null
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // ORGAN000인데 organEtc 공백
    public void testInsertRecipient_Fail_Organ000WithEmptyOrganEtc() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setOrganCode(ORGAN_CODE_DIRECT_INPUT);
        invalidRecipient.setOrganEtc("   "); // ORGAN000이지만 organEtc가 공백
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // ORGAN000인데 organEtc 길이 초과
    public void testInsertRecipient_Fail_Organ000WithTooLongOrganEtc() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setOrganCode(ORGAN_CODE_DIRECT_INPUT);
        byte[] longOrganEtcBytes = new byte[ORGAN_ETC_MAX_LENGTH_BYTES + 1];
        for (int i = 0; i < longOrganEtcBytes.length; i++) {
            longOrganEtcBytes[i] = 'a';
        }
        invalidRecipient.setOrganEtc(new String(longOrganEtcBytes, StandardCharsets.UTF_8));
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 익명 아닌데 작성자 널
    public void testInsertRecipient_Fail_WriterNullWhenNotAnonymous() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setAnonymityFlag("N");
        invalidRecipient.setLetterWriter(null);
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 익명 아닌데 작성자 공백
    public void testInsertRecipient_Fail_WriterEmptyWhenNotAnonymous() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setAnonymityFlag("N");
        invalidRecipient.setLetterWriter("   ");
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 익명 아닌데 작성자 길이 초과
    public void testInsertRecipient_Fail_WriterTooLongWhenNotAnonymous() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setAnonymityFlag("N");
        // 한글 10자를 초과하는 문자열 생성 (예: '가' 문자를 11개 반복)
        StringBuilder longWriterBuilder = new StringBuilder();
        for (int i = 0; i < (WRITER_MAX_LENGTH_BYTES / 3) + 2; i++) { // 안전하게 10바이트를 넘기기 위함
            longWriterBuilder.append('가');
        }
        String longWriterString = longWriterBuilder.toString();
        // 실제 바이트 길이 확인 (디버깅용)
        // System.out.println("Long writer string byte length: " + longWriterString.getBytes(StandardCharsets.UTF_8).length);

        invalidRecipient.setLetterWriter(longWriterString);

        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 기증받은 년도 널
    public void testInsertRecipient_Fail_RecipientYearNull() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setRecipientYear(null);
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 기증받은 년도 공백
    public void testInsertRecipient_Fail_RecipientYearEmpty() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setRecipientYear("   ");
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 기증받은 년도 숫자 아님
    public void testInsertRecipient_Fail_RecipientYearNotNumber() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setRecipientYear("ABCD");
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 기증받은 년도 범위 미달
    public void testInsertRecipient_Fail_RecipientYearTooLow() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setRecipientYear(String.valueOf(RECIPIENT_YEAR_MIN - 1));
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 기증받은 년도 범위 초과
    public void testInsertRecipient_Fail_RecipientYearTooHigh() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        invalidRecipient.setRecipientYear(String.valueOf(RECIPIENT_YEAR_MAX + 1));
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 파일 이름 길이 초과
    public void testInsertRecipient_Fail_FileNameTooLong() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        byte[] longFileNameBytes = new byte[FILE_NAME_MAX_LENGTH_BYTES + 1];
        for (int i = 0; i < longFileNameBytes.length; i++) {
            longFileNameBytes[i] = 'a';
        }
        invalidRecipient.setFileName(new String(longFileNameBytes, StandardCharsets.UTF_8));
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    @Test(expected = Exception.class) // 원본 파일 이름 길이 초과
    public void testInsertRecipient_Fail_OrgFileNameTooLong() throws Exception {
        // Given
        RecipientEntity invalidRecipient = createValidRecipientEntity();
        byte[] longOrgFileNameBytes = new byte[FILE_NAME_MAX_LENGTH_BYTES + 1];
        for (int i = 0; i < longOrgFileNameBytes.length; i++) {
            longOrgFileNameBytes[i] = 'a';
        }
        invalidRecipient.setOrgFileName(new String(longOrgFileNameBytes, StandardCharsets.UTF_8));
        // When
        recipientService.insertRecipient(invalidRecipient);
        // Then: Exception이 발생해야 함
    }

    // 파일 이름이 null/empty일 때 유효성 검사 통과 확인
    @Test
    public void testInsertRecipient_Success_FileNameAndOrgFileNameNull() throws Exception {
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
    public void testInsertRecipient_Success_FileNameAndOrgFileNameEmpty() throws Exception {
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