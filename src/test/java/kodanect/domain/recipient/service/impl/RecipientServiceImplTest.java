package kodanect.domain.recipient.service.impl;

import kodanect.common.config.GlobalsProperties;
import kodanect.domain.recipient.dto.RecipientDetailResponseDto;
import kodanect.domain.recipient.dto.RecipientRequestDto;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
import kodanect.domain.recipient.repository.RecipientRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.FileSystemUtils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class RecipientServiceImplTest {

    @Mock
    private RecipientRepository recipientRepository;

    @Mock
    private RecipientCommentRepository recipientCommentRepository;

    @Mock
    private GlobalsProperties globalsProperties;

    private RecipientServiceImpl recipientService;

    private Path tempUploadDir;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String ORGAN_CODE_DIRECT_INPUT = "ORGAN000";
    private static final String ANONYMOUS_WRITER_VALUE = "익명";

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        // 테스트를 위한 임시 디렉토리 생성
        tempUploadDir = Files.createTempDirectory("test-uploads");

        // recipientService 인스턴스 수동 생성
        recipientService = new RecipientServiceImpl(
                recipientRepository,
                recipientCommentRepository,
                globalsProperties,
                ORGAN_CODE_DIRECT_INPUT,
                ANONYMOUS_WRITER_VALUE
        );
    }

    @After
    public void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(tempUploadDir);
    }

    // 게시물 등록 - 이미지 파일 없음
    @Test
    public void testInsertRecipient_withoutImageFile_shouldSaveEntity() throws IOException {
        // Given
        RecipientRequestDto recipientDto = new RecipientRequestDto();
        recipientDto.setOrganCode("ORGAN001");
        recipientDto.setLetterTitle("제목 없음");
        recipientDto.setRecipientYear("2023");
        recipientDto.setLetterWriter("파일 없음");
        recipientDto.setAnonymityFlag("Y");
        recipientDto.setLetterContents("이미지 파일이 없는 게시물");
        recipientDto.setLetterPasscode("testPass1234");

        // 이미지 파일 관련 필드를 null로 설정 (MultipartFile 대신 String 필드)
        recipientDto.setFileName(null);
        recipientDto.setOrgFileName(null);

        // 저장될 Entity Mocking
        RecipientEntity savedEntity = RecipientEntity.builder()
                .letterSeq(1)
                .organCode(recipientDto.getOrganCode())
                .letterTitle(recipientDto.getLetterTitle())
                .recipientYear(recipientDto.getRecipientYear())
                .letterWriter(ANONYMOUS_WRITER_VALUE)
                .anonymityFlag(recipientDto.getAnonymityFlag())
                .letterContents(recipientDto.getLetterContents())
                .fileName(null)
                .orgFileName(null)
                .writeTime(LocalDateTime.now())
                .build();

        when(recipientRepository.save(any(RecipientEntity.class))).thenReturn(savedEntity);

        // When
        RecipientDetailResponseDto responseDto = recipientService.insertRecipient(recipientDto);

        // Then
        verify(recipientRepository, times(1)).save(any(RecipientEntity.class));

        assertThat(responseDto.getLetterSeq()).isEqualTo(1);
        assertThat(responseDto.getImageUrl()).isNull();
        assertThat(responseDto.getFileName()).isNull();
        assertThat(responseDto.getOrgFileName()).isNull();
        assertThat(responseDto.getLetterWriter()).isEqualTo(ANONYMOUS_WRITER_VALUE);
        assertThat(responseDto.getLetterContents()).isEqualTo(recipientDto.getLetterContents());
    }

    @Test(expected = RecipientNotFoundException.class)
    public void selectRecipient_삭제된게시물_예외발생() {
        Integer letterSeq = 2;
        RecipientEntity deletedEntity = RecipientEntity.builder()
                .letterSeq(letterSeq)
                .delFlag("Y")
                .build();

        Mockito.lenient().when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(deletedEntity));

        recipientService.selectRecipient(letterSeq);
    }

    // 게시물 수정 - ORGAN000 선택 시 organEtc 필수 입력 예외 테스트
    @Test
    public void testUpdateRecipient_organ000WithoutOrganEtc_shouldThrowException() {
        // Given
        Integer letterSeq = 1;
        String correctPasscode = "correctPass1234";

        // 기존 게시물 Entity Mocking
        RecipientEntity existingEntity = RecipientEntity.builder()
                .letterSeq(letterSeq)
                .organCode("ORGAN000")
                .letterTitle("기존 제목")
                .recipientYear("2020")
                .letterWriter("기존 작성자")
                .anonymityFlag("N")
                .letterContents("기존 내용")
                .letterPasscode(correctPasscode)
                .writeTime(LocalDateTime.now().minusDays(1))
                .delFlag("N")
                .build();

        // 수정 요청 DTO (ORGAN000 선택 시 organEtc가 null 또는 비어있음)
        RecipientRequestDto requestDto = new RecipientRequestDto();
        requestDto.setOrganCode("ORGAN000"); // ORGAN000 선택
        requestDto.setLetterTitle("새로운 제목");
        requestDto.setRecipientYear("2023");
        requestDto.setLetterWriter("새로운 작성자");
        requestDto.setAnonymityFlag("N");
        requestDto.setLetterContents("새로운 내용");
        requestDto.setLetterPasscode(correctPasscode);
        requestDto.setOrganEtc(null); // organEtc를 null로 설정하여 에러 유발

        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(existingEntity));

        // When & Then
        RecipientInvalidDataException exception = assertThrows(RecipientInvalidDataException.class, () ->
                recipientService.updateRecipient(letterSeq, requestDto)
        );

        assertThat(exception.getMessage()).isEqualTo("[잘못된 데이터] fieldName=ORGAN000 선택 시 organEtc는 필수 입력 항목입니다.");
        verify(recipientRepository, never()).save(any(RecipientEntity.class)); // save 호출 안됨 검증
    }

    // 삭제 테스트 (기존 로직 유지)
    @Test
    public void testDeleteRecipient_withCorrectPasscode_shouldSoftDeleteEntityAndComments() {
        Integer letterSeq = 1;
        String passcode = "12345678";
        RecipientEntity entity = RecipientEntity.builder().letterSeq(letterSeq).letterPasscode(passcode).delFlag("N").build();
        List<RecipientCommentEntity> comments = Arrays.asList(
                RecipientCommentEntity.builder().commentSeq(1).delFlag("N").build()
        );

        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(entity));
        when(recipientCommentRepository.findCommentsByLetterSeqAndDelFlagSorted(any(RecipientEntity.class), eq("N"))).thenReturn(comments);
        when(recipientRepository.save(any(RecipientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recipientCommentRepository.save(any(RecipientCommentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        recipientService.deleteRecipient(letterSeq, passcode);

        assertThat(entity.getDelFlag()).isEqualTo("Y");
        assertThat(comments.get(0).getDelFlag()).isEqualTo("Y");
        verify(recipientRepository, times(1)).save(entity);
        verify(recipientCommentRepository, times(1)).save(comments.get(0));
    }
}