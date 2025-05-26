package kodanect.domain.recipient.service.impl;

import kodanect.domain.recipient.dto.RecipientResponseDto;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class updateRecipientServiceImplTest {

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private RecipientCommentRepository recipientCommentRepository; // 댓글 소프트 삭제 검증용

    private RecipientEntity testRecipient;
    private RecipientEntity otherRecipient; // 삭제 테스트용 게시물

    // 서비스 임플리먼테이션에 정의된 상수들을 여기에 복사하거나
    // 해당 클래스에서 가져올 수 있도록 설정해야 합니다.
    // 실제 값과 일치하는지 반드시 확인하고 수정하세요.
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
                .letterWriter("테스터") // 10바이트 이하의 유효한 값
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

    // 테스트용 RecipientCommentEntity 생성 헬퍼 메서드
    private RecipientCommentEntity createAndSaveComment(RecipientEntity letter, String writer, String contents, String passcode, String delFlag, LocalDateTime writeTime) {
        RecipientCommentEntity comment = new RecipientCommentEntity();
        comment.setLetter(letter);
        comment.setCommentWriter(writer);
        comment.setContents(contents);
        comment.setCommentPasscode(passcode);
        comment.setDelFlag(delFlag);
        comment.setWriteTime(writeTime);
        return recipientCommentRepository.save(comment);
    }


    @Before
    public void setup() {
        // 각 테스트 전에 필요한 초기화 작업 (예: 특정 데이터 삽입)
        // @Transactional이 있으므로, 여기서 데이터를 넣어도 테스트 종료 후 롤백됩니다.
        testRecipient = new RecipientEntity();
        testRecipient.setLetterTitle("초기 게시물 제목");
        testRecipient.setLetterWriter("초기작성자");
        testRecipient.setLetterPasscode("initpass123");
        testRecipient.setLetterContents("초기 게시물 내용.");
        testRecipient.setAnonymityFlag("N");
        testRecipient.setDelFlag("N");
        testRecipient.setReadCount(0);
        testRecipient.setRecipientYear("2023");
        testRecipient.setOrganCode("ORGAN001");
        testRecipient.setWriteTime(LocalDateTime.now().minusDays(1));
        testRecipient = recipientRepository.save(testRecipient);

        // 삭제 테스트용 게시물 (댓글 포함)
        otherRecipient = new RecipientEntity();
        otherRecipient.setLetterTitle("삭제 테스트 게시물");
        otherRecipient.setLetterWriter("삭제작성자");
        otherRecipient.setLetterPasscode("delpass123");
        otherRecipient.setLetterContents("삭제될 게시물 내용.");
        otherRecipient.setAnonymityFlag("N");
        otherRecipient.setDelFlag("N");
        otherRecipient.setReadCount(0);
        otherRecipient.setRecipientYear("2024");
        otherRecipient.setOrganCode("ORGAN001");
        otherRecipient.setWriteTime(LocalDateTime.now().minusDays(2));
        otherRecipient = recipientRepository.save(otherRecipient);

        // 삭제될 게시물에 댓글 추가
        createAndSaveComment(otherRecipient, "댓글1", "삭제될 댓글1", "pass1", "N", LocalDateTime.now().minusMinutes(10));
        createAndSaveComment(otherRecipient, "댓글2", "삭제될 댓글2", "pass2", "N", LocalDateTime.now().minusMinutes(5));
        createAndSaveComment(otherRecipient, "댓글3", "삭제될 댓글3", "pass3", "Y", LocalDateTime.now().minusMinutes(20)); // 이미 삭제된 댓글
    }

    // --- insertRecipient 관련 테스트 (이전에 작성된 부분) ---
    // ... (이전에 작성한 testInsertRecipient_Success_NormalCase 등 포함) ...

    // --- 게시물 비밀번호 확인 (verifyLetterPassword) 테스트 ---

    @Test
    public void testVerifyLetterPassword_Success() throws Exception {
        // Given (setup에서 testRecipient 생성)
        String correctPasscode = testRecipient.getLetterPasscode();
        Integer letterSeq = testRecipient.getLetterSeq();

        // When
        boolean isVerified = recipientService.verifyLetterPassword(letterSeq, correctPasscode);

        // Then
        Assert.assertTrue("올바른 비밀번호로 인증에 성공해야 합니다.", isVerified);
    }

    @Test
    public void testVerifyLetterPassword_Fail_InvalidPasscode() throws Exception {
        // Given (setup에서 testRecipient 생성)
        String wrongPasscode = "wrongpass";
        Integer letterSeq = testRecipient.getLetterSeq();

        // When
        boolean isVerified = recipientService.verifyLetterPassword(letterSeq, wrongPasscode);

        // Then
        Assert.assertFalse("잘못된 비밀번호로 인증에 실패해야 합니다.", isVerified);
    }

    @Test
    public void testVerifyLetterPassword_Fail_PasscodeNull() throws Exception {
        // Given (setup에서 testRecipient 생성)
        Integer letterSeq = testRecipient.getLetterSeq();

        // When
        boolean isVerified = recipientService.verifyLetterPassword(letterSeq, null);

        // Then
        Assert.assertFalse("null 비밀번호로 인증에 실패해야 합니다.", isVerified);
    }

    @Test(expected = Exception.class)
    public void testVerifyLetterPassword_Fail_NotFound() throws Exception {
        // Given
        Integer nonExistentLetterSeq = 9999; // 존재하지 않는 게시물 번호
        String anyPasscode = "anypass";

        // When
        recipientService.verifyLetterPassword(nonExistentLetterSeq, anyPasscode);

        // Then: Exception("해당 게시물이 존재하지 않거나 이미 삭제되었습니다.") 발생해야 함
    }

    @Test(expected = Exception.class)
    public void testVerifyLetterPassword_Fail_AlreadyDeleted() throws Exception {
        // Given
        RecipientEntity deletedRecipient = new RecipientEntity();
        deletedRecipient.setLetterTitle("삭제된 게시물");
        deletedRecipient.setLetterWriter("삭제됨");
        deletedRecipient.setLetterPasscode("deletedpass");
        deletedRecipient.setLetterContents("삭제된 내용");
        deletedRecipient.setAnonymityFlag("N");
        deletedRecipient.setDelFlag("Y"); // 이미 삭제된 상태
        deletedRecipient.setReadCount(0);
        deletedRecipient.setRecipientYear("2022");
        deletedRecipient.setOrganCode("ORGAN001");
        deletedRecipient.setWriteTime(LocalDateTime.now().minusDays(5));
        deletedRecipient = recipientRepository.save(deletedRecipient);

        // When
        recipientService.verifyLetterPassword(deletedRecipient.getLetterSeq(), "deletedpass");

        // Then: Exception("해당 게시물이 존재하지 않거나 이미 삭제되었습니다.") 발생해야 함
    }


    // --- 게시물 수정 (updateRecipient) 테스트 ---

    @Test
    public void testUpdateRecipient_Success() throws Exception {
        // Given
        String originalPasscode = testRecipient.getLetterPasscode();
        Integer letterSeqToUpdate = testRecipient.getLetterSeq();

        // 업데이트할 내용이 담긴 RecipientEntity (새로운 Request DTO 역할)
        RecipientEntity updateRequest = RecipientEntity.builder()
                .letterWriter("수정작성자")
                .letterTitle("수정된 제목입니다.")
                .letterContents("수정된 내용입니다. (태그 없음)")
                .letterPasscode("newpass456") // 비밀번호 변경
                .anonymityFlag("Y") // 익명으로 변경
                .organCode("ORGAN002")
                .recipientYear("2024")
                .fileName("updated_file.pdf")
                .orgFileName("original_updated_file.pdf")
                .modifierId("admin") // 수정자 ID
                .build();

        // When
        RecipientResponseDto updatedDto = recipientService.updateRecipient(updateRequest, letterSeqToUpdate, originalPasscode);

        // Then
        Assert.assertNotNull("업데이트된 DTO는 null이 아니어야 합니다.", updatedDto);
        Assert.assertEquals("게시물 번호는 동일해야 합니다.", letterSeqToUpdate, updatedDto.getLetterSeq());
        Assert.assertEquals("작성자(익명)가 변경되어야 합니다.", ANONYMOUS_WRITER_VALUE, updatedDto.getLetterWriter()); // 익명으로 변경되었으니 "익명"이 되어야 함
        Assert.assertEquals("제목이 변경되어야 합니다.", updateRequest.getLetterTitle(), updatedDto.getLetterTitle());
        Assert.assertEquals("내용이 변경되어야 합니다.", updateRequest.getLetterContents(), updatedDto.getLetterContents());
        Assert.assertEquals("익명 플래그가 변경되어야 합니다.", "Y", updatedDto.getAnonymityFlag());
        Assert.assertEquals("장기 코드가 변경되어야 합니다.", updateRequest.getOrganCode(), updatedDto.getOrganCode());
        Assert.assertEquals("기증받은 년도가 변경되어야 합니다.", updateRequest.getRecipientYear(), updatedDto.getRecipientYear());
        Assert.assertEquals("파일 이름이 변경되어야 합니다.", updateRequest.getFileName(), updatedDto.getFileName());
        Assert.assertEquals("원본 파일 이름이 변경되어야 합니다.", updateRequest.getOrgFileName(), updatedDto.getOrgFileName());
        Assert.assertEquals("수정자 ID가 변경되어야 합니다.", updateRequest.getModifierId(), updatedDto.getModifierId());

        // DB에서 직접 조회하여 변경된 비밀번호 확인
        Optional<RecipientEntity> savedInDb = recipientRepository.findById(letterSeqToUpdate);
        Assert.assertTrue("DB에 저장된 엔티티를 찾을 수 있어야 합니다.", savedInDb.isPresent());
        Assert.assertEquals("DB에 저장된 비밀번호가 업데이트되어야 합니다.", updateRequest.getLetterPasscode(), savedInDb.get().getLetterPasscode());
        Assert.assertEquals("DB에 저장된 익명 플래그가 업데이트되어야 합니다.", "Y", savedInDb.get().getAnonymityFlag());
    }

    @Test
    public void testUpdateRecipient_Success_NoPasscodeChange() throws Exception {
        // Given
        String originalPasscode = testRecipient.getLetterPasscode();
        Integer letterSeqToUpdate = testRecipient.getLetterSeq();

        // 비밀번호를 변경하지 않는 요청
        RecipientEntity updateRequest = createValidRecipientEntity(); // 유효한 기본값으로 시작
        updateRequest.setLetterPasscode(null); // 비밀번호 필드를 null로 설정 (서비스 로직에서 변경하지 않도록)
        updateRequest.setLetterTitle("비밀번호 변경 없는 제목");

        // When
        RecipientResponseDto updatedDto = recipientService.updateRecipient(updateRequest, letterSeqToUpdate, originalPasscode);

        // Then
        Assert.assertNotNull("업데이트된 DTO는 null이 아니어야 합니다.", updatedDto);
        Assert.assertEquals("제목은 변경되어야 합니다.", updateRequest.getLetterTitle(), updatedDto.getLetterTitle());

        // DB에서 직접 조회하여 비밀번호가 그대로인지 확인
        Optional<RecipientEntity> savedInDb = recipientRepository.findById(letterSeqToUpdate);
        Assert.assertTrue("DB에 저장된 엔티티를 찾을 수 있어야 합니다.", savedInDb.isPresent());
        Assert.assertEquals("DB에 저장된 비밀번호는 변경되지 않아야 합니다.", originalPasscode, savedInDb.get().getLetterPasscode());
    }


    @Test(expected = NoSuchElementException.class)
    public void testUpdateRecipient_Fail_NotFoundOrAlreadyDeleted() throws Exception {
        // Given
        Integer nonExistentLetterSeq = 9999;
        RecipientEntity updateRequest = createValidRecipientEntity();
        String anyPasscode = "anypass";

        // When
        recipientService.updateRecipient(updateRequest, nonExistentLetterSeq, anyPasscode);

        // Then: NoSuchElementException 발생해야 함
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateRecipient_Fail_InvalidPasscode() throws Exception {
        // Given
        String wrongPasscode = "wrongpass";
        Integer letterSeqToUpdate = testRecipient.getLetterSeq();
        RecipientEntity updateRequest = createValidRecipientEntity();

        // When
        recipientService.updateRecipient(updateRequest, letterSeqToUpdate, wrongPasscode);

        // Then: IllegalArgumentException 발생해야 함
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateRecipient_Fail_PasscodeNull() throws Exception {
        // Given
        Integer letterSeqToUpdate = testRecipient.getLetterSeq();
        RecipientEntity updateRequest = createValidRecipientEntity();

        // When
        recipientService.updateRecipient(updateRequest, letterSeqToUpdate, null);

        // Then: IllegalArgumentException 발생해야 함
    }

    // --- 게시물 삭제 (deleteRecipient) 테스트 ---

    @Test
    public void testDeleteRecipient_Success_SoftDeletesComments() throws Exception {
        // Given (setup에서 otherRecipient와 댓글 2개(N), 1개(Y) 생성)
        String correctPasscode = otherRecipient.getLetterPasscode();
        Integer letterSeqToDelete = otherRecipient.getLetterSeq();

        // 삭제 전 활성 댓글 수 확인 (다른 게시물에 연결된 활성 댓글만)
        Assert.assertEquals("삭제 전 게시물에 연결된 활성 댓글은 2개여야 합니다.",
                2, recipientCommentRepository.findByLetterLetterSeqAndDelFlagOrderByWriteTimeAsc(letterSeqToDelete, "N").size());
        // 삭제된 댓글은 여전히 Y
        Assert.assertEquals("삭제 전 게시물에 연결된 삭제된 댓글은 1개여야 합니다.",
                1, recipientCommentRepository.findByLetterLetterSeqAndDelFlagOrderByWriteTimeAsc(letterSeqToDelete, "Y").size());


        // When
        recipientService.deleteRecipient(letterSeqToDelete, correctPasscode);

        // Then
        // 1. 게시물이 소프트 삭제되었는지 확인
        Optional<RecipientEntity> foundRecipient = recipientRepository.findById(letterSeqToDelete);
        Assert.assertTrue("게시물이 존재해야 합니다.", foundRecipient.isPresent()); // 소프트 삭제이므로 엔티티는 여전히 존재
        Assert.assertEquals("게시물의 delFlag가 'Y'로 변경되어야 합니다.", "Y", foundRecipient.get().getDelFlag());

        // 2. 연관된 활성 댓글들이 소프트 삭제되었는지 확인 (delFlag가 'N'인 댓글은 없어야 함)
        List<RecipientCommentEntity> commentsAfterDeletion =
                recipientCommentRepository.findByLetterLetterSeqAndDelFlagOrderByWriteTimeAsc(letterSeqToDelete, "N");
        Assert.assertTrue("게시물에 연결된 활성 댓글이 없어야 합니다.", commentsAfterDeletion.isEmpty());

        // 3. 소프트 삭제된 댓글들이 실제 delFlag='Y'로 변경되었는지 확인
        List<RecipientCommentEntity> allCommentsRelatedToDeletedPost =
                recipientCommentRepository.findByLetterLetterSeqAndDelFlagOrderByWriteTimeAsc(letterSeqToDelete, "Y");
        // 기존에 'Y'였던 1개 + 새로 'Y'로 바뀐 2개 = 총 3개
        Assert.assertEquals("게시물에 연결된 모든 댓글 (소프트 삭제 포함)은 3개여야 합니다.", 3, allCommentsRelatedToDeletedPost.size());

        // 추가: 다른 게시물의 댓글은 영향을 받지 않았는지 확인 (없다면 해당 테스트는 불필요)
        // 예를 들어 setup에 다른 게시물을 만들고 거기에 댓글을 달았다면
        // Assert.assertEquals("다른 게시물의 댓글은 여전히 1개여야 합니다.", 1, recipientCommentRepository.findByLetterLetterSeqAndDelFlagOrderByWriteTimeAsc(otherRecipient.getLetterSeq(), "N").size());
    }

    @Test(expected = NoSuchElementException.class)
    public void testDeleteRecipient_Fail_NotFoundOrAlreadyDeleted() throws Exception {
        // Given
        Integer nonExistentLetterSeq = 9999;
        String anyPasscode = "anypass";

        // When
        recipientService.deleteRecipient(nonExistentLetterSeq, anyPasscode);

        // Then: NoSuchElementException 발생해야 함
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteRecipient_Fail_InvalidPasscode() throws Exception {
        // Given
        String wrongPasscode = "wrongpass";
        Integer letterSeqToDelete = testRecipient.getLetterSeq();

        // When
        recipientService.deleteRecipient(letterSeqToDelete, wrongPasscode);

        // Then: IllegalArgumentException 발생해야 함
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteRecipient_Fail_PasscodeNullForDeletion() throws Exception {
        // Given
        Integer letterSeqToDelete = testRecipient.getLetterSeq();

        // When
        recipientService.deleteRecipient(letterSeqToDelete, null);

        // Then: IllegalArgumentException 발생해야 함
    }
}