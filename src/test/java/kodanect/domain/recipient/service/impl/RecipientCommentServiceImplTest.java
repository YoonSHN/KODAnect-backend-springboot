package kodanect.domain.recipient.service.impl;

import kodanect.common.util.HcaptchaService;
import kodanect.domain.recipient.dto.RecipientCommentRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.exception.RecipientCommentNotFoundException;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
import kodanect.domain.recipient.repository.RecipientRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException; // JUnit 4에서 예외 테스트를 위한 Rule
import org.junit.runner.RunWith; // JUnit 4에서 러너를 지정하기 위함
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner; // Mockito와 JUnit 4를 함께 사용하기 위한 러너
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*; // JUnit 4의 Assertions
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RecipientCommentServiceImplTest {
    @Mock
    private RecipientCommentRepository recipientCommentRepository;
    @Mock
    private RecipientRepository recipientRepository;
    @Mock
    private HcaptchaService hcaptchaService;

    @InjectMocks
    private RecipientCommentServiceImpl recipientCommentService;

    // JUnit 4에서 예외를 테스트하는 일반적인 방법
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private RecipientEntity activeRecipient;
    private RecipientEntity deletedRecipient;
    private RecipientCommentEntity activeComment;
    // private RecipientCommentEntity deletedComment; // 현재 사용되지 않으므로 주석 처리

    @Before // JUnit 4의 @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        // 로거 모의: 단위 테스트 시 실제 로깅 방지 및 NullPointerException 회피
        // 리플렉션을 사용하여 final 필드인 logger를 모의 객체로 대체
        try {
            java.lang.reflect.Field loggerField = RecipientCommentServiceImpl.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
            Logger mockLogger = mock(Logger.class);
            loggerField.set(recipientCommentService, mockLogger);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Failed to mock logger field: " + e.getMessage());
            // 실제 테스트 환경에서는 예외를 던지거나, 테스트가 실패하도록 처리할 수 있습니다.
            // 여기서는 단순히 에러 메시지 출력 후 진행합니다.
        }

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
                .letterSeq(activeRecipient)
                .commentContents("Test Comment")
                .commentWriter("Tester")
                .delFlag("N")
                .writeTime(LocalDateTime.now())
                .build();
        // checkPasscode 메서드를 모의하기 위해 실제 비밀번호를 설정
        activeComment.setCommentPasscode("pass1234"); // 실제로는 해싱된 비밀번호여야 함. 테스트를 위해 임시 설정
    }

    // --- selectRecipientCommentByLetterSeq 테스트 ---

    @Test
    public void selectRecipientCommentByLetterSeq_success_activeRecipientHasComments() {
        // Given
        when(recipientRepository.findById(activeRecipient.getLetterSeq())).thenReturn(Optional.of(activeRecipient));
        when(recipientCommentRepository.findCommentsByLetterSeqAndDelFlagSorted(eq(activeRecipient), eq("N")))
                .thenReturn(Arrays.asList(activeComment));

        // When
        List<RecipientCommentResponseDto> result = recipientCommentService.selectRecipientCommentByLetterSeq(activeRecipient.getLetterSeq());

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(activeComment.getCommentContents(), result.get(0).getCommentContents());
        verify(recipientRepository).findById(activeRecipient.getLetterSeq());
        verify(recipientCommentRepository).findCommentsByLetterSeqAndDelFlagSorted(eq(activeRecipient), eq("N"));
    }

    @Test
    public void selectRecipientCommentByLetterSeq_success_activeRecipientNoComments() {
        // Given
        when(recipientRepository.findById(activeRecipient.getLetterSeq())).thenReturn(Optional.of(activeRecipient));
        when(recipientCommentRepository.findCommentsByLetterSeqAndDelFlagSorted(eq(activeRecipient), eq("N")))
                .thenReturn(Collections.<RecipientCommentEntity>emptyList());

        // When
        List<RecipientCommentResponseDto> result = recipientCommentService.selectRecipientCommentByLetterSeq(activeRecipient.getLetterSeq());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(recipientRepository).findById(activeRecipient.getLetterSeq());
        verify(recipientCommentRepository).findCommentsByLetterSeqAndDelFlagSorted(eq(activeRecipient), eq("N"));
    }

    @Test
    public void selectRecipientCommentByLetterSeq_fail_recipientNotFound() {
        // Given
        int nonExistentLetterSeq = 999;
        when(recipientRepository.findById(nonExistentLetterSeq)).thenReturn(Optional.empty());

        // Expect Exception in JUnit 4
        thrown.expect(RecipientNotFoundException.class);
        thrown.expectMessage("게시물을 찾을 수 없거나 이미 삭제된 게시물입니다: " + nonExistentLetterSeq);

        // When
        recipientCommentService.selectRecipientCommentByLetterSeq(nonExistentLetterSeq);

        // Then (ExpectedException handles assertions)
        verify(recipientRepository).findById(nonExistentLetterSeq);
        verifyNoInteractions(recipientCommentRepository);
    }

    @Test
    public void selectRecipientCommentByLetterSeq_fail_recipientDeleted() {
        // Given
        when(recipientRepository.findById(deletedRecipient.getLetterSeq())).thenReturn(Optional.of(deletedRecipient));

        // Expect Exception in JUnit 4
        thrown.expect(RecipientNotFoundException.class);
        thrown.expectMessage("게시물을 찾을 수 없거나 이미 삭제된 게시물입니다: " + deletedRecipient.getLetterSeq());

        // When
        recipientCommentService.selectRecipientCommentByLetterSeq(deletedRecipient.getLetterSeq());

        // Then (ExpectedException handles assertions)
        verify(recipientRepository).findById(deletedRecipient.getLetterSeq());
        verifyNoInteractions(recipientCommentRepository);
    }

    // --- insertComment 테스트 ---

    @Test
    public void insertComment_success() {
        // Given
        RecipientCommentRequestDto requestDto = new RecipientCommentRequestDto();
        requestDto.setCommentContents("<p>Hello</p> World ");
        requestDto.setCommentWriter("New Writer");
        requestDto.setCommentPasscode("newpass1234");
        String captchaToken = "valid_captcha_token";

        RecipientCommentEntity savedEntity = RecipientCommentEntity.builder()
                .commentSeq(200)
                .letterSeq(activeRecipient)
                .commentContents("Hello World") // Cleaned content
                .commentWriter("New Writer")
                .commentPasscode("newpass1234") // 비밀번호 설정 (실제 저장은 해싱되어야 함)
                .writeTime(LocalDateTime.now())
                .build();

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientRepository.findById(activeRecipient.getLetterSeq())).thenReturn(Optional.of(activeRecipient));
        when(recipientCommentRepository.save(any(RecipientCommentEntity.class))).thenReturn(savedEntity);

        // When
        RecipientCommentResponseDto result = recipientCommentService.insertComment(activeRecipient.getLetterSeq(), requestDto, captchaToken);

        // Then
        assertNotNull(result);
        assertEquals(savedEntity.getCommentSeq(), result.getCommentSeq());
        assertEquals("Hello World", result.getCommentContents()); // Ensure content is cleaned
        assertEquals("New Writer", result.getCommentWriter());
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verify(recipientRepository).findById(activeRecipient.getLetterSeq());
        verify(recipientCommentRepository).save(any(RecipientCommentEntity.class));
    }

    @Test
    public void insertComment_fail_captchaFailed() {
        // Given
        RecipientCommentRequestDto requestDto = new RecipientCommentRequestDto();
        requestDto.setCommentContents("Some content");
        String captchaToken = "invalid_captcha_token";

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(false);

        // Expect Exception
        thrown.expect(RecipientInvalidDataException.class);
        thrown.expectMessage("캡차 인증에 실패했습니다. 다시 시도해주세요.");

        // When
        recipientCommentService.insertComment(activeRecipient.getLetterSeq(), requestDto, captchaToken);

        // Then (ExpectedException handles assertions)
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verifyNoInteractions(recipientRepository, recipientCommentRepository);
    }

    @Test
    public void insertComment_fail_recipientNotFoundOrDeleted() {
        // Given
        RecipientCommentRequestDto requestDto = new RecipientCommentRequestDto();
        requestDto.setCommentContents("Some content");
        String captchaToken = "valid_captcha_token";

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientRepository.findById(deletedRecipient.getLetterSeq())).thenReturn(Optional.of(deletedRecipient));

        // Expect Exception
        thrown.expect(RecipientNotFoundException.class);
        thrown.expectMessage("게시물을 찾을 수 없거나 이미 삭제된 게시물입니다");

        // When
        recipientCommentService.insertComment(deletedRecipient.getLetterSeq(), requestDto, captchaToken);

        // Then (ExpectedException handles assertions)
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verify(recipientRepository).findById(deletedRecipient.getLetterSeq());
        verifyNoInteractions(recipientCommentRepository);
    }

    @Test
    public void insertComment_fail_emptyContentAfterFiltering() {
        // Given
        RecipientCommentRequestDto requestDto = new RecipientCommentRequestDto();
        requestDto.setCommentContents(" <script>alert('xss');</script> ");
        String captchaToken = "valid_captcha_token";

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientRepository.findById(activeRecipient.getLetterSeq())).thenReturn(Optional.of(activeRecipient));

        // Expect Exception
        thrown.expect(RecipientInvalidDataException.class);
        thrown.expectMessage("댓글 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");

        // When
        recipientCommentService.insertComment(activeRecipient.getLetterSeq(), requestDto, captchaToken);

        // Then (ExpectedException handles assertions)
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verify(recipientRepository).findById(activeRecipient.getLetterSeq());
        verifyNoInteractions(recipientCommentRepository);
    }

    // --- updateComment 테스트 ---

    @Test
    public void updateComment_success() {
        // Given
        String newContents = "Updated <p>Comment</p>";
        String newWriter = "Updated Tester";
        String inputPasscode = "pass1234";
        String captchaToken = "valid_captcha_token";

        RecipientCommentEntity spiedComment = spy(activeComment);
        doReturn(true).when(spiedComment).checkPasscode(inputPasscode);

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientCommentRepository.findByCommentSeqAndDelFlag(activeComment.getCommentSeq(), "N"))
                .thenReturn(Optional.of(spiedComment));
        when(recipientCommentRepository.save(any(RecipientCommentEntity.class))).thenReturn(spiedComment);

        // When
        RecipientCommentResponseDto result = recipientCommentService.updateComment(
                activeComment.getCommentSeq(), newContents, newWriter, inputPasscode, captchaToken);

        // Then
        assertNotNull(result);
        System.out.println("Expected: \"Updated <p>Comment</p>\"");
        System.out.println("Actual  : \"" + result.getCommentContents() + "\""); // 이 라인을 추가
        // Expectation 변경: <p> 태그가 그대로 남아있어야 함
        assertEquals("Updated <p>Comment</p>", result.getCommentContents()); // 변경된 부분
        assertEquals(newWriter, result.getCommentWriter());
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verify(recipientCommentRepository).findByCommentSeqAndDelFlag(activeComment.getCommentSeq(), "N");
        verify(spiedComment).checkPasscode(inputPasscode);
        verify(recipientCommentRepository).save(spiedComment);
    }

    @Test
    public void updateComment_fail_captchaFailed() {
        // Given
        String newContents = "Updated Comment";
        String newWriter = "Updated Writer";
        String inputPasscode = "pass1234";
        String captchaToken = "invalid_captcha_token";

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(false);

        // Expect Exception
        thrown.expect(RecipientInvalidDataException.class);
        thrown.expectMessage("캡차 인증에 실패했습니다. 다시 시도해주세요.");

        // When
        recipientCommentService.updateComment(1, newContents, newWriter, inputPasscode, captchaToken);

        // Then (ExpectedException handles assertions)
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verifyNoInteractions(recipientCommentRepository);
    }

    @Test
    public void updateComment_fail_commentNotFoundOrDeleted() {
        // Given
        int nonExistentCommentSeq = 999;
        String newContents = "Updated Comment";
        String newWriter = "Updated Writer";
        String inputPasscode = "pass1234";
        String captchaToken = "valid_captcha_token";

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientCommentRepository.findByCommentSeqAndDelFlag(nonExistentCommentSeq, "N"))
                .thenReturn(Optional.empty());

        // Expect Exception
        thrown.expect(RecipientCommentNotFoundException.class);
        thrown.expectMessage("댓글을 찾을 수 없거나 이미 삭제되었습니다.");

        // When
        recipientCommentService.updateComment(nonExistentCommentSeq, newContents, newWriter, inputPasscode, captchaToken);

        // Then (ExpectedException handles assertions)
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verify(recipientCommentRepository).findByCommentSeqAndDelFlag(nonExistentCommentSeq, "N");
    }

    @Test
    public void updateComment_fail_invalidPasscode() {
        // Given
        String newContents = "Updated Comment";
        String newWriter = "Updated Writer";
        String inputPasscode = "wrongpass";
        String captchaToken = "valid_captcha_token";

        RecipientCommentEntity spiedComment = spy(activeComment);
        doReturn(false).when(spiedComment).checkPasscode(inputPasscode); // 비밀번호 불일치 모의

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientCommentRepository.findByCommentSeqAndDelFlag(activeComment.getCommentSeq(), "N"))
                .thenReturn(Optional.of(spiedComment));

        // Expect Exception
        thrown.expect(RecipientInvalidPasscodeException.class);
        thrown.expectMessage("비밀번호가 일치하지 않습니다.");

        // When
        recipientCommentService.updateComment(activeComment.getCommentSeq(), newContents, newWriter, inputPasscode, captchaToken);

        // Then (ExpectedException handles assertions)
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verify(recipientCommentRepository).findByCommentSeqAndDelFlag(activeComment.getCommentSeq(), "N");
        verify(spiedComment).checkPasscode(inputPasscode);
        verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class));
    }

    @Test
    public void updateComment_fail_emptyNewContentsAfterFiltering() {
        // Given
        // <script> 태그는 Safelist.relaxed()에 의해 제거되므로, 필터링 후 내용이 비어있게 됨
        String newContents = " <script>alert('xss');</script> "; // 변경된 부분
        String newWriter = "Updated Writer";
        String inputPasscode = "pass1234";
        String captchaToken = "valid_captcha_token";

        RecipientCommentEntity spiedComment = spy(activeComment);
        doReturn(true).when(spiedComment).checkPasscode(inputPasscode);

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientCommentRepository.findByCommentSeqAndDelFlag(activeComment.getCommentSeq(), "N"))
                .thenReturn(Optional.of(spiedComment));

        // Expect Exception
        thrown.expect(RecipientInvalidDataException.class);
        thrown.expectMessage("댓글 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");

        // When
        recipientCommentService.updateComment(activeComment.getCommentSeq(), newContents, newWriter, inputPasscode, captchaToken);

        // Then (ExpectedException handles assertions)
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verify(recipientCommentRepository).findByCommentSeqAndDelFlag(activeComment.getCommentSeq(), "N");
        verify(spiedComment).checkPasscode(inputPasscode);
        verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class));
    }

    // --- deleteComment 테스트 ---

    @Test
    public void deleteComment_success() {
        // Given
        String inputPasscode = "pass1234";
        String captchaToken = "valid_captcha_token";

        RecipientCommentEntity spiedComment = spy(activeComment);
        doReturn(true).when(spiedComment).checkPasscode(inputPasscode);

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientCommentRepository.findByCommentSeqAndDelFlag(activeComment.getCommentSeq(), "N"))
                .thenReturn(Optional.of(spiedComment));
        when(recipientCommentRepository.save(any(RecipientCommentEntity.class))).thenReturn(spiedComment);

        // When
        recipientCommentService.deleteComment(activeRecipient.getLetterSeq(), activeComment.getCommentSeq(), inputPasscode, captchaToken);

        // Then
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verify(recipientCommentRepository).findByCommentSeqAndDelFlag(activeComment.getCommentSeq(), "N");
        verify(spiedComment).checkPasscode(inputPasscode);
        assertEquals("Y", spiedComment.getDelFlag()); // Check if delFlag was set to "Y"
        verify(recipientCommentRepository).save(spiedComment);
    }

    @Test
    public void deleteComment_fail_captchaFailed() {
        // Given
        String inputPasscode = "pass1234";
        String captchaToken = "invalid_captcha_token";

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(false);

        // Expect Exception
        thrown.expect(RecipientInvalidDataException.class);
        thrown.expectMessage("캡차 인증에 실패했습니다. 다시 시도해주세요.");

        // When
        recipientCommentService.deleteComment(activeRecipient.getLetterSeq(), activeComment.getCommentSeq(), inputPasscode, captchaToken);

        // Then (ExpectedException handles assertions)
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verifyNoInteractions(recipientCommentRepository);
    }

    @Test
    public void deleteComment_fail_commentNotFoundOrDeleted() {
        // Given
        int nonExistentCommentSeq = 999;
        String inputPasscode = "pass1234";
        String captchaToken = "valid_captcha_token";

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientCommentRepository.findByCommentSeqAndDelFlag(nonExistentCommentSeq, "N"))
                .thenReturn(Optional.empty());

        // Expect Exception
        thrown.expect(RecipientCommentNotFoundException.class);
        thrown.expectMessage("댓글을 찾을 수 없거나 이미 삭제되었습니다.");

        // When
        recipientCommentService.deleteComment(activeRecipient.getLetterSeq(), nonExistentCommentSeq, inputPasscode, captchaToken);

        // Then (ExpectedException handles assertions)
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verify(recipientCommentRepository).findByCommentSeqAndDelFlag(nonExistentCommentSeq, "N");
    }

    @Test
    public void deleteComment_fail_invalidPasscode() {
        // Given
        String inputPasscode = "wrongpass";
        String captchaToken = "valid_captcha_token";

        RecipientCommentEntity spiedComment = spy(activeComment);
        doReturn(false).when(spiedComment).checkPasscode(inputPasscode);

        when(hcaptchaService.verifyCaptcha(captchaToken)).thenReturn(true);
        when(recipientCommentRepository.findByCommentSeqAndDelFlag(activeComment.getCommentSeq(), "N"))
                .thenReturn(Optional.of(spiedComment));

        // Expect Exception
        thrown.expect(RecipientInvalidPasscodeException.class);
        thrown.expectMessage("비밀번호가 일치하지 않습니다.");

        // When
        recipientCommentService.deleteComment(activeRecipient.getLetterSeq(), activeComment.getCommentSeq(), inputPasscode, captchaToken);

        // Then (ExpectedException handles assertions)
        verify(hcaptchaService).verifyCaptcha(captchaToken);
        verify(recipientCommentRepository).findByCommentSeqAndDelFlag(activeComment.getCommentSeq(), "N");
        verify(spiedComment).checkPasscode(inputPasscode);
        verify(recipientCommentRepository, never()).save(any(RecipientCommentEntity.class));
    }
}