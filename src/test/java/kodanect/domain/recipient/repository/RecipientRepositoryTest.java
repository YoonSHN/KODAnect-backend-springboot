package kodanect.domain.recipient.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class RecipientRepositoryTest {
    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private TestEntityManager entityManager; // JPA 엔티티 관리 및 테스트 데이터 설정을 위해 사용

    // JPAQueryFactory 빈을 테스트 컨텍스트에 제공하는 내부 설정 클래스
    @TestConfiguration // 이 설정은 테스트에만 적용됩니다.
    static class TestConfig {
        @Autowired
        private EntityManager entityManager; // @DataJpaTest가 자동으로 EntityManager를 제공합니다.

        @Bean
        public JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(entityManager);
        }
    }

    // 테스트 데이터 생성 편의를 위한 메서드
    private RecipientEntity createRecipient(String title, String passcode, String writer, String delFlag, String organCode, LocalDateTime writeTime) {
        return RecipientEntity.builder()
                .letterTitle(title)
                .letterPasscode(passcode)
                .letterWriter(writer)
                .letterContents("테스트 편지 내용")
                .anonymityFlag("N")
                .recipientYear("2024")
                .organCode(organCode)
                .readCount(0)
                .delFlag(delFlag)
                .writeTime(writeTime)
                .writerId("testUser")
                .build();
    }

    // 테스트 데이터 생성 편의를 위한 메서드
    private RecipientCommentEntity createComment(RecipientEntity recipient, String contents, String writer, String delFlag, LocalDateTime writeTime) {
        return RecipientCommentEntity.builder()
                .letterSeq(recipient)
                .commentContents(contents)
                .commentWriter(writer)
                .commentPasscode("1234")
                .delFlag(delFlag)
                .writeTime(writeTime)
                .writerId("commenter")
                .build();
    }

    @Before
    @Transactional // 각 테스트 메서드 전에 실행되며, 트랜잭션으로 묶여 롤백됩니다.
    public void setup() {
        // 기존 데이터 삭제 (테스트 간의 간섭 방지)
        recipientRepository.deleteAll();
        entityManager.clear(); // 영속성 컨텍스트 초기화
    }

    // --- 테스트 메서드 ---

    @Test
    public void testSaveRecipient() {
        // Given
        RecipientEntity recipient = createRecipient("테스트 편지", "1234", "홍길동", "N", "ORG01", LocalDateTime.now());

        // When
        RecipientEntity savedRecipient = recipientRepository.save(recipient);
        entityManager.flush(); // DB에 즉시 반영

        // Then
        assertThat(savedRecipient).isNotNull();
        assertThat(savedRecipient.getLetterSeq()).isNotNull(); // AutoIncrement 확인
        assertThat(savedRecipient.getLetterTitle()).isEqualTo("테스트 편지");
        assertThat(savedRecipient.getDelFlag()).isEqualTo("N");
    }

    @Test
    public void testFindByIdWithComments() {
        // Given
        RecipientEntity recipient = createRecipient("댓글 포함 게시물", "5678", "김철수", "N", "ORG02", LocalDateTime.now().minusDays(2));
        recipient = recipientRepository.save(recipient); // 게시물 저장 후 ID 할당 받음
        entityManager.flush();

        // 댓글 추가
        RecipientCommentEntity comment1 = createComment(recipient, "첫 번째 댓글", "익명1", "N", LocalDateTime.now().minusDays(1));
        RecipientCommentEntity comment2 = createComment(recipient, "두 번째 댓글 (삭제됨)", "익명2", "Y", LocalDateTime.now().minusHours(10));
        RecipientCommentEntity comment3 = createComment(recipient, "세 번째 댓글", "익명3", "N", LocalDateTime.now().minusHours(5));

        // RecipientCommentEntity를 직접 저장하는 Repository가 필요합니다.
        // 테스트를 위해 직접 persist 합니다. 실제 앱에서는 RecipientCommentRepository를 사용할 것입니다.
        entityManager.persist(comment1);
        entityManager.persist(comment2); // 삭제된 댓글
        entityManager.persist(comment3);
        entityManager.flush();
        entityManager.clear(); // 영속성 컨텍스트 초기화 (새로운 조회 시 DB에서 가져오도록)

        // When
        Optional<RecipientEntity> foundRecipientOptional = recipientRepository.findByIdWithComments(recipient.getLetterSeq());

        // Then
        assertThat(foundRecipientOptional).isPresent();
        RecipientEntity foundRecipient = foundRecipientOptional.get();
        assertThat(foundRecipient.getLetterTitle()).isEqualTo("댓글 포함 게시물");
        assertThat(foundRecipient.getDelFlag()).isEqualTo("N"); // 게시물은 삭제되지 않음

        // 댓글 목록 확인 (delFlag='N'인 댓글만 가져와야 함)
        assertThat(foundRecipient.getComments()).isNotNull();
        assertThat(foundRecipient.getComments()).hasSize(2); // 삭제되지 않은 댓글만 포함
        assertThat(foundRecipient.getComments().get(0).getCommentContents()).isEqualTo("첫 번째 댓글");
        assertThat(foundRecipient.getComments().get(1).getCommentContents()).isEqualTo("세 번째 댓글");
        // 정렬 순서 확인 (writeTime ASC)
        assertThat(foundRecipient.getComments().get(0).getWriteTime()).isBefore(foundRecipient.getComments().get(1).getWriteTime());
    }

    @Test
    public void testCountCommentsByLetterSeq() {
        // Given
        RecipientEntity recipient = createRecipient("댓글 수 테스트", "0000", "테스터", "N", "ORG03", LocalDateTime.now());
        recipient = recipientRepository.save(recipient);
        entityManager.flush();

        entityManager.persist(createComment(recipient, "댓글1", "A", "N", LocalDateTime.now().minusMinutes(30)));
        entityManager.persist(createComment(recipient, "댓글2", "B", "N", LocalDateTime.now().minusMinutes(20)));
        entityManager.persist(createComment(recipient, "삭제 댓글", "C", "Y", LocalDateTime.now().minusMinutes(10)));
        entityManager.flush();
        entityManager.clear();

        // When
        Integer commentCount = recipientRepository.countCommentsByLetterSeq(recipient.getLetterSeq());

        // Then
        assertThat(commentCount).isEqualTo(2); // delFlag='N'인 댓글만 카운트
    }

    @Test
    public void testCountCommentsByLetterSeqs() {
        // Given
        RecipientEntity recipient1 = createRecipient("게시물1", "1111", "작가1", "N", "ORG01", LocalDateTime.now().minusHours(3));
        RecipientEntity recipient2 = createRecipient("게시물2", "2222", "작가2", "N", "ORG02", LocalDateTime.now().minusHours(2));
        RecipientEntity recipient3 = createRecipient("게시물3 (삭제됨)", "3333", "작가3", "Y", "ORG03", LocalDateTime.now().minusHours(1));

        recipient1 = recipientRepository.save(recipient1);
        recipient2 = recipientRepository.save(recipient2);
        recipient3 = recipientRepository.save(recipient3);
        entityManager.flush();

        // recipient1에 댓글 2개 (1개는 삭제)
        entityManager.persist(createComment(recipient1, "R1 댓글1", "X", "N", LocalDateTime.now().minusMinutes(50)));
        entityManager.persist(createComment(recipient1, "R1 댓글2 (삭제)", "Y", "Y", LocalDateTime.now().minusMinutes(40)));
        entityManager.persist(createComment(recipient1, "R1 댓글3", "Z", "N", LocalDateTime.now().minusMinutes(30)));

        // recipient2에 댓글 1개
        entityManager.persist(createComment(recipient2, "R2 댓글1", "P", "N", LocalDateTime.now().minusMinutes(20)));

        // recipient3에 댓글 1개 (게시물이 삭제됨)
        entityManager.persist(createComment(recipient3, "R3 댓글1", "Q", "N", LocalDateTime.now().minusMinutes(10)));

        entityManager.flush();
        entityManager.clear();

        List<Integer> letterSeqs = Arrays.asList(recipient1.getLetterSeq(), recipient2.getLetterSeq(), recipient3.getLetterSeq());

        // When
        List<Object[]> commentCounts = recipientRepository.countCommentsByLetterSeqs(letterSeqs);

        // Then
        assertThat(commentCounts).isNotNull().hasSize(3);

        // 결과 검증
        boolean foundR1 = false;
        boolean foundR2 = false;
        boolean foundR3 = false;

        for (Object[] row : commentCounts) {
            Integer letterSeq = (Integer) row[0];

            Long count = ((java.math.BigInteger) row[1]).longValue();

            if (letterSeq.equals(recipient1.getLetterSeq())) {
                assertThat(count).isEqualTo(2L);
                foundR1 = true;
            } else if (letterSeq.equals(recipient2.getLetterSeq())) {
                assertThat(count).isEqualTo(1L);
                foundR2 = true;
            } else if (letterSeq.equals(recipient3.getLetterSeq())) {
                assertThat(count).isEqualTo(1L);
                foundR3 = true;
            }
        }
        assertThat(foundR1).isTrue();
        assertThat(foundR2).isTrue();
        assertThat(foundR3).isTrue();
    }

    @Test
    @Transactional // 트랜잭션 내에서 엔티티 변경 후 flush, clear 필요
    public void testSoftDeleteRecipientAndComments() {
        // Given
        RecipientEntity recipient = createRecipient("소프트 삭제 테스트", "abcd", "삭제예정", "N", "ORG04", LocalDateTime.now());
        recipient = recipientRepository.save(recipient);
        entityManager.flush();

        RecipientCommentEntity comment1 = createComment(recipient, "삭제될 댓글1", "Comm1", "N", LocalDateTime.now().minusHours(1));
        RecipientCommentEntity comment2 = createComment(recipient, "삭제될 댓글2", "Comm2", "N", LocalDateTime.now().minusHours(2));
        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.flush();

        // 중요한 부분: softDelete를 호출하기 전에 댓글 컬렉션을 로드해야 합니다.
        // LazyLoding이므로, 컬렉션에 접근하기 전에 트랜잭션이 활성화되어 있어야 합니다.
        entityManager.refresh(recipient); // DB에서 최신 상태로 다시 로드하여 comments 컬렉션을 초기화

        // When
        recipient.softDelete(); // 엔티티의 비즈니스 로직 호출
        entityManager.flush(); // 변경사항을 DB에 반영

        // Then
        // 1. 게시물 자체가 delFlag가 'Y'인지 확인
        entityManager.clear(); // 영속성 컨텍스트를 비워 DB에서 새로운 값을 가져오도록 함
        Optional<RecipientEntity> deletedRecipientOptional = recipientRepository.findById(recipient.getLetterSeq());
        assertThat(deletedRecipientOptional).isPresent();
        assertThat(deletedRecipientOptional.get().getDelFlag()).isEqualTo("Y");

        // 삭제된 게시물은 findByIdWithComments 쿼리에서 가져오지 않도록 설정되어 있으므로,이 쿼리를 사용하면 Optional이 비어있어야 합니다.
        Optional<RecipientEntity> foundAfterSoftDelete = recipientRepository.findByIdWithComments(recipient.getLetterSeq());
        assertThat(foundAfterSoftDelete).isNotPresent(); // 게시물 자체가 delFlag='Y'이므로 조회되지 않아야 함
    }

    @Test
    public void testFindActivePostsByLastId_FirstPage() {
        // Given
        // 역순으로 생성 (letterSeq DESC)
        RecipientEntity post1 = createRecipient("제목1", "p1", "w1", "N", "O1", LocalDateTime.now().minusDays(5)); // letterSeq: 1
        RecipientEntity post2 = createRecipient("제목2", "p2", "w2", "N", "O2", LocalDateTime.now().minusDays(4)); // letterSeq: 2
        RecipientEntity post3 = createRecipient("제목3", "p3", "w3", "N", "O3", LocalDateTime.now().minusDays(3)); // letterSeq: 3
        RecipientEntity post4_deleted = createRecipient("제목4 (삭제)", "p4", "w4", "Y", "O4", LocalDateTime.now().minusDays(2)); // letterSeq: 4 (삭제됨)
        RecipientEntity post5 = createRecipient("제목5", "p5", "w5", "N", "O5", LocalDateTime.now().minusDays(1)); // letterSeq: 5

        recipientRepository.saveAll(Arrays.asList(post1, post2, post3, post4_deleted, post5));
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 3); // 첫 페이지, 3개 조회

        // When
        List<RecipientEntity> posts = recipientRepository.findActivePostsByLastId(null, pageable);

        // Then
        assertThat(posts).isNotNull();
        assertThat(posts).hasSize(3); // 활성 게시물 중 최신 3개
        assertThat(posts.get(0).getLetterTitle()).isEqualTo("제목5"); // 최신 순 (DESC)
        assertThat(posts.get(1).getLetterTitle()).isEqualTo("제목3");
        assertThat(posts.get(2).getLetterTitle()).isEqualTo("제목2");
        assertThat(posts).extracting(RecipientEntity::getDelFlag).containsOnly("N"); // 삭제되지 않은 게시물만
    }

    @Test
    public void testFindActivePostsByLastId_NextPage() {
        // Given
        RecipientEntity post1 = createRecipient("제목1", "p1", "w1", "N", "O1", LocalDateTime.now().minusDays(5));
        RecipientEntity post2 = createRecipient("제목2", "p2", "w2", "N", "O2", LocalDateTime.now().minusDays(4));
        RecipientEntity post3 = createRecipient("제목3", "p3", "w3", "N", "O3", LocalDateTime.now().minusDays(3));
        RecipientEntity post4 = createRecipient("제목4", "p4", "w4", "N", "O4", LocalDateTime.now().minusDays(2));
        RecipientEntity post5 = createRecipient("제목5", "p5", "w5", "N", "O5", LocalDateTime.now().minusDays(1));

        recipientRepository.saveAll(Arrays.asList(post1, post2, post3, post4, post5));
        entityManager.flush();
        entityManager.clear();

        // 첫 번째 페이지 조회 (가장 높은 ID부터 시작, 가정: 5, 4, 3)
        // lastId를 3으로 설정하여 그보다 작은 ID를 가져옵니다.
        // 다음 페이지는 2, 1이 될 것
        Pageable pageable = PageRequest.of(0, 2); // 다음 페이지에서 2개 조회

        // When
        // lastId는 이전 페이지에서 가장 작은 letterSeq가 됩니다. (예: 제목3의 ID)
        List<RecipientEntity> posts = recipientRepository.findActivePostsByLastId(post3.getLetterSeq(), pageable);

        // Then
        assertThat(posts).isNotNull();
        assertThat(posts).hasSize(2);
        assertThat(posts.get(0).getLetterTitle()).isEqualTo("제목2"); // 3보다 작은 letterSeq 중 최신
        assertThat(posts.get(1).getLetterTitle()).isEqualTo("제목1");
        assertThat(posts).extracting(RecipientEntity::getDelFlag).containsOnly("N");
    }

    @Test
    public void testFindActivePostsByLastId_NoMorePosts() {
        // Given
        RecipientEntity post1 = createRecipient("제목1", "p1", "w1", "N", "O1", LocalDateTime.now().minusDays(2));
        RecipientEntity post2 = createRecipient("제목2", "p2", "w2", "N", "O2", LocalDateTime.now().minusDays(1));
        recipientRepository.saveAll(Arrays.asList(post1, post2));
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 3); // 3개 조회 요청

        // When
        // 모든 게시물이 조회되었고, 더 이상 조회할 게시물이 없을 때 (lastId가 가장 작은 게시물의 ID보다 작을 때)
        List<RecipientEntity> posts = recipientRepository.findActivePostsByLastId(post1.getLetterSeq() - 1, pageable);

        // Then
        assertThat(posts).isNotNull();
        assertThat(posts).isEmpty(); // 결과가 없어야 함
    }
}