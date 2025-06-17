package kodanect.domain.recipient.service.impl;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Before
    public void setUp() {
        // MockitoAnnotations.initMocks(this); // @RunWith(MockitoJUnitRunner.class) 사용 시 불필요

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
            String newContents = "업데이트된 <br>내용";
            String newWriter = "수정된 작성자";

            // 서비스 호출 전의 시간을 기록합니다.
            LocalDateTime beforeServiceCallTime = LocalDateTime.now();

            // Jsoup.clean() Mocking: HTML 태그 제거 후 공백 정리된 내용 반환
            mockedJsoup.when(() -> Jsoup.clean(anyString(), any(Safelist.class)))
                    .thenReturn("업데이트된 내용");

            // recipientCommentRepository.findByCommentSeqAndDelFlag() Mocking: 활성 댓글 반환
            when(recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq, "N"))
                    .thenReturn(Optional.of(activeComment));

            // recipientCommentRepository.save() Mocking: 업데이트된 댓글 반환
            // 서비스 메서드가 activeComment를 직접 수정하므로, 해당 인스턴스를 반환하도록 Mocking
            when(recipientCommentRepository.save(any(RecipientCommentEntity.class)))
                    .thenReturn(activeComment);

            // When
            RecipientCommentResponseDto result = recipientCommentService.updateComment(commentSeq, newContents, newWriter);

            // 서비스 호출 후의 시간을 기록합니다.
            LocalDateTime afterServiceCallTime = LocalDateTime.now();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContents()).isEqualTo("업데이트된 내용");
            assertThat(result.getCommentWriter()).isEqualTo(newWriter);

            // modifyTime이 서비스 호출 전후 시간 사이에 있는지 확인합니다.
            // LocalDateTime.now()는 매우 빠르게 호출될 수 있으므로 'strictly after' 대신 'after or equal to'를 사용합니다.
            assertThat(result.getModifyTime())
                    .isAfterOrEqualTo(beforeServiceCallTime)
                    .isBeforeOrEqualTo(afterServiceCallTime);

            // verify 호출 횟수
            verify(recipientCommentRepository, times(1)).findByCommentSeqAndDelFlag(commentSeq, "N");
            verify(recipientCommentRepository, times(1)).save(any(RecipientCommentEntity.class));
            mockedJsoup.verify(() -> Jsoup.clean(anyString(), any(Safelist.class)), times(1));
        }
    }

    @Test
    public void updateComment_CommentNotFound() {
        // Given
        Integer nonExistentCommentSeq = 999;
        String newContents = "내용";
        String newWriter = "작가";

        when(recipientCommentRepository.findByCommentSeqAndDelFlag(nonExistentCommentSeq, "N"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> recipientCommentService.updateComment(nonExistentCommentSeq, newContents, newWriter))
                .isInstanceOf(RecipientCommentNotFoundException.class)
                .hasMessageContaining("[댓글 없음] commentId=" + nonExistentCommentSeq); // 이 부분을 수정했습니다.

        verify(recipientCommentRepository, times(1)).findByCommentSeqAndDelFlag(nonExistentCommentSeq, "N");
        verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class));
    }

    @Test
    public void updateComment_InvalidPasscode() {
        // Given
        Integer commentSeq = activeComment.getCommentSeq();
        String wrongPasscode = "wrongpass"; // 잘못된 비밀번호

        when(recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq, "N"))
                .thenReturn(Optional.of(activeComment));

        // When & Then (updateComment_InvalidPasscode는 authenticateComment를 테스트해야 합니다.)
        // updateComment는 이제 비밀번호를 검증하지 않으므로, 이 테스트는 authenticateComment를 호출해야 합니다.
        assertThatThrownBy(() -> recipientCommentService.authenticateComment(commentSeq, wrongPasscode))
                .isInstanceOf(RecipientInvalidPasscodeException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다.");

        verify(recipientCommentRepository, times(1)).findByCommentSeqAndDelFlag(commentSeq, "N");
        verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class)); // authenticateComment는 save를 호출하지 않음
    }

    @Test
    public void updateComment_EmptyContentsAfterCleaning() {
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            // Given
            Integer commentSeq = activeComment.getCommentSeq();
            String newContents = "<p></p>"; // 필터링 후 비어질 내용
            String newWriter = "작가";
            // String inputPasscode = "pass1234"; // 이 메서드에서는 더 이상 사용되지 않음

            // Jsoup.clean()이 공백만 반환하도록 Mocking (trim 후 비어지도록)
            mockedJsoup.when(() -> Jsoup.clean(anyString(), any(Safelist.class)))
                    .thenReturn("  ");

            when(recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq, "N"))
                    .thenReturn(Optional.of(activeComment));

            // When & Then
            assertThatThrownBy(() -> recipientCommentService.updateComment(commentSeq, newContents, newWriter))
                    .isInstanceOf(RecipientInvalidDataException.class)
                    .hasMessageContaining("댓글 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");

            verify(recipientCommentRepository, times(1)).findByCommentSeqAndDelFlag(commentSeq, "N");
            verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class));
            mockedJsoup.verify(() -> Jsoup.clean(anyString(), any(Safelist.class)), times(1));
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
        thrown.expectMessage("비밀번호가 일치하지 않습니다.");

        // When
        recipientCommentService.deleteComment(letterSeq, commentSeq, wrongPasscode);

        verify(recipientCommentRepository, times(1)).findByCommentSeqAndDelFlag(commentSeq, "N");
        verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class));
    }
}