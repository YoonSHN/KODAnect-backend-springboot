package kodanect.domain.recipient.service.impl;

import kodanect.domain.recipient.dto.RecipientCommentUpdateRequestDto;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
import kodanect.domain.recipient.dto.RecipientCommentRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.exception.RecipientCommentNotFoundException;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
import kodanect.domain.recipient.repository.RecipientRepository;

import org.jsoup.Jsoup; // Jsoup import
import org.jsoup.safety.Safelist; // Safelist import

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RecipientCommentServiceImplTest {

    @Mock
    private RecipientCommentRepository recipientCommentRepository;

    @Mock
    private RecipientRepository recipientRepository;

    @InjectMocks // 테스트 대상 서비스
    private RecipientCommentServiceImpl recipientCommentService;

    @Rule // 예외 테스트를 위한 ExpectedException
    public ExpectedException thrown = ExpectedException.none();

    // 테스트에 사용될 가상 데이터
    private RecipientEntity activeRecipient;
    private RecipientEntity deletedRecipient;
    private RecipientCommentEntity activeComment;
    private RecipientCommentEntity deletedComment;
    private RecipientCommentUpdateRequestDto baseUpdateDto;

    @Before
    public void setUp() {
        // 테스트를 위한 가상 엔티티 초기화
        activeRecipient = RecipientEntity.builder()
                .letterSeq(1)
                .delFlag("N")
                .build();

        deletedRecipient = RecipientEntity.builder()
                .letterSeq(2)
                .delFlag("Y")
                .build();

        activeComment = RecipientCommentEntity.builder()
                .commentSeq(100)
                .letterSeq(activeRecipient) // 부모 게시물 연결
                .contents("원래 댓글 내용")
                .commentWriter("원래 작성자")
                .delFlag("N")
                .writeTime(LocalDateTime.now())
                .build();
        activeComment.setCommentPasscode("pass1234"); // 비밀번호 설정

        deletedComment = RecipientCommentEntity.builder()
                .commentSeq(101)
                .letterSeq(activeRecipient) // 부모 게시물 연결
                .contents("삭제된 댓글 내용")
                .commentWriter("삭제된 작성자")
                .delFlag("Y") // 삭제된 상태
                .writeTime(LocalDateTime.now())
                .build();
        deletedComment.setCommentPasscode("pass5678");

        baseUpdateDto = RecipientCommentUpdateRequestDto.builder()
                .commentWriter("수정된 작성자")
                .contents("업데이트된 내용")
                .commentPasscode("pass1234")
                .build();
    }

    // --- insertComment 테스트 ---

    @Test
    public void insertComment_Success() {
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            // Given
            Integer letterSeq = activeRecipient.getLetterSeq();
            RecipientCommentRequestDto requestDto = RecipientCommentRequestDto.builder()
                    .contents("<p>새로운 댓글 내용</p>")
                    .commentWriter("새로운 작성자")
                    .commentPasscode("newpass")
                    .build();

            // Jsoup.clean() Mocking: HTML 태그 제거 후 깨끗한 내용 반환
            mockedJsoup.when(() -> Jsoup.clean(anyString(), any(Safelist.class)))
                    .thenReturn("새로운 댓글 내용");

            // recipientRepository.findById() Mocking: 활성 게시물 반환
            when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(activeRecipient));

            // --- 수정된 부분: save() 메서드가 인자로 받은 객체에 commentSeq를 설정하고 반환하도록 Mocking ---
            when(recipientCommentRepository.save(any(RecipientCommentEntity.class)))
                    .thenAnswer(new Answer<RecipientCommentEntity>() {
                        @Override
                        public RecipientCommentEntity answer(InvocationOnMock invocation) throws Throwable {
                            RecipientCommentEntity entity = invocation.getArgument(0); // save()의 첫 번째 인자(저장될 엔티티)를 가져옵니다.
                            entity.setCommentSeq(activeComment.getCommentSeq()); // activeComment와 동일한 시퀀스를 부여
                            return entity;
                        }
                    });

            // When
            RecipientCommentResponseDto result = recipientCommentService.insertComment(letterSeq, requestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCommentSeq()).isEqualTo(activeComment.getCommentSeq()); // Mocking된 반환 값 확인 (100)
            assertThat(result.getContents()).isEqualTo("새로운 댓글 내용"); // 클린된 내용으로 저장됐는지 확인

            verify(recipientRepository, times(1)).findById(letterSeq);
            verify(recipientCommentRepository, times(1)).save(any(RecipientCommentEntity.class));
            mockedJsoup.verify(() -> Jsoup.clean(anyString(), any(Safelist.class)), times(1));
        }
    }

    @Test
    public void insertComment_RecipientNotFound() {
        // Given
        Integer letterSeq = 999; // 존재하지 않는 게시물 시퀀스

        when(recipientRepository.findById(letterSeq)).thenReturn(Optional.empty());

        // Then
        thrown.expect(RecipientNotFoundException.class);
        thrown.expectMessage("[대상 없음] recipientId=" + letterSeq);

        // When
        recipientCommentService.insertComment(letterSeq, RecipientCommentRequestDto.builder().build());

        verify(recipientRepository, times(1)).findById(letterSeq);
        verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class));
    }

    @Test
    public void insertComment_EmptyContentsAfterCleaning() {
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            // Given
            Integer letterSeq = activeRecipient.getLetterSeq();
            RecipientCommentRequestDto requestDto = RecipientCommentRequestDto.builder()
                    .contents("<p></p>") // 내용이 없거나 필터링 후 비어질 내용
                    .commentWriter("test")
                    .commentPasscode("pass")
                    .build();

            // Jsoup.clean() Mocking: 필터링 후 빈 문자열 반환
            mockedJsoup.when(() -> Jsoup.clean(anyString(), any(Safelist.class)))
                    .thenReturn("   "); // 공백만 반환하여 trim 후 isEmpty()가 true 되도록

            when(recipientRepository.findById(letterSeq)).thenReturn(Optional.of(activeRecipient));

            // Then
            thrown.expect(RecipientInvalidDataException.class);
            thrown.expectMessage("댓글 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");

            // When
            recipientCommentService.insertComment(letterSeq, requestDto);

            verify(recipientRepository, times(1)).findById(letterSeq);
            verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class));
            mockedJsoup.verify(() -> Jsoup.clean(anyString(), any(Safelist.class)), times(1));
        }
    }

    // --- updateComment 테스트 ---

    @Test
    public void updateComment_Success() {
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            // Given
            Integer commentSeq = activeComment.getCommentSeq();

            LocalDateTime beforeServiceCallTime = LocalDateTime.now();

            mockedJsoup.when(() -> Jsoup.clean(anyString(), any(Safelist.class)))
                    .thenReturn("업데이트된 내용");

            // --- 여기를 수정합니다 ---
            // recipientCommentRepository.findById() 대신 findByCommentSeqAndDelFlag()를 Mocking
            when(recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq, "N"))
                    .thenReturn(Optional.of(activeComment));

            when(recipientCommentRepository.save(any(RecipientCommentEntity.class)))
                    .thenReturn(activeComment);

            // When
            RecipientCommentResponseDto result = recipientCommentService.updateComment(commentSeq, baseUpdateDto);

            LocalDateTime afterServiceCallTime = LocalDateTime.now();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContents()).isEqualTo("업데이트된 내용");
            assertThat(result.getCommentWriter()).isEqualTo(baseUpdateDto.getCommentWriter());

            assertThat(result.getModifyTime())
                    .isAfterOrEqualTo(beforeServiceCallTime)
                    .isBeforeOrEqualTo(afterServiceCallTime);

            // --- verify 부분도 수정합니다 ---
            verify(recipientCommentRepository, times(1)).findByCommentSeqAndDelFlag(commentSeq, "N");
            verify(recipientCommentRepository, times(1)).save(any(RecipientCommentEntity.class));
            mockedJsoup.verify(() -> Jsoup.clean(eq(baseUpdateDto.getContents()), any(Safelist.class)), times(1));
        }
    }

    @Test
    public void updateComment_CommentNotFound() {
        // Given
        Integer nonExistentCommentSeq = 999;

        // 여기도 findById() 대신 findByCommentSeqAndDelFlag()를 Mocking
        when(recipientCommentRepository.findByCommentSeqAndDelFlag(nonExistentCommentSeq, "N"))
                .thenReturn(Optional.empty());

        // When & Then
        thrown.expect(RecipientCommentNotFoundException.class);
        thrown.expectMessage(String.format("[댓글 없음] commentId=%d", nonExistentCommentSeq));

        recipientCommentService.updateComment(nonExistentCommentSeq, baseUpdateDto);

        verify(recipientCommentRepository, times(1)).findByCommentSeqAndDelFlag(nonExistentCommentSeq, "N");
        verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class));
    }

    @Test
    public void updateComment_EmptyContentsAfterCleaning() {
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            // Given
            Integer commentSeq = activeComment.getCommentSeq();

            mockedJsoup.when(() -> Jsoup.clean(anyString(), any(Safelist.class)))
                    .thenReturn("  ");

            // 여기도 findById() 대신 findByCommentSeqAndDelFlag()를 Mocking
            when(recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq, "N"))
                    .thenReturn(Optional.of(activeComment));

            RecipientCommentUpdateRequestDto emptyContentsDto = RecipientCommentUpdateRequestDto.builder()
                    .commentWriter(baseUpdateDto.getCommentWriter())
                    .contents("<p></p>")
                    .commentPasscode(baseUpdateDto.getCommentPasscode())
                    .build();

            // When & Then
            thrown.expect(RecipientInvalidDataException.class);
            thrown.expectMessage("댓글 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");

            recipientCommentService.updateComment(commentSeq, emptyContentsDto);

            verify(recipientCommentRepository, times(1)).findByCommentSeqAndDelFlag(commentSeq, "N");
            verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class));
            mockedJsoup.verify(() -> Jsoup.clean(eq(emptyContentsDto.getContents()), any(Safelist.class)), times(1));
        }
    }


    // --- deleteComment 테스트 ---

    @Test
    public void deleteComment_Success() {
        // Given
        Integer letterSeq = activeRecipient.getLetterSeq(); // deleteComment는 letterSeq도 인자로 받음
        Integer commentSeq = activeComment.getCommentSeq();
        String inputPasscode = "pass1234";

        when(recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq, "N"))
                .thenReturn(Optional.of(activeComment));

        // When
        recipientCommentService.deleteComment(letterSeq, commentSeq, inputPasscode);

        // Then
        // save 메서드가 호출되었는지 (delFlag가 'Y'로 업데이트되었는지)
        verify(recipientCommentRepository, times(1)).findByCommentSeqAndDelFlag(commentSeq, "N");
        verify(recipientCommentRepository, times(1)).save(activeComment); // activeComment 객체가 Y로 업데이트된 후 저장됨
        assertThat(activeComment.getDelFlag()).isEqualTo("Y"); // 실제 객체의 상태 변화도 검증
    }

    @Test
    public void deleteComment_CommentNotFound() {
        // Given
        Integer letterSeq = 1;
        Integer commentSeq = 999; // 존재하지 않는 댓글
        String inputPasscode = "pass";

        when(recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq, "N"))
                .thenReturn(Optional.empty());

        // Then
        thrown.expect(RecipientCommentNotFoundException.class);
        thrown.expectMessage("[댓글 없음] commentId=" + commentSeq);

        // When
        recipientCommentService.deleteComment(letterSeq, commentSeq, inputPasscode);

        verify(recipientCommentRepository, times(1)).findByCommentSeqAndDelFlag(commentSeq, "N");
        verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class));
    }

    @Test
    public void deleteComment_InvalidPasscode() {
        // Given
        Integer letterSeq = activeRecipient.getLetterSeq();
        Integer commentSeq = activeComment.getCommentSeq();
        String wrongPasscode = "wrongpass"; // 잘못된 비밀번호

        when(recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq, "N"))
                .thenReturn(Optional.of(activeComment));

        // Then
        thrown.expect(RecipientInvalidPasscodeException.class);
        // 여기서 메시지를 실제 예외가 던지는 메시지에 맞게 수정합니다.
        thrown.expectMessage(String.format("[비밀번호 불일치] 리소스 ID=%d", commentSeq)); // <-- 수정된 부분!

        // When
        recipientCommentService.deleteComment(letterSeq, commentSeq, wrongPasscode);

        verify(recipientCommentRepository, times(1)).findByCommentSeqAndDelFlag(commentSeq, "N");
        verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class));
    }
}