//package kodanect.domain.recipient.service.impl;
//
//import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
//import kodanect.domain.recipient.entity.RecipientCommentEntity;
//import kodanect.domain.recipient.entity.RecipientEntity;
//import kodanect.domain.recipient.repository.RecipientCommentRepository;
//import kodanect.domain.recipient.repository.RecipientRepository;
//import kodanect.domain.recipient.service.RecipientCommentService;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.Assert.*;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest
//@Transactional
//public class RecipientCommentServiceImplTest {
//    @Autowired
//    private RecipientCommentService recipientCommentService;
//
//    @Autowired
//    private RecipientCommentRepository recipientCommentRepository;
//
//    @Autowired
//    private RecipientRepository recipientRepository;
//
//    private RecipientEntity testRecipient; // 댓글 테스트용 게시물
//    private RecipientEntity deletedRecipient; // 삭제된 게시물 (댓글 작성 방지 테스트용)
//
//    private RecipientCommentEntity activeComment1; // 활성 댓글 (조회, 수정, 삭제 테스트용)
//    private RecipientCommentEntity activeComment2; // 활성 댓글 (조회 테스트용)
//    private RecipientCommentEntity deletedComment; // 삭제된 댓글 (조회 방지, 복원 방지 테스트용)
//
//    // 댓글 비밀번호 유효성 검사용 정규식
//    private static final String COMMENT_PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$";
//
//    @Before
//    public void setup() {
//        // 데이터 초기화 (다른 테스트의 영향을 받지 않도록)
//        recipientCommentRepository.deleteAll();
//        recipientRepository.deleteAll();
//
//        // 테스트 게시물 (활성)
//        testRecipient = RecipientEntity.builder()
//                .letterTitle("댓글 테스트용 게시물")
//                .letterWriter("게시물작성자")
//                .letterPasscode("boardpass1")
//                .letterContents("댓글 달릴 내용.")
//                .anonymityFlag("N")
//                .organCode("ORGAN001")
//                .recipientYear("2024")
//                .readCount(0)
//                .delFlag("N")
//                .writeTime(LocalDateTime.now().minusDays(1))
//                .build();
//        testRecipient = recipientRepository.save(testRecipient);
//
//        // 삭제된 게시물 (댓글 작성 방지 테스트용)
//        deletedRecipient = RecipientEntity.builder()
//                .letterTitle("삭제된 게시물")
//                .letterWriter("삭제된작성자")
//                .letterPasscode("delboardpass")
//                .letterContents("삭제된 내용.")
//                .anonymityFlag("N")
//                .organCode("ORGAN001")
//                .recipientYear("2023")
//                .readCount(0)
//                .delFlag("Y") // 삭제된 상태
//                .writeTime(LocalDateTime.now().minusDays(2))
//                .build();
//        deletedRecipient = recipientRepository.save(deletedRecipient);
//
//
//        // 활성 댓글 1 (조회, 수정, 삭제, 비밀번호 확인 테스트용)
//        activeComment1 = RecipientCommentEntity.builder()
//                .letter(testRecipient)
//                .commentWriter("댓글작성자1")
//                .contents("첫 번째 댓글입니다.")
//                .commentPasscode("comment1234")
//                .delFlag("N")
//                .writeTime(LocalDateTime.now().minusHours(2))
//                .build();
//        activeComment1 = recipientCommentRepository.save(activeComment1);
//
//        // 활성 댓글 2 (조회 테스트용)
//        activeComment2 = RecipientCommentEntity.builder()
//                .letter(testRecipient)
//                .commentWriter("댓글작성자2")
//                .contents("두 번째 댓글입니다.")
//                .commentPasscode("comment5678")
//                .delFlag("N")
//                .writeTime(LocalDateTime.now().minusHours(1))
//                .build();
//        activeComment2 = recipientCommentRepository.save(activeComment2);
//
//        // 삭제된 댓글 (조회 방지 테스트용)
//        deletedComment = RecipientCommentEntity.builder()
//                .letter(testRecipient)
//                .commentWriter("삭제된댓글")
//                .contents("이 댓글은 삭제되었습니다.")
//                .commentPasscode("delcomment")
//                .delFlag("Y")
//                .writeTime(LocalDateTime.now().minusHours(3))
//                .build();
//        deletedComment = recipientCommentRepository.save(deletedComment);
//    }
//
//    // --- 특정 게시물의 댓글 조회 (selectRecipientCommentByLetterSeq) 테스트 ---
//
//    @Test
//    public void testSelectRecipientCommentByLetterSeq_Success_ActiveCommentsOnly() throws Exception {
//        // Given
//        int letterSeq = testRecipient.getLetterSeq();
//
//        // When
//        List<RecipientCommentResponseDto> comments = recipientCommentService.selectRecipientCommentByLetterSeq(letterSeq);
//
//        // Then
//        Assert.assertNotNull("댓글 목록은 null이 아니어야 합니다.", comments);
//        Assert.assertEquals("활성 댓글 2개가 조회되어야 합니다.", 2, comments.size());
//
//        // 댓글 순서 (작성 시간 오름차순) 및 내용 확인
//        Assert.assertEquals("첫 번째 댓글은 activeComment1이어야 합니다.", activeComment1.getCommentSeq(), comments.get(0).getCommentSeq());
//        Assert.assertEquals("두 번째 댓글은 activeComment2이어야 합니다.", activeComment2.getCommentSeq(), comments.get(1).getCommentSeq());
//
//        Assert.assertEquals("첫 번째 댓글 내용 일치", activeComment1.getContents(), comments.get(0).getContents());
//        Assert.assertEquals("두 번째 댓글 내용 일치", activeComment2.getContents(), comments.get(1).getContents());
//
//        // 삭제된 댓글은 조회되지 않아야 함
//        Assert.assertFalse("삭제된 댓글은 목록에 포함되지 않아야 합니다.",
//                comments.stream().anyMatch(c -> c.getCommentSeq().equals(deletedComment.getCommentSeq())));
//    }
//
//    @Test
//    public void testSelectRecipientCommentByLetterSeq_Success_NoComments() throws Exception {
//        // Given
//        // 새로운 게시물 생성 (댓글 없음)
//        RecipientEntity newRecipient = RecipientEntity.builder()
//                .letterTitle("댓글 없는 게시물")
//                .letterWriter("새작성자")
//                .letterPasscode("newpass123")
//                .letterContents("내용")
//                .anonymityFlag("N")
//                .organCode("ORGAN001")
//                .recipientYear("2024")
//                .readCount(0)
//                .delFlag("N")
//                .writeTime(LocalDateTime.now().minusDays(1))
//                .build();
//        newRecipient = recipientRepository.save(newRecipient);
//
//        // When
//        List<RecipientCommentResponseDto> comments = recipientCommentService.selectRecipientCommentByLetterSeq(newRecipient.getLetterSeq());
//
//        // Then
//        Assert.assertNotNull("댓글 목록은 null이 아니어야 합니다.", comments);
//        Assert.assertTrue("댓글이 없으므로 빈 목록이 반환되어야 합니다.", comments.isEmpty());
//    }
//
//    @Test
//    public void testSelectRecipientCommentByLetterSeq_Success_NonExistentLetter() throws Exception {
//        // Given
//        int nonExistentLetterSeq = 9999;
//
//        // When
//        List<RecipientCommentResponseDto> comments = recipientCommentService.selectRecipientCommentByLetterSeq(nonExistentLetterSeq);
//
//        // Then
//        Assert.assertNotNull("댓글 목록은 null이 아니어야 합니다.", comments);
//        Assert.assertTrue("존재하지 않는 게시물의 댓글은 없어야 합니다.", comments.isEmpty());
//    }
//
//
//    // --- 댓글 작성 (insertComment) 테스트 ---
//
//    @Test
//    public void testInsertComment_Success() throws Exception {
//        // Given
//        RecipientCommentEntity newCommentRequest = RecipientCommentEntity.builder()
//                .letter(testRecipient) // 부모 게시물 설정
//                .commentWriter("새 댓글작성자")
//                .contents("새로운 댓글 내용입니다. <b>굵게</b>.") // HTML 태그 포함
//                .commentPasscode("newpass123")
//                .delFlag("N") // 기본값은 N
//                .build();
//
//        // When
//        RecipientCommentResponseDto savedCommentDto = recipientCommentService.insertComment(newCommentRequest);
//
//        // Then
//        Assert.assertNotNull("저장된 댓글 DTO는 null이 아니어야 합니다.", savedCommentDto);
//        Assert.assertNotNull("댓글 시퀀스가 부여되어야 합니다.", savedCommentDto.getCommentSeq());
//        Assert.assertEquals("부모 게시물 시퀀스가 일치해야 합니다.", testRecipient.getLetterSeq(), savedCommentDto.getLetterSeq());
//        Assert.assertEquals("작성자가 일치해야 합니다.", newCommentRequest.getCommentWriter(), savedCommentDto.getCommentWriter());
//        // HTML 필터링 후 내용 확인 (<b>태그가 제거되고 <br>은 유지되지만 여기선 br 허용했으므로 테스트에서 br 없으면 그대로 나올 것)
//        // 여기서는 Safelist.none().addTags("br") 이므로 <b> 태그는 제거되어야 합니다.
//        Assert.assertEquals("내용이 HTML 필터링되어야 합니다.", "새로운 댓글 내용입니다. 굵게.", savedCommentDto.getContents());
//        Assert.assertEquals("delFlag는 N이어야 합니다.", "N", savedCommentDto.getDelFlag());
//
//        // DB에서 직접 조회하여 확인
//        Optional<RecipientCommentEntity> savedInDb = recipientCommentRepository.findById(savedCommentDto.getCommentSeq());
//        Assert.assertTrue("DB에 저장된 댓글을 찾을 수 있어야 합니다.", savedInDb.isPresent());
//        Assert.assertEquals("DB의 내용도 필터링되어야 합니다.", "새로운 댓글 내용입니다. 굵게.", savedInDb.get().getContents());
//        Assert.assertEquals("DB에 저장된 비밀번호가 요청값과 일치해야 합니다.", newCommentRequest.getCommentPasscode(), savedInDb.get().getCommentPasscode());
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testInsertComment_Fail_NoParentLetter() throws Exception {
//        // Given
//        RecipientCommentEntity newCommentRequest = RecipientCommentEntity.builder()
//                .commentWriter("test")
//                .contents("내용")
//                .commentPasscode("testpass123")
//                .build(); // letter가 null
//
//        // When
//        recipientCommentService.insertComment(newCommentRequest);
//
//        // Then: IllegalArgumentException("댓글을 달 게시물 정보가 누락되었습니다.") 발생해야 함
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testInsertComment_Fail_NonExistentParentLetter() throws Exception {
//        // Given
//        RecipientEntity nonExistentLetter = RecipientEntity.builder().letterSeq(9999).build(); // 존재하지 않는 게시물
//        RecipientCommentEntity newCommentRequest = RecipientCommentEntity.builder()
//                .letter(nonExistentLetter)
//                .commentWriter("test")
//                .contents("내용")
//                .commentPasscode("testpass123")
//                .build();
//
//        // When
//        recipientCommentService.insertComment(newCommentRequest);
//
//        // Then: IllegalArgumentException("게시물을 찾을 수 없습니다: 9999") 발생해야 함
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testInsertComment_Fail_DeletedParentLetter() throws Exception {
//        // Given
//        RecipientCommentEntity newCommentRequest = RecipientCommentEntity.builder()
//                .letter(deletedRecipient) // 삭제된 게시물에 댓글 시도
//                .commentWriter("test")
//                .contents("내용")
//                .commentPasscode("testpass123")
//                .build();
//
//        // When
//        recipientCommentService.insertComment(newCommentRequest);
//
//        // Then: IllegalArgumentException("삭제된 게시물에는 댓글을 달 수 없습니다.") 발생해야 함
//    }
//
//    @Test(expected = Exception.class) // Exception("비밀번호는 영문 숫자 8자 이상 이어야 합니다.")
//    public void testInsertComment_Fail_InvalidPassword_TooShort() throws Exception {
//        // Given
//        RecipientCommentEntity newCommentRequest = RecipientCommentEntity.builder()
//                .letter(testRecipient)
//                .commentWriter("test")
//                .contents("내용")
//                .commentPasscode("short") // 너무 짧음
//                .build();
//
//        // When
//        recipientCommentService.insertComment(newCommentRequest);
//    }
//
//    @Test(expected = Exception.class)
//    public void testInsertComment_Fail_InvalidPassword_NoNumber() throws Exception {
//        // Given
//        RecipientCommentEntity newCommentRequest = RecipientCommentEntity.builder()
//                .letter(testRecipient)
//                .commentWriter("test")
//                .contents("내용")
//                .commentPasscode("abcdefghi") // 숫자 없음
//                .build();
//
//        // When
//        recipientCommentService.insertComment(newCommentRequest);
//    }
//
//    @Test(expected = Exception.class)
//    public void testInsertComment_Fail_InvalidPassword_NoLetter() throws Exception {
//        // Given
//        RecipientCommentEntity newCommentRequest = RecipientCommentEntity.builder()
//                .letter(testRecipient)
//                .commentWriter("test")
//                .contents("내용")
//                .commentPasscode("123456789") // 영문 없음
//                .build();
//
//        // When
//        recipientCommentService.insertComment(newCommentRequest);
//    }
//
//    @Test(expected = Exception.class) // Exception("댓글 내용은 필수 입력 항목입니다.")
//    public void testInsertComment_Fail_ContentsEmpty() throws Exception {
//        // Given
//        RecipientCommentEntity newCommentRequest = RecipientCommentEntity.builder()
//                .letter(testRecipient)
//                .commentWriter("test")
//                .contents("") // 빈 내용
//                .commentPasscode("testpass123")
//                .build();
//
//        // When
//        recipientCommentService.insertComment(newCommentRequest);
//    }
//
//    @Test(expected = Exception.class) // Exception("댓글 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)")
//    public void testInsertComment_Fail_ContentsOnlyHtmlTags() throws Exception {
//        // Given
//        RecipientCommentEntity newCommentRequest = RecipientCommentEntity.builder()
//                .letter(testRecipient)
//                .commentWriter("test")
//                .contents("<b> <i> <u> </b>") // 필터링 후 내용이 없어지는 경우
//                .commentPasscode("testpass123")
//                .build();
//
//        // When
//        recipientCommentService.insertComment(newCommentRequest);
//    }
//
//
//    // --- 댓글 수정 (updateComment) 테스트 ---
//
//    @Test
//    public void testUpdateComment_Success() throws Exception {
//        // Given
//        RecipientCommentEntity updateRequest = RecipientCommentEntity.builder()
//                .commentSeq(activeComment1.getCommentSeq()) // 수정할 댓글 시퀀스
//                .contents("수정된 댓글 내용입니다. <i>이탤릭</i>") // 새로운 내용
//                .modifierId("updater1") // 수정자 ID
//                .build();
//        String correctPassword = activeComment1.getCommentPasscode();
//
//        // When
//        RecipientCommentResponseDto updatedDto = recipientCommentService.updateComment(updateRequest, correctPassword);
//
//        // Then
//        Assert.assertNotNull("업데이트된 DTO는 null이 아니어야 합니다.", updatedDto);
//        Assert.assertEquals("댓글 시퀀스는 동일해야 합니다.", activeComment1.getCommentSeq(), updatedDto.getCommentSeq());
//        // Jsoup 필터링 후 내용 확인 (<i> 태그가 제거되어야 함)
//        Assert.assertEquals("내용이 HTML 필터링되어야 합니다.", "수정된 댓글 내용입니다. 이탤릭", updatedDto.getContents());
//        Assert.assertEquals("수정자 ID가 업데이트되어야 합니다.", "updater1", updatedDto.getModifierId());
//        Assert.assertNotNull("수정 시간이 업데이트되어야 합니다.", updatedDto.getModifyTime());
//
//        // DB에서 직접 조회하여 확인
//        Optional<RecipientCommentEntity> savedInDb = recipientCommentRepository.findById(activeComment1.getCommentSeq());
//        Assert.assertTrue("DB에 저장된 댓글을 찾을 수 있어야 합니다.", savedInDb.isPresent());
//        Assert.assertEquals("DB의 내용도 필터링되어야 합니다.", "수정된 댓글 내용입니다. 이탤릭", savedInDb.get().getContents());
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testUpdateComment_Fail_NotFoundOrDeleted() throws Exception {
//        // Given
//        RecipientCommentEntity updateRequest = RecipientCommentEntity.builder()
//                .commentSeq(9999) // 존재하지 않는 시퀀스
//                .contents("내용")
//                .build();
//        String anyPassword = "anypass";
//
//        // When
//        recipientCommentService.updateComment(updateRequest, anyPassword);
//
//        // Then: IllegalArgumentException("댓글을 찾을 수 없거나 이미 삭제되었습니다.") 발생해야 함
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testUpdateComment_Fail_PasswordMismatch() throws Exception {
//        // Given
//        RecipientCommentEntity updateRequest = RecipientCommentEntity.builder()
//                .commentSeq(activeComment1.getCommentSeq())
//                .contents("내용")
//                .build();
//        String wrongPassword = "wrongpass";
//
//        // When
//        recipientCommentService.updateComment(updateRequest, wrongPassword);
//
//        // Then: IllegalArgumentException("비밀번호가 일치하지 않습니다.") 발생해야 함
//    }
//
//    @Test(expected = IllegalArgumentException.class) // IllegalArgumentException("수정할 댓글 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)")
//    public void testUpdateComment_Fail_ContentsEmptyAfterFiltering() throws Exception {
//        // Given
//        RecipientCommentEntity updateRequest = RecipientCommentEntity.builder()
//                .commentSeq(activeComment1.getCommentSeq())
//                .contents("<b> <i> </i> </b>") // 필터링 후 빈 내용
//                .build();
//        String correctPassword = activeComment1.getCommentPasscode();
//
//        // When
//        recipientCommentService.updateComment(updateRequest, correctPassword);
//    }
//
//
//    // --- 댓글 삭제 (deleteComment) 테스트 ---
//
//    @Test
//    public void testDeleteComment_Success_SoftDelete() throws Exception {
//        // Given
//        Integer commentSeqToDelete = activeComment1.getCommentSeq();
//        String correctPassword = activeComment1.getCommentPasscode();
//
//        // When
//        recipientCommentService.deleteComment(commentSeqToDelete, correctPassword);
//
//        // Then
//        // DB에서 직접 조회하여 delFlag가 'Y'로 변경되었는지 확인
//        Optional<RecipientCommentEntity> deletedCommentOptional = recipientCommentRepository.findById(commentSeqToDelete);
//        Assert.assertTrue("삭제된 댓글 엔티티는 DB에 여전히 존재해야 합니다 (소프트 삭제).", deletedCommentOptional.isPresent());
//        Assert.assertEquals("댓글의 delFlag가 'Y'로 변경되어야 합니다.", "Y", deletedCommentOptional.get().getDelFlag());
//        Assert.assertNotNull("삭제 시간이 기록되어야 합니다.", deletedCommentOptional.get().getModifyTime());
//        // modifierId가 commentWriter로 설정되었는지 확인
//        Assert.assertEquals("modifierId가 댓글 작성자 ID로 설정되어야 합니다.", activeComment1.getCommentWriter(), deletedCommentOptional.get().getModifierId());
//
//        // selectRecipientCommentByLetterSeq에서 조회되지 않아야 함
//        List<RecipientCommentResponseDto> remainingComments = recipientCommentService.selectRecipientCommentByLetterSeq(testRecipient.getLetterSeq());
//        Assert.assertEquals("활성 댓글이 하나 줄어야 합니다.", 1, remainingComments.size());
//        Assert.assertFalse("삭제된 댓글은 목록에 포함되지 않아야 합니다.",
//                remainingComments.stream().anyMatch(c -> c.getCommentSeq().equals(commentSeqToDelete)));
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testDeleteComment_Fail_NotFoundOrDeleted() throws Exception {
//        // Given
//        Integer nonExistentCommentSeq = 9999;
//        String anyPassword = "anypass";
//
//        // When
//        recipientCommentService.deleteComment(nonExistentCommentSeq, anyPassword);
//
//        // Then: IllegalArgumentException("댓글을 찾을 수 없거나 이미 삭제되었습니다.") 발생해야 함
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testDeleteComment_Fail_PasswordMismatch() throws Exception {
//        // Given
//        Integer commentSeqToDelete = activeComment1.getCommentSeq();
//        String wrongPassword = "wrongpass";
//
//        // When
//        recipientCommentService.deleteComment(commentSeqToDelete, wrongPassword);
//
//        // Then: IllegalArgumentException("비밀번호가 일치하지 않습니다.") 발생해야 함
//    }
//
//
//    // --- 댓글 비밀번호 확인 (verifyCommentPassword) 테스트 ---
//
//    @Test
//    public void testVerifyCommentPassword_Success() throws Exception {
//        // Given
//        Integer commentSeq = activeComment1.getCommentSeq();
//        String correctPassword = activeComment1.getCommentPasscode();
//
//        // When
//        boolean isVerified = recipientCommentService.verifyCommentPassword(commentSeq, correctPassword);
//
//        // Then
//        Assert.assertTrue("올바른 비밀번호로 인증에 성공해야 합니다.", isVerified);
//    }
//
//    @Test
//    public void testVerifyCommentPassword_Fail_InvalidPassword() throws Exception {
//        // Given
//        Integer commentSeq = activeComment1.getCommentSeq();
//        String wrongPassword = "wrongpass";
//
//        // When
//        boolean isVerified = recipientCommentService.verifyCommentPassword(commentSeq, wrongPassword);
//
//        // Then
//        Assert.assertFalse("잘못된 비밀번호로 인증에 실패해야 합니다.", isVerified);
//    }
//
//    @Test
//    public void testVerifyCommentPassword_Fail_NotFoundOrDeleted() throws Exception {
//        // Given
//        Integer nonExistentCommentSeq = 9999; // 존재하지 않는 댓글
//        String anyPassword = "anypass";
//
//        // When
//        boolean isVerified = recipientCommentService.verifyCommentPassword(nonExistentCommentSeq, anyPassword);
//
//        // Then
//        Assert.assertFalse("존재하지 않는 댓글은 인증에 실패해야 합니다.", isVerified);
//
//        // Given - 삭제된 댓글
//        Integer deletedCommentSeq = deletedComment.getCommentSeq();
//        String deletedCommentPassword = deletedComment.getCommentPasscode();
//
//        // When
//        boolean isVerifiedDeleted = recipientCommentService.verifyCommentPassword(deletedCommentSeq, deletedCommentPassword);
//
//        // Then
//        Assert.assertFalse("삭제된 댓글은 인증에 실패해야 합니다.", isVerifiedDeleted);
//    }
//}