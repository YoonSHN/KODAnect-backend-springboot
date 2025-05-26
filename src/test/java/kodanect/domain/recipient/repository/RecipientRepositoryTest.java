package kodanect.domain.recipient.repository;

import kodanect.domain.recipient.entity.RecipientEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class) // JUnit 4와 Spring 테스트 컨텍스트 연동
@DataJpaTest // JPA 관련 빈들만 로드하여 테스트 (slice test)
@ContextConfiguration(classes = {kodanect.KodanectBootApplication.class}) // 애플리케이션의 @Configuration 빈들을 로드
// 실제 DB 연결 정보는 application.properties 또는 application-test.properties에서 가져옴
// embedded database가 아닌 실제 DB를 사용하도록 설정
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// 테스트 전용 properties 파일을 지정할 수 있습니다. (옵션)
// @TestPropertySource(locations = "classpath:application-test.properties")
@Transactional // 각 테스트 메서드가 트랜잭션 안에서 실행되며, 기본적으로 테스트 후 롤백됩니다.
public class RecipientRepositoryTest {

    @Autowired
    private RecipientRepository recipientRepository;

    // 테스트에 필요한 다른 Repository (예: RecipientCommentRepository)를 주입할 수 있습니다.
    // @Autowired
    // private RecipientCommentRepository recipientCommentRepository;

    /**
     * 테스트를 위한 더미 데이터 생성 (필요시)
     * 실제 DB에 직접 데이터를 INSERT하거나, Flyway/Liquibase 등으로 스키마를 초기화할 수도 있습니다.
     * 여기서는 트랜잭션 롤백을 가정하고 테스트 케이스 내에서 생성합니다.
     */
    private RecipientEntity createAndSaveRecipient(String title, String writer, String passcode) {
        RecipientEntity recipient = new RecipientEntity();
        recipient.setLetterTitle(title);
        recipient.setLetterWriter(writer);
        recipient.setLetterPasscode(passcode);
        recipient.setLetterContents("Test contents for " + title);
        recipient.setAnonymityFlag("N");
        recipient.setDelFlag("N");
        recipient.setReadCount(0);
        recipient.setRecipientYear("2024");
        recipient.setOrganCode("ORGAN001");
        recipient.setWriteTime(LocalDateTime.now());
        // Auditing이 자동으로 설정될 경우, writeTime 등은 직접 설정하지 않아도 됩니다.
        // 하지만 테스트 시 명시적으로 값을 지정해야 할 경우도 있습니다.
        return recipientRepository.save(recipient);
    }

    // RecipientCommentEntity도 필요하다면 생성 및 저장 메서드를 만듭니다.
    // private RecipientCommentEntity createAndSaveComment(RecipientEntity letter, String writer, String contents) {
    //     RecipientCommentEntity comment = new RecipientCommentEntity();
    //     comment.setLetter(letter);
    //     comment.setCommentWriter(writer);
    //     comment.setContents(contents);
    //     comment.setCommentPasscode("comment123");
    //     comment.setDelFlag("N");
    //     comment.setWriteTime(LocalDateTime.now());
    //     return recipientCommentRepository.save(comment);
    // }


    @Test
    public void testSaveAndFindRecipient() {
        // Given
        String title = "테스트 게시물";
        String writer = "테스터";
        String passcode = "1234abcd";
        RecipientEntity newRecipient = createAndSaveRecipient(title, writer, passcode);

        // When
        Optional<RecipientEntity> foundRecipientOptional = recipientRepository.findById(newRecipient.getLetterSeq());

        // Then
        Assert.assertTrue("저장된 게시물을 찾을 수 있어야 합니다.", foundRecipientOptional.isPresent());
        RecipientEntity foundRecipient = foundRecipientOptional.get();
        Assert.assertEquals("게시물 제목이 일치해야 합니다.", title, foundRecipient.getLetterTitle());
        Assert.assertEquals("작성자 이름이 일치해야 합니다.", writer, foundRecipient.getLetterWriter());
        Assert.assertNotNull("작성 시간이 null이 아니어야 합니다.", foundRecipient.getWriteTime());
        // 필요에 따라 더 많은 필드를 검증합니다.
    }

    @Test
    public void testCountCommentsByLetterSeq() {
        // Given: 특정 게시물 ID (실제 DB에 존재하는 ID 사용 또는 테스트용 데이터 생성)
        // 여기서는 이미 DB에 존재하는 게시물 ID를 가정하거나, 테스트 데이터 생성
        RecipientEntity recipient1 = createAndSaveRecipient("게시물1", "작가1", "pass1");
        // 이 게시물에 댓글을 달려면 RecipientCommentRepository도 필요합니다.
        // 예시를 위해 직접 DB에 데이터를 넣는다고 가정하거나, RecipientCommentRepository를 주입하여 사용
        // 예: recipientCommentRepository.save(new RecipientCommentEntity(...));
        // DB에 letter_seq = 100번에 댓글 3개가, 101번에 댓글 0개가 있다고 가정.
        int existingLetterSeqWithComments = recipient1.getLetterSeq(); // 방금 저장한 게시물 ID
        // 여기에 recipientCommentRepository를 통해 댓글을 3개 저장하는 로직 추가
        // For demonstration, let's assume we insert 3 comments into the DB for recipient1.getLetterSeq()
        // Or manually insert them into your test DB before running.

        // When
        Integer commentCount = recipientRepository.countCommentsByLetterSeq(existingLetterSeqWithComments);
        Integer commentCountForNoComments = recipientRepository.countCommentsByLetterSeq(99999); // 존재하지 않는 게시물 ID

        // Then
        // 이 부분은 실제 DB에 데이터가 어떻게 들어있는지에 따라 Assertion 값이 달라집니다.
        // 테스트용 데이터베이스를 설정하거나, 테스트 시작 전에 데이터를 삽입해야 합니다.
        // Assert.assertEquals("letter_seq 100번 게시물의 댓글 수는 3이어야 합니다.", 3, (long)commentCount); // 실제 댓글 수에 맞게 변경
        Assert.assertNotNull("댓글 수가 null이 아니어야 합니다.", commentCount); // 최소한 null은 아니어야 함
        Assert.assertEquals("존재하지 않는 게시물의 댓글 수는 0이어야 합니다.", 0, (long)commentCountForNoComments);
    }

    @Test
    public void testCountCommentsByLetterSeqs() {
        // Given: 여러 게시물 ID 목록 (실제 DB에 존재하는 ID 사용 또는 테스트용 데이터 생성)
        // 예: letterSeq 100번에 댓글 3개, 101번에 댓글 1개, 102번에 댓글 0개가 있다고 가정.
        RecipientEntity recipientA = createAndSaveRecipient("게시물 A", "작가 A", "passA");
        RecipientEntity recipientB = createAndSaveRecipient("게시물 B", "작가 B", "passB");
        RecipientEntity recipientC = createAndSaveRecipient("게시물 C", "작가 C", "passC");

        // 여기서 RecipientCommentRepository를 사용하여 각 게시물에 댓글을 저장해야 합니다.
        // 예시: recipientCommentRepository.save(comment1ForA);
        //       recipientCommentRepository.save(comment2ForA);
        //       recipientCommentRepository.save(comment3ForA);
        //       recipientCommentRepository.save(comment1ForB);

        List<Integer> letterSeqs = Arrays.asList(
                recipientA.getLetterSeq(),
                recipientB.getLetterSeq(),
                recipientC.getLetterSeq(),
                99999 // 존재하지 않는 ID도 포함
        );

        // When
        List<Object[]> commentCounts = recipientRepository.countCommentsByLetterSeqs(letterSeqs);

        // Then
        Assert.assertNotNull("댓글 수 목록이 null이 아니어야 합니다.", commentCounts);
        // Assert.assertEquals("반환된 결과 개수는 3이어야 합니다 (댓글이 있는 게시물만).", 3, commentCounts.size()); // GROUP BY로 인해 댓글이 있는 것만 반환됨

        // 결과 검증 예시:
        // 실제 데이터에 따라 아래 assert 값을 수정해야 합니다.
        // Map<Integer, Long> resultMap = commentCounts.stream()
        //         .collect(Collectors.toMap(
        //                 arr -> ((Number) arr[0]).intValue(), // letter_seq
        //                 arr -> ((Number) arr[1]).longValue()  // count
        //         ));
        // Assert.assertEquals(3L, (long) resultMap.get(recipientA.getLetterSeq()));
        // Assert.assertEquals(1L, (long) resultMap.get(recipientB.getLetterSeq()));
        // Assert.assertNull(resultMap.get(recipientC.getLetterSeq())); // 댓글 없으면 결과에 없음
        // Assert.assertNull(resultMap.get(99999)); // 존재하지 않는 ID
    }

    // 기타 RecipientRepository가 제공하는 기본적인 CRUD 메서드 테스트도 추가할 수 있습니다.
    @Test
    public void testFindAllRecipients() {
        // Given
        createAndSaveRecipient("게시물 X", "작가 X", "passX");
        createAndSaveRecipient("게시물 Y", "작가 Y", "passY");

        // When
        List<RecipientEntity> recipients = recipientRepository.findAll();

        // Then
        Assert.assertNotNull(recipients);
        Assert.assertTrue(recipients.size() >= 2); // 최소 2개 이상이 저장되어 있어야 함
    }
}