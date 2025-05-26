package kodanect.domain.recipient.repository;

import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {kodanect.KodanectBootApplication.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class RecipientCommentRepositoryTest {

    @Autowired
    private RecipientCommentRepository recipientCommentRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    private RecipientEntity testRecipient;
    private RecipientEntity otherRecipient;

    @Before
    public void setup() {
        testRecipient = new RecipientEntity();
        testRecipient.setLetterTitle("테스트 게시물 제목");
        testRecipient.setLetterWriter("테스트 작성자");
        testRecipient.setLetterPasscode("test1234");
        testRecipient.setLetterContents("테스트 게시물 내용입니다.");
        testRecipient.setAnonymityFlag("N");
        testRecipient.setDelFlag("N");
        testRecipient.setReadCount(0);
        testRecipient.setRecipientYear("2024");
        testRecipient.setOrganCode("ORGAN001");
        testRecipient.setWriteTime(LocalDateTime.now().minusDays(1));
        testRecipient = recipientRepository.save(testRecipient);

        otherRecipient = new RecipientEntity();
        otherRecipient.setLetterTitle("다른 게시물 제목");
        otherRecipient.setLetterWriter("다른 작성자");
        otherRecipient.setLetterPasscode("otherabcd");
        otherRecipient.setLetterContents("다른 게시물 내용입니다.");
        otherRecipient.setAnonymityFlag("N");
        otherRecipient.setDelFlag("N");
        otherRecipient.setReadCount(0);
        otherRecipient.setRecipientYear("2024");
        otherRecipient.setOrganCode("ORGAN002");
        otherRecipient.setWriteTime(LocalDateTime.now().minusDays(2));
        otherRecipient = recipientRepository.save(otherRecipient);
    }

    @After
    public void teardown() {
        // @Transactional이 롤백을 처리하므로, 여기서 특별히 할 일 없음.
    }

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

    @Test
    public void testSaveAndFindComment() {
        // Given
        String commentWriter = "댓글 작성자1";
        String commentContents = "첫 번째 댓글 내용입니다.";
        String commentPasscode = "comment123";
        LocalDateTime writeTime = LocalDateTime.now().minusMinutes(5);

        RecipientCommentEntity newComment = createAndSaveComment(testRecipient, commentWriter, commentContents, commentPasscode, "N", writeTime);

        // When
        Optional<RecipientCommentEntity> foundCommentOptional = recipientCommentRepository.findById(newComment.getCommentSeq());

        // Then
        Assert.assertTrue("저장된 댓글을 찾을 수 있어야 합니다.", foundCommentOptional.isPresent());
        RecipientCommentEntity foundComment = foundCommentOptional.get();
        Assert.assertEquals("게시물 ID가 일치해야 합니다.", testRecipient.getLetterSeq(), foundComment.getLetter().getLetterSeq());
        Assert.assertEquals("댓글 작성자가 일치해야 합니다.", commentWriter, foundComment.getCommentWriter());
        Assert.assertEquals("댓글 내용이 일치해야 합니다.", commentContents, foundComment.getContents());
        Assert.assertEquals("댓글 비밀번호가 일치해야 합니다.", commentPasscode, foundComment.getCommentPasscode());
        Assert.assertEquals("삭제 플래그가 일치해야 합니다.", "N", foundComment.getDelFlag());
        Assert.assertNotNull("작성 시간이 null이 아니어야 합니다.", foundComment.getWriteTime());
    }

    @Test
    public void testFindByLetterLetterSeqAndDelFlagOrderByWriteTimeAsc() {
        // Given
        // 테스트 게시물(testRecipient)에 댓글 추가 (정렬 순서 및 삭제 플래그 고려)
        createAndSaveComment(testRecipient, "댓글A", "내용A", "passA", "Y", LocalDateTime.now().minusMinutes(30)); // 삭제된 댓글
        RecipientCommentEntity comment1 = createAndSaveComment(testRecipient, "댓글C", "내용C", "passC", "N", LocalDateTime.now().minusMinutes(25)); // 시간은 더 이르지만, 나중에 저장
        RecipientCommentEntity comment2 = createAndSaveComment(testRecipient, "댓글B", "내용B", "passB", "N", LocalDateTime.now().minusMinutes(20));
        RecipientCommentEntity comment3 = createAndSaveComment(testRecipient, "댓글D", "내용D", "passD", "N", LocalDateTime.now().minusMinutes(15));

        // 다른 게시물(otherRecipient)에도 댓글 추가 (조회 대상이 아님)
        createAndSaveComment(otherRecipient, "다른게시물댓글", "다른내용", "otherpass", "N", LocalDateTime.now().minusMinutes(10));

        // When
        List<RecipientCommentEntity> foundComments = recipientCommentRepository.findByLetterLetterSeqAndDelFlagOrderByWriteTimeAsc(testRecipient.getLetterSeq(), "N");

        // Then
        Assert.assertNotNull("조회된 댓글 목록은 null이 아니어야 합니다.", foundComments);
        Assert.assertEquals("삭제되지 않은 댓글은 3개여야 합니다.", 3, foundComments.size());

        // 작성 시간 오름차순으로 정렬되었는지 확인
        // 실제 저장된 객체의 시간을 기준으로 정렬되어야 합니다.
        List<RecipientCommentEntity> expectedOrder = foundComments.stream()
                .sorted(Comparator.comparing(RecipientCommentEntity::getWriteTime))
                .collect(Collectors.toList());

        Assert.assertEquals("댓글이 작성 시간 오름차순으로 정렬되어야 합니다.", expectedOrder, foundComments);
        Assert.assertEquals("첫 번째 댓글의 내용이 '내용C'여야 합니다.", comment1.getContents(), foundComments.get(0).getContents());
        Assert.assertEquals("두 번째 댓글의 내용이 '내용B'여야 합니다.", comment2.getContents(), foundComments.get(1).getContents());
        Assert.assertEquals("세 번째 댓글의 내용이 '내용D'여야 합니다.", comment3.getContents(), foundComments.get(2).getContents());

        // 삭제된 댓글은 포함되지 않았는지 확인
        boolean containsDeletedComment = foundComments.stream()
                .anyMatch(c -> c.getCommentWriter().equals("댓글A"));
        Assert.assertFalse("삭제된 댓글은 포함되지 않아야 합니다.", containsDeletedComment);
    }

    @Test
    public void testFindByCommentSeqAndDelFlag() {
        // Given
        RecipientCommentEntity activeComment = createAndSaveComment(testRecipient, "활성댓글", "활성 댓글 내용", "activepass", "N", LocalDateTime.now());
        RecipientCommentEntity deletedComment = createAndSaveComment(testRecipient, "삭제댓글", "삭제 댓글 내용", "deletepass", "Y", LocalDateTime.now());

        // When
        Optional<RecipientCommentEntity> foundActiveComment = recipientCommentRepository.findByCommentSeqAndDelFlag(activeComment.getCommentSeq(), "N");
        Optional<RecipientCommentEntity> foundDeletedCommentActiveFlag = recipientCommentRepository.findByCommentSeqAndDelFlag(deletedComment.getCommentSeq(), "N"); // 삭제된 댓글을 활성 플래그로 조회 시
        Optional<RecipientCommentEntity> foundActiveCommentDeletedFlag = recipientCommentRepository.findByCommentSeqAndDelFlag(activeComment.getCommentSeq(), "Y"); // 활성 댓글을 삭제 플래그로 조회 시

        // Then
        Assert.assertTrue("활성 댓글과 'N' 플래그로 조회 시 찾아져야 합니다.", foundActiveComment.isPresent());
        Assert.assertEquals("조회된 댓글의 내용이 일치해야 합니다.", activeComment.getContents(), foundActiveComment.get().getContents());
        Assert.assertEquals("조회된 댓글의 delFlag가 'N'이어야 합니다.", "N", foundActiveComment.get().getDelFlag());

        Assert.assertFalse("삭제된 댓글을 'N' 플래그로 조회 시 찾아지지 않아야 합니다.", foundDeletedCommentActiveFlag.isPresent());
        Assert.assertFalse("활성 댓글을 'Y' 플래그로 조회 시 찾아지지 않아야 합니다.", foundActiveCommentDeletedFlag.isPresent());

        // 삭제된 댓글을 'Y' 플래그로 조회 시 찾아지는지 확인 (추가 테스트)
        Optional<RecipientCommentEntity> foundDeletedComment = recipientCommentRepository.findByCommentSeqAndDelFlag(deletedComment.getCommentSeq(), "Y");
        Assert.assertTrue("삭제된 댓글을 'Y' 플래그로 조회 시 찾아져야 합니다.", foundDeletedComment.isPresent());
        Assert.assertEquals("조회된 댓글의 내용이 일치해야 합니다.", deletedComment.getContents(), foundDeletedComment.get().getContents());
        Assert.assertEquals("조회된 댓글의 delFlag가 'Y'이어야 합니다.", "Y", foundDeletedComment.get().getDelFlag());
    }

    @Test
    public void testUpdateComment() {
        // Given
        RecipientCommentEntity comment = createAndSaveComment(testRecipient, "초기 작성자", "초기 내용", "oldpass", "N", LocalDateTime.now());
        Assert.assertNotNull("댓글이 저장되었는지 확인", comment.getCommentSeq());

        String updatedContents = "수정된 댓글 내용입니다.";
        String updatedPasscode = "newpass";
        String updatedDelFlag = "Y";

        // When
        comment.setContents(updatedContents);
        comment.setCommentPasscode(updatedPasscode);
        comment.setDelFlag(updatedDelFlag);
        RecipientCommentEntity updatedComment = recipientCommentRepository.save(comment);

        // Then
        Optional<RecipientCommentEntity> foundCommentOptional = recipientCommentRepository.findById(updatedComment.getCommentSeq());
        Assert.assertTrue("수정된 댓글을 찾을 수 있어야 합니다.", foundCommentOptional.isPresent());
        RecipientCommentEntity foundComment = foundCommentOptional.get();

        Assert.assertEquals("댓글 내용이 수정되었는지 확인", updatedContents, foundComment.getContents());
        Assert.assertEquals("댓글 비밀번호가 수정되었는지 확인", updatedPasscode, foundComment.getCommentPasscode());
        Assert.assertEquals("삭제 플래그가 수정되었는지 확인", updatedDelFlag, foundComment.getDelFlag());
    }

    @Test
    public void testFindAllComments() {
        // Given
        createAndSaveComment(testRecipient, "댓글 A", "내용 A", "passA", "N", LocalDateTime.now().minusMinutes(3));
        createAndSaveComment(testRecipient, "댓글 B", "내용 B", "passB", "N", LocalDateTime.now().minusMinutes(2));
        createAndSaveComment(otherRecipient, "댓글 C", "내용 C", "passC", "N", LocalDateTime.now().minusMinutes(1));

        // When
        List<RecipientCommentEntity> allComments = recipientCommentRepository.findAll();

        // Then
        Assert.assertNotNull("모든 댓글 목록이 null이 아니어야 합니다.", allComments);
        // setup 메서드에서 testRecipient, otherRecipient 생성 시 댓글이 추가되지 않으므로,
        // 현재 테스트 메서드에서 추가된 3개의 댓글 + 이전에 다른 테스트에서 추가된 댓글들 (transactional 롤백 전)
        // 에 따라 size가 달라질 수 있습니다. 최소 3개 이상인지 확인하는 것이 안전합니다.
        Assert.assertTrue("최소 3개의 댓글이 있어야 합니다.", allComments.size() >= 3);
    }
}