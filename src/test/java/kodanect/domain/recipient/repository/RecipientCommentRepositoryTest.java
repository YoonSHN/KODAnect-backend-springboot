package kodanect.domain.recipient.repository;

import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// QueryDSL 관련 임포트 추가
import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 DB 사용
// @ContextConfiguration(classes = Application.class) // 필요한 경우 주석 해제 (전체 컨텍스트 로드 시)
@ActiveProfiles("test")
public class RecipientCommentRepositoryTest {

    @Autowired
    private RecipientCommentRepository recipientCommentRepository;

    @Autowired
    private RecipientRepository recipientRepository; // RecipientEntity를 저장하기 위해 필요

    @Autowired
    private TestEntityManager entityManager;

    // JPAQueryFactory 빈을 테스트 컨텍스트에 제공하는 내부 설정 클래스 추가
    @TestConfiguration
    static class TestConfig {
        @Autowired
        private EntityManager entityManager; // @DataJpaTest가 자동으로 EntityManager를 주입해줍니다.

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
                .letterSeq(recipient) // RecipientEntity 객체 자체를 넘깁니다.
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
        // 모든 댓글 및 게시물 데이터 삭제 (테스트 간의 간섭 방지)
        // 외래 키 제약 조건 때문에 자식(댓글)부터 삭제해야 합니다.
        recipientCommentRepository.deleteAll();
        recipientRepository.deleteAll();
        entityManager.clear(); // 영속성 컨텍스트 초기화
    }

    // --- 테스트 메서드 ---

    @Test
    public void testSaveRecipientComment() {
        // Given
        RecipientEntity recipient = createRecipient("테스트 게시물", "1111", "작성자", "N", "ORG01", LocalDateTime.now());
        recipient = recipientRepository.save(recipient); // 부모 게시물 먼저 저장
        entityManager.flush();

        RecipientCommentEntity comment = createComment(recipient, "새로운 댓글입니다.", "댓글러", "N", LocalDateTime.now());

        // When
        RecipientCommentEntity savedComment = recipientCommentRepository.save(comment);
        entityManager.flush(); // DB에 즉시 반영

        // Then
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getCommentSeq()).isNotNull(); // AutoIncrement 확인
        assertThat(savedComment.getCommentContents()).isEqualTo("새로운 댓글입니다.");
        assertThat(savedComment.getLetterSeq().getLetterSeq()).isEqualTo(recipient.getLetterSeq()); // 부모 게시물 ID 확인
        assertThat(savedComment.getDelFlag()).isEqualTo("N");
    }

    @Test
    public void testFindByLetterSeqAndDelFlagOrderByWriteTimeAsc() {
        // Given
        RecipientEntity recipient = createRecipient("정렬 테스트 게시물", "2222", "정렬", "N", "ORG02", LocalDateTime.now());
        recipient = recipientRepository.save(recipient);
        entityManager.flush();

        // 시간을 생성하고 변수에 저장
        LocalDateTime time1 = LocalDateTime.now(); // 첫 번째 댓글 시간
        LocalDateTime time2 = LocalDateTime.now().plusHours(1); // 두 번째 댓글 시간
        LocalDateTime time3 = LocalDateTime.now().plusHours(2); // 삭제된 댓글 시간

        // comment2 (첫 번째 댓글)를 먼저 persist 하여 가장 작은 commentSeq를 할당받게 합니다.
        RecipientCommentEntity comment2 = createComment(recipient, "첫 번째 댓글", "C1", "N", time1); // 가장 이른 시간
        RecipientCommentEntity comment1 = createComment(recipient, "두 번째 댓글", "C2", "N", time2); // 중간 시간
        RecipientCommentEntity comment3Deleted = createComment(recipient, "삭제된 댓글", "CD", "Y", time3); // 가장 늦은 시간 (변수명 수정됨)

        // 댓글들을 저장합니다.
        recipientCommentRepository.saveAll(Arrays.asList(comment2, comment1, comment3Deleted));
        entityManager.flush(); // DB에 즉시 반영

        // When
        // letterSeq가 일치하고 delFlag가 'N'인 댓글들을 writeTime 오름차순으로 조회
        // Repository의 @Query 메서드 이름을 사용하고, RecipientEntity 객체를 직접 전달
        List<RecipientCommentEntity> foundComments =
                recipientCommentRepository.findCommentsByLetterSeqAndDelFlagSorted(
                        recipient, // RecipientEntity 객체 자체를 전달 (letterSeq 필드 대신)
                        "N"
                );

        // Then
        // 조회된 댓글이 두 개인지 확인 (삭제된 댓글 제외)
        assertThat(foundComments).hasSize(2);

        // writeTime 기준으로 올바르게 정렬되었는지 확인
        assertThat(foundComments.get(0).getCommentContents()).isEqualTo("첫 번째 댓글"); // time1
        assertThat(foundComments.get(1).getCommentContents()).isEqualTo("두 번째 댓글"); // time2

        // 각 댓글의 delFlag가 'N'인지 확인
        assertThat(foundComments.get(0).getDelFlag()).isEqualTo("N");
        assertThat(foundComments.get(1).getDelFlag()).isEqualTo("N");
    }

    @Test
    public void testFindByCommentSeqAndDelFlag() {
        // Given
        RecipientEntity recipient = createRecipient("단일 댓글 조회 테스트", "3333", "조회", "N", "ORG03", LocalDateTime.now());
        recipient = recipientRepository.save(recipient);
        entityManager.flush();

        RecipientCommentEntity comment1 = createComment(recipient, "조회할 댓글", "Finder", "N", LocalDateTime.now());
        // 변수명 수정: comment2_deleted -> comment2Deleted
        RecipientCommentEntity comment2Deleted = createComment(recipient, "조회 안될 댓글", "NoFinder", "Y", LocalDateTime.now().plusHours(1));

        // 댓글들을 persist하여 ID를 할당받도록 합니다.
        comment1 = entityManager.persist(comment1);
        comment2Deleted = entityManager.persist(comment2Deleted); // 수정된 변수명 사용
        entityManager.flush();
        entityManager.clear();

        // When
        // delFlag가 'N'인 comment1을 commentSeq로 조회
        Optional<RecipientCommentEntity> foundComment = recipientCommentRepository.findByCommentSeqAndDelFlag(comment1.getCommentSeq(), "N");
        // delFlag가 'Y'인 comment2를 commentSeq로 조회 (찾을 수 없어야 함)
        Optional<RecipientCommentEntity> notFoundComment = recipientCommentRepository.findByCommentSeqAndDelFlag(comment2Deleted.getCommentSeq(), "N");

        // Then
        // comment1은 찾을 수 있어야 하며, 내용이 일치해야 합니다.
        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getCommentContents()).isEqualTo("조회할 댓글");
        assertThat(foundComment.get().getDelFlag()).isEqualTo("N");

        // comment2는 delFlag가 'N'이 아니므로 찾을 수 없어야 합니다.
        assertThat(notFoundComment).isNotPresent();
    }

    @Test
    public void testCountCommentsByLetterSeqs() {
        // Given
        RecipientEntity recipient1 = createRecipient("게시물A", "aaaa", "작가A", "N", "ORG01", LocalDateTime.now().minusDays(3));
        RecipientEntity recipient2 = createRecipient("게시물B", "bbbb", "작가B", "N", "ORG02", LocalDateTime.now().minusDays(2));
        // 변수명 수정: recipient3_deletedPost -> recipient3DeletedPost
        RecipientEntity recipient3DeletedPost = createRecipient("게시물C (삭제된 게시물)", "cccc", "작가C", "Y", "ORG03", LocalDateTime.now().minusDays(1)); // 게시물 자체는 삭제됨

        recipient1 = recipientRepository.save(recipient1);
        recipient2 = recipientRepository.save(recipient2);
        recipient3DeletedPost = recipientRepository.save(recipient3DeletedPost);
        entityManager.flush();

        // recipient1에 댓글 2개 (N, N)
        entityManager.persist(createComment(recipient1, "R1 댓글1", "C1-1", "N", LocalDateTime.now().minusMinutes(50)));
        entityManager.persist(createComment(recipient1, "R1 댓글2", "C1-2", "N", LocalDateTime.now().minusMinutes(40)));
        entityManager.persist(createComment(recipient1, "R1 삭제 댓글", "C1-3", "Y", LocalDateTime.now().minusMinutes(30))); // 삭제된 댓글

        // recipient2에 댓글 1개 (N)
        entityManager.persist(createComment(recipient2, "R2 댓글1", "C2-1", "N", LocalDateTime.now().minusMinutes(20)));

        // recipient3 (삭제된 게시물)에 댓글 1개 (N)
        // 이 댓글은 댓글 자체의 delFlag가 'N'이므로 쿼리에서 카운트되어야 함.
        entityManager.persist(createComment(recipient3DeletedPost, "R3 댓글1", "C3-1", "N", LocalDateTime.now().minusMinutes(10)));

        entityManager.flush();
        entityManager.clear();

        List<Integer> letterSeqs = Arrays.asList(recipient1.getLetterSeq(), recipient2.getLetterSeq(), recipient3DeletedPost.getLetterSeq());

        // When
        List<Object[]> commentCounts = recipientCommentRepository.countCommentsByLetterSeqs(letterSeqs);

        // Then
        // recipient1: 2개 (N)
        // recipient2: 1개 (N)
        // recipient3_deletedPost: 1개 (댓글의 delFlag가 N이므로 카운트됨)
        assertThat(commentCounts).isNotNull().hasSize(3); // 예상 사이즈는 3

        boolean foundR1 = false;
        boolean foundR2 = false;
        boolean foundR3 = false;

        for (Object[] row : commentCounts) {
            Integer letterSeq = (Integer) row[0];
            // BigInteger로 받은 다음 longValue()를 호출하여 Long으로 변환합니다.
            Long count = ((Number) row[1]).longValue();

            if (letterSeq.equals(recipient1.getLetterSeq())) {
                assertThat(count).isEqualTo(2L);
                foundR1 = true;
            } else if (letterSeq.equals(recipient2.getLetterSeq())) {
                assertThat(count).isEqualTo(1L);
                foundR2 = true;
            } else if (letterSeq.equals(recipient3DeletedPost.getLetterSeq())) {
                assertThat(count).isEqualTo(1L);
                foundR3 = true;
            }
        }
        assertThat(foundR1).isTrue();
        assertThat(foundR2).isTrue();
        assertThat(foundR3).isTrue();
    }

    @Test
    public void testFindByLetterSeqAndDelFlag_Paging() {
        // Given
        RecipientEntity recipient = createRecipient("페이징 테스트 게시물", "4444", "페이저", "N", "ORG04", LocalDateTime.now());
        recipient = recipientRepository.save(recipient);
        entityManager.flush();

        // 6개의 활성 댓글 생성 (시간 순서대로)
        for (int i = 0; i < 6; i++) {
            entityManager.persist(createComment(recipient, "댓글 " + i, "Writer" + i, "N", LocalDateTime.now().plusMinutes(i)));
        }
        // 삭제된 댓글 생성
        entityManager.persist(createComment(recipient, "삭제된 댓글", "Del", "Y", LocalDateTime.now().plusMinutes(100))); // 삭제된 댓글

        entityManager.flush();
        entityManager.clear();

        // 첫 번째 페이지를 요청 (페이지 번호: 0, 페이지당 항목 수: 3)
        Pageable pageable = PageRequest.of(0, 3);

        // When
        // 게시물 ID와 'N' (활성) delFlag로 댓글 목록을 페이징하여 조회
        Page<RecipientCommentEntity> firstPage = recipientCommentRepository.findByLetterSeqAndDelFlag(recipient, "N", pageable);

        // Then
        // 조회 결과가 null이 아닌지 확인
        assertThat(firstPage).isNotNull();
        // 삭제된 댓글을 제외한 총 활성 댓글 수가 6개인지 확인
        assertThat(firstPage.getTotalElements()).isEqualTo(6);
        // 총 페이지 수가 2개인지 확인 (6개 / 3개 = 2 페이지)
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        // 현재 페이지 번호가 0인지 확인 (isZero() 사용)
        assertThat(firstPage.getNumber()).isZero(); // isEqualTo(0) -> isZero()로 수정
        // 현재 페이지의 댓글 수가 3개인지 확인
        assertThat(firstPage.getContent()).hasSize(3);
        // 첫 번째 댓글의 내용이 "댓글 0"인지 확인 (시간 오름차순 정렬 가정)
        assertThat(firstPage.getContent().get(0).getCommentContents()).isEqualTo("댓글 0");

        // 두 번째 페이지 (3개)
        pageable = PageRequest.of(1, 3);
        Page<RecipientCommentEntity> secondPage = recipientCommentRepository.findByLetterSeqAndDelFlag(recipient, "N", pageable);

        assertThat(secondPage).isNotNull();
        assertThat(secondPage.getNumber()).isEqualTo(1);
        assertThat(secondPage.getContent()).hasSize(3);
        assertThat(secondPage.getContent().get(0).getCommentContents()).isEqualTo("댓글 3");
    }

    @Test
    public void testFindPaginatedComments() {
        // Given
        RecipientEntity recipient = createRecipient("더 보기 댓글 테스트", "5555", "더보기", "N", "ORG05", LocalDateTime.now());
        recipient = recipientRepository.save(recipient);
        entityManager.flush();

        // 댓글들을 생성하고 ID를 할당받음.
        // 모든 개별 comment 변수들은 이제 직접 사용되지 않으므로, 선언하지 않고 persist만 수행합니다.
        entityManager.persist(createComment(recipient, "첫 댓글", "C1", "N", LocalDateTime.now().minusHours(3)));
        entityManager.persist(createComment(recipient, "두 번째 댓글", "C2", "N", LocalDateTime.now().minusHours(2)));
        entityManager.persist(createComment(recipient, "세 번째 댓글", "C3", "N", LocalDateTime.now().minusHours(1)));

        // 삭제된 댓글도 변수 선언 없이 persist만 수행합니다.
        entityManager.persist(createComment(recipient, "삭제 댓글", "CD", "Y", LocalDateTime.now().minusMinutes(30)));

        entityManager.persist(createComment(recipient, "네 번째 댓글", "C4", "N", LocalDateTime.now().minusMinutes(20)));
        entityManager.persist(createComment(recipient, "마지막 댓글", "C5", "N", LocalDateTime.now().minusMinutes(10)));

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 3); // 페이지 사이즈 3

        // When (첫 페이지 조회 - lastCommentId = null)
        List<RecipientCommentEntity> firstPageComments = recipientCommentRepository.findPaginatedComments(recipient, null, pageable);

        // Then (첫 페이지 결과 검증: "첫 댓글", "두 번째 댓글", "세 번째 댓글")
        assertThat(firstPageComments).isNotNull().hasSize(3);
        assertThat(firstPageComments.get(0).getCommentContents()).isEqualTo("첫 댓글");
        assertThat(firstPageComments.get(1).getCommentContents()).isEqualTo("두 번째 댓글");
        assertThat(firstPageComments.get(2).getCommentContents()).isEqualTo("세 번째 댓글");

        // When (두 번째 페이지 조회 - lastCommentId는 첫 페이지의 마지막 댓글 ID)
        Integer lastCommentIdOfFirstPage = firstPageComments.get(firstPageComments.size() - 1).getCommentSeq();
        List<RecipientCommentEntity> secondPageComments = recipientCommentRepository.findPaginatedComments(recipient, lastCommentIdOfFirstPage, pageable);

        // Then (두 번째 페이지 결과 검증: "네 번째 댓글", "마지막 댓글")
        // 삭제된 댓글은 delFlag='Y'이므로 포함되지 않음
        assertThat(secondPageComments).isNotNull().hasSize(2);
        assertThat(secondPageComments.get(0).getCommentContents()).isEqualTo("네 번째 댓글");
        assertThat(secondPageComments.get(1).getCommentContents()).isEqualTo("마지막 댓글");

        // When (더 이상 댓글이 없는 경우)
        Integer lastCommentIdOfSecondPage = secondPageComments.get(secondPageComments.size() - 1).getCommentSeq();
        List<RecipientCommentEntity> emptyPageComments = recipientCommentRepository.findPaginatedComments(recipient, lastCommentIdOfSecondPage, pageable);

        // Then
        assertThat(emptyPageComments).isNotNull().isEmpty();
    }
}