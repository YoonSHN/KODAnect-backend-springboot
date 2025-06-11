package kodanect.domain.recipient.service.impl;

import kodanect.common.config.GlobalsProperties;
import kodanect.domain.recipient.dto.RecipientDetailResponseDto;
import kodanect.domain.recipient.dto.RecipientRequestDto;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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

        // GlobalsProperties Mock 설정
        when(globalsProperties.getFileStorePath()).thenReturn(tempUploadDir.toString());

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

    // 게시물 등록 - 이미지 파일 없음 (기존 로직 유지)
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
        recipientDto.setImageFile(null);

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
                .build();

        when(recipientRepository.save(any(RecipientEntity.class))).thenReturn(savedEntity);

        // When
        RecipientDetailResponseDto responseDto = recipientService.insertRecipient(recipientDto);

        // Then
        verify(recipientRepository, times(1)).save(any(RecipientEntity.class));

        assertThat(responseDto.getLetterSeq()).isEqualTo(1L);
        assertThat(responseDto.getImageUrl()).isNull();
        assertThat(responseDto.getOrgFileName()).isNull();
        assertThat(responseDto.getLetterWriter()).isEqualTo(ANONYMOUS_WRITER_VALUE);
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

    // 게시물 수정 - 이미지 파일 변경 포함 (GlobalsProperties 의존성 수정)
    @Test
    public void testUpdateRecipient_withImageFileChange_shouldUpdateFieldsAndImage() throws IOException {
        // Given
        Integer letterSeq = 1;
        String passcode = "12345678";
        RecipientEntity existingEntity = RecipientEntity.builder()
                .letterSeq(letterSeq)
                .letterPasscode(passcode)
                .delFlag("N")
                .fileName("old_uuid.jpg")
                .orgFileName("old_original.jpg")
                .build();

        RecipientRequestDto requestDto = RecipientRequestDto.builder()
                .letterWriter("새 작성자")
                .anonymityFlag("N")
                .letterContents("이미지 변경 포함 게시물 내용")
                .build();
        MockMultipartFile newMockFile = new MockMultipartFile(
                "imageFile", "new_image.png", "image/png", "new content".getBytes(StandardCharsets.UTF_8)
        );
        requestDto.setImageFile(newMockFile);

        String newOriginalFileName = newMockFile.getOriginalFilename();

        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(existingEntity));
        when(recipientRepository.save(any(RecipientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RecipientDetailResponseDto result = recipientService.updateRecipient(letterSeq, requestDto);

        // Then
        assertThat(result.getImageUrl()).isNotNull();
        assertThat(result.getImageUrl()).endsWith(".png");
        assertThat(result.getOrgFileName()).isEqualTo(newOriginalFileName);

        Path savedFilePath = tempUploadDir.resolve(result.getImageUrl().substring(result.getImageUrl().lastIndexOf("/") + 1));
        assertThat(Files.exists(savedFilePath)).isTrue();
        assertThat(Files.readAllBytes(savedFilePath)).isEqualTo(newMockFile.getBytes());

        verify(recipientRepository, times(1)).save(any(RecipientEntity.class));
    }

    // 게시물 수정 - 이미지 파일 변경 없음 (기존 로직 유지)
    @Test
    public void testUpdateRecipient_withoutImageFileChange_shouldUpdateFields() throws IOException {
        // Given
        Integer letterSeq = 1;
        String passcode = "12345678";
        RecipientEntity existingEntity = RecipientEntity.builder()
                .letterSeq(letterSeq)
                .delFlag("N")
                .fileName("old_uuid.jpg")
                .orgFileName("old_original.jpg")
                .letterContents("기존 내용입니다.")
                .build();

        RecipientRequestDto requestDto = RecipientRequestDto.builder()
                .letterWriter("새 작성자")
                .anonymityFlag("N")
                .letterContents("수정된 게시물 내용입니다.")
                .imageFile(null)
                .build();

        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(existingEntity));
        when(recipientRepository.save(any(RecipientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RecipientDetailResponseDto result = recipientService.updateRecipient(letterSeq, requestDto);

        // Then
        assertThat(result.getImageUrl()).isNotNull(); // 기존 URL이 유지되므로 null이 아님
        assertThat(result.getImageUrl()).contains("old_uuid.jpg"); // 기존 파일명 포함 확인
        assertThat(result.getOrgFileName()).isEqualTo("old_original.jpg");

        verify(recipientRepository, times(1)).save(any(RecipientEntity.class));
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