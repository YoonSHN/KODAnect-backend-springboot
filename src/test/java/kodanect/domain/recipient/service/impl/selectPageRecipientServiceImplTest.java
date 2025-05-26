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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class selectPageRecipientServiceImplTest {

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private RecipientCommentRepository recipientCommentRepository;

    private RecipientEntity testRecipient1; // 조회수, 댓글 수 테스트용
    private RecipientEntity testRecipient2; // 페이징 테스트용
    private RecipientEntity testRecipient3; // 페이징 테스트용
    private RecipientEntity deletedRecipient; // 조회수, 댓글수 증가에서 제외

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
                .letterWriter("테스터") // 10바이트 이하의 유효한 값
                .letterTitle("테스트 제목입니다.")
                .letterContents("테스트 내용입니다.") // Jsoup 필터링 후 공백 없음
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
        // 각 테스트 전에 필요한 초기화 작업
        recipientCommentRepository.deleteAll(); // 댓글 먼저 삭제 (외래 키 제약 조건 고려)
        recipientRepository.deleteAll(); // 모든 게시물 삭제

        // 테스트 게시물 1
        testRecipient1 = RecipientEntity.builder()
                .letterTitle("제목1")
                .letterWriter("작성자1")
                .letterPasscode("pass1111")
                .letterContents("내용1")
                .anonymityFlag("N")
                .organCode("ORGAN001")
                .recipientYear("2023")
                .readCount(0)
                .delFlag("N")
                .writeTime(LocalDateTime.now().minusHours(3))
                .build();
        testRecipient1 = recipientRepository.save(testRecipient1);
        // testRecipient1에 댓글 2개 추가 (활성)
        createAndSaveComment(testRecipient1, "댓글1-1", "내용1-1", "cpass1", "N", LocalDateTime.now().minusMinutes(30));
        createAndSaveComment(testRecipient1, "댓글1-2", "내용1-2", "cpass2", "N", LocalDateTime.now().minusMinutes(20));
        // testRecipient1에 삭제된 댓글 1개 추가
        createAndSaveComment(testRecipient1, "삭제댓글1", "삭제됨", "cpass3", "Y", LocalDateTime.now().minusMinutes(40));


        // 테스트 게시물 2
        testRecipient2 = RecipientEntity.builder()
                .letterTitle("제목2")
                .letterWriter("작성자2")
                .letterPasscode("pass2222")
                .letterContents("내용2")
                .anonymityFlag("N")
                .organCode("ORGAN002")
                .recipientYear("2022")
                .readCount(5)
                .delFlag("N")
                .writeTime(LocalDateTime.now().minusHours(2))
                .build();
        testRecipient2 = recipientRepository.save(testRecipient2);
        // testRecipient2에 댓글 1개 추가 (활성)
        createAndSaveComment(testRecipient2, "댓글2-1", "내용2-1", "cpass4", "N", LocalDateTime.now().minusMinutes(15));


        // 테스트 게시물 3
        testRecipient3 = RecipientEntity.builder()
                .letterTitle("제목3")
                .letterWriter("작성자3")
                .letterPasscode("pass3333")
                .letterContents("내용3")
                .anonymityFlag("N")
                .organCode("ORGAN003")
                .recipientYear("2021")
                .readCount(10)
                .delFlag("N")
                .writeTime(LocalDateTime.now().minusHours(1))
                .build();
        testRecipient3 = recipientRepository.save(testRecipient3);
        // testRecipient3에는 댓글 없음

        // 삭제된 게시물
        deletedRecipient = RecipientEntity.builder()
                .letterTitle("삭제된 제목")
                .letterWriter("삭제된작성자")
                .letterPasscode("delpass")
                .letterContents("삭제된 내용")
                .anonymityFlag("N")
                .organCode("ORGAN001")
                .recipientYear("2020")
                .readCount(0)
                .delFlag("Y") // 삭제됨
                .writeTime(LocalDateTime.now().minusHours(4))
                .build();
        deletedRecipient = recipientRepository.save(deletedRecipient);
        // 삭제된 게시물에는 댓글 추가하지 않음
    }


    // --- selectRecipient (특정 게시물 조회) 테스트 ---

    @Test
    public void testSelectRecipient_Success_IncreasesReadCountAndGetsCommentCount() throws Exception {
        // Given
        int initialReadCount = testRecipient1.getReadCount();
        Integer letterSeq = testRecipient1.getLetterSeq();

        // When
        RecipientResponseDto resultDto = recipientService.selectRecipient(letterSeq);

        // Then
        Assert.assertNotNull("조회된 게시물 DTO는 null이 아니어야 합니다.", resultDto);
        Assert.assertEquals("게시물 번호가 일치해야 합니다.", letterSeq, resultDto.getLetterSeq());
        Assert.assertEquals("조회수가 1 증가해야 합니다.", initialReadCount + 1, resultDto.getReadCount());
        Assert.assertEquals("활성 댓글 수가 올바르게 설정되어야 합니다.", 2, resultDto.getCommentCount()); // 2개 활성 댓글 예상

        // DB에서 직접 조회하여 조회수 증가 확인
        Optional<RecipientEntity> updatedEntity = recipientRepository.findById(letterSeq);
        Assert.assertTrue("DB에 저장된 엔티티를 찾을 수 있어야 합니다.", updatedEntity.isPresent());
        Assert.assertEquals("DB의 조회수도 1 증가해야 합니다.", initialReadCount + 1, updatedEntity.get().getReadCount());
    }

    @Test(expected = NoSuchElementException.class)
    public void testSelectRecipient_Fail_NotFound() throws Exception {
        // Given
        int nonExistentLetterSeq = 9999;

        // When
        recipientService.selectRecipient(nonExistentLetterSeq);

        // Then: NoSuchElementException 발생해야 함
    }

    @Test(expected = NoSuchElementException.class)
    public void testSelectRecipient_Fail_AlreadyDeleted() throws Exception {
        // Given
        Integer deletedLetterSeq = deletedRecipient.getLetterSeq();

        // When
        recipientService.selectRecipient(deletedLetterSeq);

        // Then: NoSuchElementException 발생해야 함 (delFlag='Y'로 필터링됨)
    }

    // --- selectRecipientListPaged (페이징 처리된 목록 조회) 테스트 ---

    @Test
    public void testSelectRecipientListPaged_Success_DefaultPage() throws Exception {
        // Given
        RecipientEntity searchCondition = new RecipientEntity(); // 검색 조건 없음
        Pageable pageable = PageRequest.of(0, 2, Sort.by("letterSeq").ascending()); // 첫 페이지, 2개씩, letterSeq 오름차순

        // When
        Page<RecipientResponseDto> resultPage = recipientService.selectRecipientListPaged(searchCondition, pageable);

        // Then
        Assert.assertNotNull("결과 페이지는 null이 아니어야 합니다.", resultPage);
        Assert.assertEquals("총 게시물 수는 3개여야 합니다 (삭제된 게시물 제외).", 3, resultPage.getTotalElements());
        Assert.assertEquals("총 페이지 수는 2개여야 합니다.", 2, resultPage.getTotalPages());
        Assert.assertEquals("현재 페이지의 게시물 수는 2개여야 합니다.", 2, resultPage.getContent().size());

        // 첫 번째 게시물 (testRecipient1)
        RecipientResponseDto firstDto = resultPage.getContent().get(0);
        Assert.assertEquals("첫 번째 게시물은 testRecipient1이어야 합니다.", testRecipient1.getLetterSeq(), firstDto.getLetterSeq());
        Assert.assertEquals("testRecipient1의 댓글 수는 2개여야 합니다.", 2, firstDto.getCommentCount());

        // 두 번째 게시물 (testRecipient2)
        RecipientResponseDto secondDto = resultPage.getContent().get(1);
        Assert.assertEquals("두 번째 게시물은 testRecipient2이어야 합니다.", testRecipient2.getLetterSeq(), secondDto.getLetterSeq());
        Assert.assertEquals("testRecipient2의 댓글 수는 1개여야 합니다.", 1, secondDto.getCommentCount());
    }

    @Test
    public void testSelectRecipientListPaged_Success_SecondPage() throws Exception {
        // Given
        RecipientEntity searchCondition = new RecipientEntity(); // 검색 조건 없음
        Pageable pageable = PageRequest.of(1, 2, Sort.by("letterSeq").ascending()); // 두 번째 페이지, 2개씩

        // When
        Page<RecipientResponseDto> resultPage = recipientService.selectRecipientListPaged(searchCondition, pageable);

        // Then
        Assert.assertNotNull("결과 페이지는 null이 아니어야 합니다.", resultPage);
        Assert.assertEquals("총 게시물 수는 3개여야 합니다.", 3, resultPage.getTotalElements());
        Assert.assertEquals("현재 페이지의 게시물 수는 1개여야 합니다.", 1, resultPage.getContent().size());

        // 세 번째 게시물 (testRecipient3)
        RecipientResponseDto thirdDto = resultPage.getContent().get(0);
        Assert.assertEquals("첫 번째 게시물은 testRecipient3이어야 합니다.", testRecipient3.getLetterSeq(), thirdDto.getLetterSeq());
        Assert.assertEquals("testRecipient3의 댓글 수는 0개여야 합니다.", 0, thirdDto.getCommentCount());
    }

    @Test
    public void testSelectRecipientListPaged_Success_WithSearchCondition() throws Exception {
        // Given
        RecipientEntity searchCondition = new RecipientEntity();
        searchCondition.setLetterTitle("제목2"); // 제목으로 검색
        Pageable pageable = PageRequest.of(0, 10, Sort.by("letterSeq").ascending());

        // When
        Page<RecipientResponseDto> resultPage = recipientService.selectRecipientListPaged(searchCondition, pageable);

        // Then
        Assert.assertNotNull("결과 페이지는 null이 아니어야 합니다.", resultPage);
        Assert.assertEquals("검색 결과 게시물 수는 1개여야 합니다.", 1, resultPage.getTotalElements());
        Assert.assertEquals("첫 번째 게시물은 testRecipient2이어야 합니다.", testRecipient2.getLetterSeq(), resultPage.getContent().get(0).getLetterSeq());
        Assert.assertEquals("testRecipient2의 댓글 수는 1개여야 합니다.", 1, resultPage.getContent().get(0).getCommentCount());
    }

    @Test
    public void testSelectRecipientListPaged_Success_NoResults() throws Exception {
        // Given
        RecipientEntity searchCondition = new RecipientEntity();
        searchCondition.setLetterTitle("존재하지 않는 제목"); // 존재하지 않는 검색 조건
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RecipientResponseDto> resultPage = recipientService.selectRecipientListPaged(searchCondition, pageable);

        // Then
        Assert.assertNotNull("결과 페이지는 null이 아니어야 합니다.", resultPage);
        Assert.assertTrue("결과가 없어야 합니다.", resultPage.isEmpty());
        Assert.assertEquals("총 요소는 0개여야 합니다.", 0, resultPage.getTotalElements());
    }


    // --- selectRecipientList (목록 조회) 테스트 ---

    @Test
    public void testSelectRecipientList_Success_AllActivePosts() throws Exception {
        // Given
        RecipientEntity searchCondition = new RecipientEntity(); // 검색 조건 없음

        // When
        List<RecipientResponseDto> resultList = recipientService.selectRecipientList(searchCondition);

        // Then
        Assert.assertNotNull("결과 목록은 null이 아니어야 합니다.", resultList);
        Assert.assertEquals("총 게시물 수는 3개여야 합니다 (삭제된 게시물 제외).", 3, resultList.size());

        // 댓글 수 확인
        Assert.assertEquals("testRecipient1의 댓글 수는 2개여야 합니다.", 2,
                resultList.stream().filter(dto -> dto.getLetterSeq().equals(testRecipient1.getLetterSeq())).findFirst().get().getCommentCount());
        Assert.assertEquals("testRecipient2의 댓글 수는 1개여야 합니다.", 1,
                resultList.stream().filter(dto -> dto.getLetterSeq().equals(testRecipient2.getLetterSeq())).findFirst().get().getCommentCount());
        Assert.assertEquals("testRecipient3의 댓글 수는 0개여야 합니다.", 0,
                resultList.stream().filter(dto -> dto.getLetterSeq().equals(testRecipient3.getLetterSeq())).findFirst().get().getCommentCount());
    }

    @Test
    public void testSelectRecipientList_Success_WithKeywordSearch() throws Exception {
        // Given
        RecipientEntity searchCondition = new RecipientEntity();
        searchCondition.setLetterContents("내용2"); // 내용으로 검색

        // When
        List<RecipientResponseDto> resultList = recipientService.selectRecipientList(searchCondition);

        // Then
        Assert.assertNotNull("결과 목록은 null이 아니어야 합니다.", resultList);
        Assert.assertEquals("검색 결과 게시물 수는 1개여야 합니다.", 1, resultList.size());
        Assert.assertEquals("첫 번째 게시물은 testRecipient2이어야 합니다.", testRecipient2.getLetterSeq(), resultList.get(0).getLetterSeq());
    }

    // --- selectRecipientListTotCnt (총 개수 조회) 테스트 ---

    @Test
    public void testSelectRecipientListTotCnt_Success_NoCondition() throws Exception {
        // Given
        RecipientEntity searchCondition = new RecipientEntity(); // 조건 없음

        // When
        int totalCount = recipientService.selectRecipientListTotCnt(searchCondition);

        // Then
        Assert.assertEquals("총 활성 게시물 수는 3개여야 합니다.", 3, totalCount);
    }

    @Test
    public void testSelectRecipientListTotCnt_Success_WithCondition() throws Exception {
        // Given
        RecipientEntity searchCondition = new RecipientEntity();
        searchCondition.setLetterTitle("제목1");

        // When
        int totalCount = recipientService.selectRecipientListTotCnt(searchCondition);

        // Then
        Assert.assertEquals("검색 조건에 맞는 게시물은 1개여야 합니다.", 1, totalCount);
    }

    @Test
    public void testSelectRecipientListTotCnt_Success_NoMatchingCondition() throws Exception {
        // Given
        RecipientEntity searchCondition = new RecipientEntity();
        searchCondition.setLetterTitle("없는 제목"); // 존재하지 않는 조건

        // When
        int totalCount = recipientService.selectRecipientListTotCnt(searchCondition);

        // Then
        Assert.assertEquals("조건에 맞는 게시물이 없어야 합니다.", 0, totalCount);
    }


    // --- getRecipientSpecification Helper Method (내부 동작 확인용) ---
    // 이 부분은 서비스 로직의 getRecipientSpecification 메서드를 테스트합니다.
    // 서비스 Impl에 @Autowired로 주입받아 테스트하는 것이 일반적이나,
    // 여기서는 Service 인터페이스만 주입받았으므로, Impl의 해당 메서드를 직접 호출할 수 없습니다.
    // 만약 private 또는 protected 메서드라면, 리플렉션 등을 사용해야 하지만 권장되지 않습니다.
    // 대신, 이 메서드의 동작은 selectRecipientListPaged/selectRecipientList/selectRecipientListTotCnt 테스트를 통해 간접적으로 검증됩니다.
    // 혹은 RecipientServiceImpl을 주입받아 테스트를 작성할 수도 있습니다.
    /*
    @Test
    public void testGetRecipientSpecification_NoCondition() {
        RecipientEntity searchCondition = new RecipientEntity();
        Specification<RecipientEntity> spec = ((RecipientServiceImpl) recipientService).getRecipientSpecification(searchCondition);
        // spec의 동작을 직접 테스트하기는 어려움. findAll/count 메서드를 통해 간접 검증.
    }
    */
}