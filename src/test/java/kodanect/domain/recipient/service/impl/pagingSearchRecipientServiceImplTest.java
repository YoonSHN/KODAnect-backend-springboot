package kodanect.domain.recipient.service.impl;

import kodanect.common.util.HcaptchaService;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.dto.RecipientDetailResponseDto;
import kodanect.domain.recipient.dto.RecipientListResponseDto;
import kodanect.domain.recipient.dto.RecipientSearchCondition;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
import kodanect.domain.recipient.repository.RecipientRepository;
import kodanect.domain.recipient.dto.SearchType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class pagingSearchRecipientServiceImplTest {

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private RecipientCommentRepository recipientCommentRepository;

    @Mock
    private HcaptchaService hcaptchaService;

    @InjectMocks
    private RecipientServiceImpl recipientService; // @InjectMocks를 사용하여 의존성 주입

    private static final String RECIPIENT_NOT_FOUND_MESSAGE = "해당 게시물이 존재하지 않거나 이미 삭제되었습니다.";

    // 파일 업로드 관련 경로 (실제 파일 시스템에 저장하지 않으므로 임시 경로 사용)
    private String testUploadDir = "test_uploads";
    private String testFileBaseUrl = "/uploads";


    @Before
    public void setUp() throws IOException {

        ReflectionTestUtils.setField(recipientService, "uploadDir", testUploadDir);
        ReflectionTestUtils.setField(recipientService, "fileBaseUrl", testFileBaseUrl);

        // 테스트를 위해 임시 디렉토리 생성 (실제 파일 시스템에 영향 주지 않음)
        // 기존 deleteRecipient_Failure_NotFound 테스트에서 생성되는 부분이 있으므로, 중복 방지를 위해 확인 후 생성
        Path path = Paths.get(testUploadDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    // RecipientEntity 생성 헬퍼 메서드
    private RecipientEntity createRecipientEntity(Integer seq, String title, String contents, String writer, String passcode, String delFlag, int readCount) {
        RecipientEntity entity = RecipientEntity.builder()
                .letterSeq(seq)
                .letterTitle(title)
                .letterContents(contents)
                .letterWriter(writer)
                .letterPasscode(passcode)
                .delFlag(delFlag)
                .readCount(readCount)
                .recipientYear("2024")
                .organCode("ORG001")
                .writeTime(LocalDateTime.now())
                .build();
        return entity;
    }

    // RecipientCommentEntity 생성 헬퍼 메서드
    private RecipientCommentEntity createCommentEntity(Integer seq, RecipientEntity recipient, String contents, String writer, String delFlag, LocalDateTime writeTime) {
        RecipientCommentEntity comment = RecipientCommentEntity.builder()
                .commentSeq(seq)
                .letterSeq(recipient) // RecipientEntity 객체로 설정
                .commentContents(contents)
                .commentWriter(writer)
                .delFlag(delFlag)
                .writeTime(writeTime)
                .build();
        return comment;
    }

    /**
     * ---
     * <p>
     * ## `selectRecipient` 테스트
     * <p>
     * ### 게시물 조회 성공 - 댓글 있음
     * <p>
     * ```java
     */
    @Test
    public void selectRecipient_Success_WithComments() {
        // Given
        int letterSeq = 1;
        RecipientEntity recipient = createRecipientEntity(letterSeq, "테스트 제목", "테스트 내용", "테스트 작가", "1234", "N", 5);

        // 댓글 총 개수 Mocking
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(recipient));
        when(recipientRepository.countCommentsByLetterSeq(letterSeq)).thenReturn(5); // 5개의 댓글이 있다고 가정

        // When
        RecipientDetailResponseDto result = recipientService.selectRecipient(letterSeq);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLetterSeq()).isEqualTo(letterSeq);
        assertThat(result.getLetterTitle()).isEqualTo("테스트 제목");
        assertThat(result.getReadCount()).isEqualTo(6); // 조회수 1 증가 확인

        assertThat(result.getCommentCount()).isEqualTo(5);
        // RecipientServiceImpl에서 INITIAL_COMMENT_LOAD_LIMIT를 3으로 가정하면, 5개는 3보다 많으므로 true
        assertThat(result.isHasMoreComments()).isTrue();

        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientRepository, times(1)).save(recipient); // 조회수 업데이트를 위해 save 호출 확인
        verify(recipientRepository, times(1)).countCommentsByLetterSeq(letterSeq);
    }

    /**
     * ```
     * <p>
     * ### 게시물 조회 성공 - 댓글 없음
     * <p>
     * ```java
     */
    @Test
    public void selectRecipient_Success_NoComments() {
        // Given
        int letterSeq = 2;
        RecipientEntity recipient = createRecipientEntity(letterSeq, "댓글 없는 게시물", "내용", "작가", "1234", "N", 10);

        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(recipient));
        when(recipientRepository.countCommentsByLetterSeq(letterSeq)).thenReturn(0); // 댓글 없음

        // When
        RecipientDetailResponseDto result = recipientService.selectRecipient(letterSeq);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLetterSeq()).isEqualTo(letterSeq);
        assertThat(result.getReadCount()).isEqualTo(11); // 조회수 1 증가 확인

        assertThat(result.getCommentCount()).isEqualTo(0);
        assertThat(result.isHasMoreComments()).isFalse();

        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientRepository, times(1)).save(recipient);
        verify(recipientRepository, times(1)).countCommentsByLetterSeq(letterSeq);
    }

    /**
     * ```
     * <p>
     * ### 게시물 조회 실패 - 게시물 없음
     * <p>
     * ```java
     */
    @Test
    public void selectRecipient_Failure_NotFound() {
        // Given
        int letterSeq = 999;
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> recipientService.selectRecipient(letterSeq))
                .isInstanceOf(RecipientNotFoundException.class)
                // RecipientServiceImpl의 RECIPIENT_NOT_FOUND_MESSAGE 상수에 직접 의존합니다.
                // 이 상수가 @Value로 주입되도록 변경했다면, 테스트 컨텍스트에서 해당 값을 정의하거나,
                // 스파이를 사용하여 직접 Stubbing해야 합니다.
                // 현재 테스트는 `RECIPIENT_NOT_FOUND_MESSAGE` 상수가 RecipientServiceImpl 내부에서 그대로 사용된다고 가정합니다.
                .hasMessage(RECIPIENT_NOT_FOUND_MESSAGE);

        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientRepository, never()).save(any(RecipientEntity.class)); // 조회수 증가 안됨
        verify(recipientRepository, never()).countCommentsByLetterSeq(anyInt()); // 댓글 수 조회 안됨
    }

    /**
     * ```
     * <p>
     * ### 게시물 조회 실패 - 게시물이 삭제됨
     * <p>
     * ```java
     */
    @Test
    public void selectRecipient_Failure_Deleted() {
        // Given
        int letterSeq = 3;
        RecipientEntity deletedRecipient = createRecipientEntity(letterSeq, "삭제된 게시물", "내용", "작가", "1234", "Y", 5);

        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(deletedRecipient));

        // When & Then
        assertThatThrownBy(() -> recipientService.selectRecipient(letterSeq))
                .isInstanceOf(RecipientNotFoundException.class)
                .hasMessage(RECIPIENT_NOT_FOUND_MESSAGE);

        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientRepository, never()).save(any(RecipientEntity.class));
        verify(recipientRepository, never()).countCommentsByLetterSeq(anyInt());
    }

    /**
     * ```
     * <p>
     * ---
     * <p>
     * ## `selectPaginatedCommentsForRecipient` 테스트
     * <p>
     * ### 페이징된 댓글 조회 성공 - 첫 페이지 (lastCommentId가 null)
     * <p>
     * ```java
     */
    @Test
    public void selectPaginatedCommentsForRecipient_Success_FirstPage_NullLastCommentId() {
        // Given
        int letterSeq = 1;
        int size = 2;
        RecipientEntity recipient = createRecipientEntity(letterSeq, "제목", "내용", "작가", "1234", "N", 0);

        LocalDateTime now = LocalDateTime.now();
        List<RecipientCommentEntity> comments = Arrays.asList(
                createCommentEntity(1, recipient, "댓글1", "익명1", "N", now.minusMinutes(5)),
                createCommentEntity(2, recipient, "댓글2", "익명2", "N", now.minusMinutes(3)),
                createCommentEntity(3, recipient, "댓글3", "익명3", "N", now.minusMinutes(1))
        );

        // findById는 삭제되지 않은 게시물을 확인하기 위해 한 번 호출
        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(recipient));

        // findByLetterSeqAndDelFlag는 첫 페이지 댓글 조회를 위해 호출
        Page<RecipientCommentEntity> commentPage = new PageImpl<>(comments.subList(0, size)); // 처음 2개만 반환
        when(recipientCommentRepository.findByLetterSeqAndDelFlag(eq(recipient), eq("N"), any(Pageable.class)))
                .thenReturn(commentPage);

        // When
        List<RecipientCommentResponseDto> result = recipientService.selectPaginatedCommentsForRecipient(letterSeq, null, size);

        // Then
        assertThat(result).hasSize(size);
        assertThat(result.get(0).getCommentSeq()).isEqualTo(1);
        assertThat(result.get(1).getCommentSeq()).isEqualTo(2);

        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientCommentRepository, times(1))
                .findByLetterSeqAndDelFlag(eq(recipient), eq("N"), any(Pageable.class));
        verify(recipientCommentRepository, never()).findAll(any(Specification.class), any(Pageable.class)); // Specification은 호출되지 않음
    }

    /**
     * ```
     * <p>
     * ### 페이징된 댓글 조회 성공 - 다음 페이지 (lastCommentId 존재)
     * <p>
     * ```java
     */
    @Test
    public void selectPaginatedCommentsForRecipient_Success_NextPage_WithLastCommentId() {
        // Given
        int letterSeq = 1;
        int lastCommentId = 2;
        int size = 2;
        RecipientEntity recipient = createRecipientEntity(letterSeq, "제목", "내용", "작가", "1234", "N", 0);

        LocalDateTime now = LocalDateTime.now();
        List<RecipientCommentEntity> comments = Arrays.asList(
                createCommentEntity(3, recipient, "댓글3", "익명3", "N", now.minusMinutes(1)),
                createCommentEntity(4, recipient, "댓글4", "익명4", "N", now.now())
        );

        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(recipient));

        // findAll (Specification)이 호출될 때 Mocking
        Page<RecipientCommentEntity> commentPage = new PageImpl<>(comments);
        when(recipientCommentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(commentPage);

        // When
        List<RecipientCommentResponseDto> result = recipientService.selectPaginatedCommentsForRecipient(letterSeq, lastCommentId, size);

        // Then
        assertThat(result).hasSize(size);
        assertThat(result.get(0).getCommentSeq()).isEqualTo(3);
        assertThat(result.get(1).getCommentSeq()).isEqualTo(4);

        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientCommentRepository, never())
                .findByLetterSeqAndDelFlag(any(RecipientEntity.class), anyString(), any(Pageable.class)); // 첫 페이지 조회는 호출되지 않음
        verify(recipientCommentRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * ```
     * <p>
     * ### 페이징된 댓글 조회 실패 - 게시물 없음
     * <p>
     * ```java
     */
    @Test
    public void selectPaginatedCommentsForRecipient_Failure_RecipientNotFound() {
        // Given
        int letterSeq = 999;
        int lastCommentId = 0;
        int size = 5;

        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> recipientService.selectPaginatedCommentsForRecipient(letterSeq, lastCommentId, size))
                .isInstanceOf(RecipientNotFoundException.class)
                .hasMessage(RECIPIENT_NOT_FOUND_MESSAGE); // 서비스 상수에 직접 의존

        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientCommentRepository, never()).findByLetterSeqAndDelFlag(any(), anyString(), any());
        verify(recipientCommentRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * ```
     * <p>
     * ### 페이징된 댓글 조회 실패 - 게시물 삭제됨
     * <p>
     * ```java
     */
    @Test
    public void selectPaginatedCommentsForRecipient_Failure_RecipientDeleted() {
        // Given
        int letterSeq = 4;
        int lastCommentId = 0;
        int size = 5;
        RecipientEntity deletedRecipient = createRecipientEntity(letterSeq, "삭제된 게시물", "내용", "작가", "1234", "Y", 0);

        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(deletedRecipient));

        // When & Then
        assertThatThrownBy(() -> recipientService.selectPaginatedCommentsForRecipient(letterSeq, lastCommentId, size))
                .isInstanceOf(RecipientNotFoundException.class)
                .hasMessage(RECIPIENT_NOT_FOUND_MESSAGE); // 서비스 상수에 직접 의존

        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientCommentRepository, never()).findByLetterSeqAndDelFlag(any(), anyString(), any());
        verify(recipientCommentRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    /**
     * ```
     * <p>
     * ### 페이징된 댓글 조회 성공 - 댓글이 없을 때
     * <p>
     * ```java
     */
    @Test
    public void selectPaginatedCommentsForRecipient_Success_NoCommentsFound() {
        // Given
        int letterSeq = 5;
        int lastCommentId = 0;
        int size = 5;
        RecipientEntity recipient = createRecipientEntity(letterSeq, "댓글 없는 게시물", "내용", "작가", "1234", "N", 0);

        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(recipient));

        // 댓글을 찾지 못하여 빈 리스트 반환
        Page<RecipientCommentEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(recipientCommentRepository.findByLetterSeqAndDelFlag(eq(recipient), eq("N"), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        List<RecipientCommentResponseDto> result = recipientService.selectPaginatedCommentsForRecipient(letterSeq, lastCommentId, size);

        // Then
        assertThat(result).isEmpty();
        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientCommentRepository, times(1))
                .findByLetterSeqAndDelFlag(eq(recipient), eq("N"), any(Pageable.class));
    }

    /**
     * ```
     * <p>
     * ---
     * <p>
     * ## `selectRecipientList` 테스트
     * <p>
     * ### 게시물 목록 조회 성공 - 검색 조건 없음 (전체 최신순)
     * <p>
     * ```java
     */
    @Test
    public void selectRecipientList_Success_NoSearchCondition_FirstPage() {
        // Given
        RecipientSearchCondition searchCondition = new RecipientSearchCondition(); // 검색 조건 없음
        Integer lastId = null;
        int size = 2;

        RecipientEntity recipient1 = createRecipientEntity(3, "제목3", "내용3", "작가3", "1234", "N", 0);
        RecipientEntity recipient2 = createRecipientEntity(2, "제목2", "내용2", "작가2", "1234", "N", 0);
        RecipientEntity recipient3 = createRecipientEntity(1, "제목1", "내용1", "작가1", "1234", "N", 0);

        List<RecipientEntity> foundRecipients = Arrays.asList(recipient1, recipient2); // 최신 2개
        Page<RecipientEntity> recipientPage = new PageImpl<>(foundRecipients);

        // findAll(Specification, Pageable)을 Mocking
        when(recipientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(recipientPage);

        // 댓글 수 Mocking (실제 게시물 목록에 대한 댓글 수를 반환)
        List<Object[]> commentCountsRaw = new ArrayList<>();
        commentCountsRaw.add(new Object[]{3, BigInteger.valueOf(5)});
        commentCountsRaw.add(new Object[]{2, BigInteger.valueOf(2)});
        when(recipientRepository.countCommentsByLetterSeqs(anyList())).thenReturn(commentCountsRaw);


        // When
        List<RecipientListResponseDto> result = recipientService.selectRecipientList(searchCondition, lastId, size);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLetterSeq()).isEqualTo(3);
        assertThat(result.get(0).getLetterTitle()).isEqualTo("제목3");
        assertThat(result.get(0).getCommentCount()).isEqualTo(5);

        assertThat(result.get(1).getLetterSeq()).isEqualTo(2);
        assertThat(result.get(1).getLetterTitle()).isEqualTo("제목2");
        assertThat(result.get(1).getCommentCount()).isEqualTo(2);

        verify(recipientRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(recipientRepository, times(1)).countCommentsByLetterSeqs(anyList());
    }

    /**
     * ```
     * <p>
     * ### 게시물 목록 조회 성공 - "더보기" (lastId 존재)
     * <p>
     * ```java
     */
    @Test
    public void selectRecipientList_Success_LoadMore_WithLastId() {
        // Given
        RecipientSearchCondition searchCondition = new RecipientSearchCondition();
        Integer lastId = 2; // letterSeq 2보다 작은 게시물 조회
        int size = 2;

        RecipientEntity recipient1 = createRecipientEntity(1, "제목1", "내용1", "작가1", "1234", "N", 0);

        List<RecipientEntity> foundRecipients = Collections.singletonList(recipient1); // ID 1만 조회됨
        Page<RecipientEntity> recipientPage = new PageImpl<>(foundRecipients);

        when(recipientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(recipientPage);

        List<Object[]> commentCountsRaw = new ArrayList<>();
        commentCountsRaw.add(new Object[]{1, BigInteger.valueOf(10)});
        when(recipientRepository.countCommentsByLetterSeqs(anyList())).thenReturn(commentCountsRaw);

        // When
        List<RecipientListResponseDto> result = recipientService.selectRecipientList(searchCondition, lastId, size);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLetterSeq()).isEqualTo(1);
        assertThat(result.get(0).getCommentCount()).isEqualTo(10);

        verify(recipientRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(recipientRepository, times(1)).countCommentsByLetterSeqs(anyList());
    }

    /**
     * ```
     * <p>
     * ### 게시물 목록 조회 성공 - 제목 검색
     * <p>
     * ```java
     */
    @Test
    public void selectRecipientList_Success_SearchByTitle() {
        // Given
        RecipientSearchCondition searchCondition = new RecipientSearchCondition();
        searchCondition.setSearchType(SearchType.TITLE);
        searchCondition.setSearchKeyword("테스트");
        Integer lastId = null;
        int size = 5;

        RecipientEntity recipient1 = createRecipientEntity(5, "새로운 테스트 제목", "내용", "작가", "1234", "N", 0);
        RecipientEntity recipient2 = createRecipientEntity(4, "테스트입니다", "다른 내용", "작가", "1234", "N", 0);

        List<RecipientEntity> foundRecipients = Arrays.asList(recipient1, recipient2);
        Page<RecipientEntity> recipientPage = new PageImpl<>(foundRecipients);

        when(recipientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(recipientPage);

        List<Object[]> commentCountsRaw = new ArrayList<>();
        commentCountsRaw.add(new Object[]{5, BigInteger.valueOf(1)});
        commentCountsRaw.add(new Object[]{4, BigInteger.valueOf(0)});
        when(recipientRepository.countCommentsByLetterSeqs(anyList())).thenReturn(commentCountsRaw);

        // When
        List<RecipientListResponseDto> result = recipientService.selectRecipientList(searchCondition, lastId, size);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLetterTitle()).contains("테스트");
        assertThat(result.get(1).getLetterTitle()).contains("테스트");

        verify(recipientRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(recipientRepository, times(1)).countCommentsByLetterSeqs(anyList());
    }

    /**
     * ```
     * <p>
     * ### 게시물 목록 조회 성공 - 내용 검색
     * <p>
     * ```java
     */
    @Test
    public void selectRecipientList_Success_SearchByContents() {
        // Given
        RecipientSearchCondition searchCondition = new RecipientSearchCondition();
        searchCondition.setSearchType(SearchType.CONTENTS);
        searchCondition.setSearchKeyword("내용");
        Integer lastId = null;
        int size = 5;

        // createRecipientEntity는 전체 필드를 포함하는 엔티티를 생성하지만,
        // DTO로 변환될 때 letterContents는 포함되지 않습니다.
        RecipientEntity recipient1 = createRecipientEntity(6, "제목", "새로운 내용입니다.", "작가", "1234", "N", 0);
        RecipientEntity recipient2 = createRecipientEntity(7, "제목2", "내용 테스트", "작가2", "1234", "N", 0);

        List<RecipientEntity> foundRecipients = Arrays.asList(recipient1, recipient2);
        Page<RecipientEntity> recipientPage = new PageImpl<>(foundRecipients);

        when(recipientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(recipientPage);

        List<Object[]> commentCountsRaw = new ArrayList<>();
        commentCountsRaw.add(new Object[]{6, BigInteger.valueOf(3)});
        commentCountsRaw.add(new Object[]{7, BigInteger.valueOf(0)});
        when(recipientRepository.countCommentsByLetterSeqs(anyList())).thenReturn(commentCountsRaw);

        // When
        List<RecipientListResponseDto> result = recipientService.selectRecipientList(searchCondition, lastId, size);

        // Then
        assertThat(result).hasSize(2);

        // 대신, 제목이 올바르게 매핑되었는지 등 검증할 수 있습니다.
        assertThat(result.get(0).getLetterTitle()).isEqualTo("제목");
        assertThat(result.get(1).getLetterTitle()).isEqualTo("제목2");


        verify(recipientRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(recipientRepository, times(1)).countCommentsByLetterSeqs(anyList());
    }

    /**
     * ```
     * <p>
     * ### 게시물 목록 조회 성공 - 제목+내용 통합 검색 (SearchType.ALL)
     * <p>
     * ```java
     */
    @Test
    public void selectRecipientList_Success_SearchByAll() {
        // Given
        RecipientSearchCondition searchCondition = new RecipientSearchCondition();
        searchCondition.setSearchType(SearchType.ALL); // 또는 null
        searchCondition.setSearchKeyword("키워드");
        Integer lastId = null;
        int size = 5;

        RecipientEntity recipient1 = createRecipientEntity(8, "제목1 키워드", "내용1", "작가1", "1234", "N", 0);
        RecipientEntity recipient2 = createRecipientEntity(9, "제목2", "내용2 키워드", "작가2", "1234", "N", 0);

        List<RecipientEntity> foundRecipients = Arrays.asList(recipient1, recipient2);
        Page<RecipientEntity> recipientPage = new PageImpl<>(foundRecipients);

        when(recipientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(recipientPage);

        List<Object[]> commentCountsRaw = new ArrayList<>();
        commentCountsRaw.add(new Object[]{8, BigInteger.valueOf(1)});
        commentCountsRaw.add(new Object[]{9, BigInteger.valueOf(2)});
        when(recipientRepository.countCommentsByLetterSeqs(anyList())).thenReturn(commentCountsRaw);

        // When
        List<RecipientListResponseDto> result = recipientService.selectRecipientList(searchCondition, lastId, size);

        // Then
        assertThat(result).hasSize(2);
        // 제목 또는 내용에 키워드가 포함되어 있는지 확인 (여기서는 Mocking된 데이터가 그러함)
        // 실제 Specification의 동작은 JPA에 위임하므로, Mocking 시에는 반환되는 데이터의 정확성을 확인합니다.
        assertThat(result.get(0).getLetterSeq()).isEqualTo(8);
        assertThat(result.get(1).getLetterSeq()).isEqualTo(9);

        verify(recipientRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(recipientRepository, times(1)).countCommentsByLetterSeqs(anyList());
    }

    /**
     * ```
     * <p>
     * ### 게시물 목록 조회 성공 - 검색 결과 없음
     * <p>
     * ```java
     */
    @Test
    public void selectRecipientList_Success_NoResults() {
        // Given
        RecipientSearchCondition searchCondition = new RecipientSearchCondition();
        searchCondition.setSearchKeyword("없는 키워드");
        Integer lastId = null;
        int size = 5;

        Page<RecipientEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(recipientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        // When
        List<RecipientListResponseDto> result = recipientService.selectRecipientList(searchCondition, lastId, size);

        // Then
        assertThat(result).isEmpty();

        verify(recipientRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(recipientRepository, never()).countCommentsByLetterSeqs(anyList()); // 게시물 없으므로 댓글 수 조회X
    }

    /**
     * ```
     * <p>
     * ---
     * <p>
     * ## `selectRecipientListTotCnt` 테스트
     * <p>
     * ### 게시물 총 개수 조회 성공 - 검색 조건 없음
     * <p>
     * ```java
     */
    @Test
    public void selectRecipientListTotCnt_Success_NoSearchCondition() {
        // Given
        RecipientSearchCondition searchCondition = new RecipientSearchCondition();
        long expectedCount = 10L;

        when(recipientRepository.count(any(Specification.class))).thenReturn(expectedCount);

        // When
        int result = recipientService.selectRecipientListTotCnt(searchCondition);

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(recipientRepository, times(1)).count(any(Specification.class));
    }

    /**
     * ```
     * <p>
     * ### 게시물 총 개수 조회 성공 - 검색 조건 있음
     * <p>
     * ```java
     */
    @Test
    public void selectRecipientListTotCnt_Success_WithSearchCondition() {
        // Given
        RecipientSearchCondition searchCondition = new RecipientSearchCondition();
        searchCondition.setSearchType(SearchType.TITLE);
        searchCondition.setSearchKeyword("특정 제목");
        long expectedCount = 3L;

        when(recipientRepository.count(any(Specification.class))).thenReturn(expectedCount);

        // When
        int result = recipientService.selectRecipientListTotCnt(searchCondition);

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(recipientRepository, times(1)).count(any(Specification.class));
    }

    /**
     * ```
     * <p>
     * ### 게시물 총 개수 조회 성공 - 검색 결과 0개
     * <p>
     * ```java
     */
    @Test
    public void selectRecipientListTotCnt_Success_ZeroResults() {
        // Given
        RecipientSearchCondition searchCondition = new RecipientSearchCondition();
        searchCondition.setSearchType(SearchType.CONTENTS);
        searchCondition.setSearchKeyword("없는 내용");
        long expectedCount = 0L;

        when(recipientRepository.count(any(Specification.class))).thenReturn(expectedCount);

        // When
        int result = recipientService.selectRecipientListTotCnt(searchCondition);

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(recipientRepository, times(1)).count(any(Specification.class));
    }

    /**
     * ```
     * <p>
     * ---
     */
}