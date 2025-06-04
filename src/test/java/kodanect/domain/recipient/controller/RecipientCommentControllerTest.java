package kodanect.domain.recipient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.exception.custom.RecipientExceptionHandler;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import kodanect.domain.recipient.dto.CommentDeleteRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
import kodanect.domain.recipient.repository.RecipientRepository;
import kodanect.domain.recipient.service.RecipientCommentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(
        controllers = {RecipientCommentController.class, RecipientExceptionHandler.class},
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class}
        )
public class RecipientCommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // DTO를 JSON으로 변환하기 위해 필요

    @MockBean
    private RecipientCommentService recipientCommentService;

    @MockBean
    private MessageSourceAccessor messageSourceAccessor;

    @MockBean
    private RecipientRepository recipientRepository;

    @MockBean
    private RecipientCommentRepository recipientCommentRepository;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;

    @Before
    public void setup() {
        // 각 테스트 전에 필요한 초기화 작업
    }

    /**---

            ## 1. 댓글 작성 API (`POST /{letterSeq}/comments`) 테스트

    ### 성공 케이스: 댓글 작성 성공

    ```java*/
    @Test
    public void writeComment_success() throws Exception {
        // given
        int letterSeq = 1;
        RecipientCommentRequestDto requestDto = RecipientCommentRequestDto.builder()
                .commentContents("새로운 댓글입니다.")
                .commentWriter("테스터")
                .commentPasscode("pass1234") // 실제 유효성 검사 규칙에 맞게 설정
                .captchaToken("valid-captcha")
                .build();

        RecipientCommentResponseDto mockResponseDto = RecipientCommentResponseDto.builder()
                .commentSeq(1)
                .letterSeq(letterSeq)
                .commentContents(requestDto.getCommentContents())
                .commentWriter(requestDto.getCommentWriter())
                .writeTime(LocalDateTime.now())
                .build();

        // 서비스 Mocking: 모든 인자가 매칭될 때 mockResponseDto를 반환
        when(recipientCommentService.insertComment(
                eq(letterSeq),
                any(RecipientCommentRequestDto.class), // DTO 객체는 any()로 매칭
                eq(requestDto.getCaptchaToken())
        )).thenReturn(mockResponseDto);

        // when
        ResultActions actions = mockMvc.perform(post("/recipientLetters/{letterSeq}/comments", letterSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)) // DTO 객체를 JSON 문자열로 변환
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isCreated()) // HttpStatus.CREATED (201)
                .andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.message").value("댓글이 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data.commentSeq").value(mockResponseDto.getCommentSeq()))
                .andExpect(jsonPath("$.data.letterSeq").value(mockResponseDto.getLetterSeq()))
                .andDo(print());

        // 서비스 메서드 호출 검증
        verify(recipientCommentService).insertComment(
                eq(letterSeq),
                any(RecipientCommentRequestDto.class),
                eq(requestDto.getCaptchaToken())
        );
    }

    /**---

            ### 실패 케이스: 유효성 검사 실패 (ex: 내용 누락)

    ```java*/
    @Test
    public void writeComment_validationFailed() throws Exception {
        // given
        int letterSeq = 1;
        // 유효성 검사 실패 조건: commentContents가 null (또는 @NotBlank 규칙 위반)
        RecipientCommentRequestDto requestDto = RecipientCommentRequestDto.builder()
                .commentContents(null) // 내용 누락
                .commentWriter("테스터")
                .commentPasscode("pass1234")
                .captchaToken("valid-captcha")
                .build();

        // 서비스 Mocking은 필요 없습니다. 유효성 검사 단계에서 이미 실패하기 때문입니다.

        // when
        ResultActions actions = mockMvc.perform(post("/recipientLetters/{letterSeq}/comments", letterSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isBadRequest()) // HttpStatus.BAD_REQUEST (400)
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                // 에러 메시지는 @Valid 어노테이션에 따라 다를 수 있습니다.
                // 정확한 메시지를 확인하려면 실제로 이 테스트를 한번 실행해봐야 합니다.
                .andExpect(jsonPath("$.message").exists()) // 메시지 존재 여부만 확인
                .andDo(print());

        // 서비스 메서드는 호출되지 않았을 것입니다.
        verify(recipientCommentService, never()).insertComment(anyInt(), any(RecipientCommentRequestDto.class), anyString());
    }

    /**---

            ### 실패 케이스: 서비스 예외 발생 (ex: 캡차 인증 실패)

    ```java*/
    @Test
    public void writeComment_serviceException_captchaFailed() throws Exception {
        // given
        int letterSeq = 1;
        RecipientCommentRequestDto requestDto = RecipientCommentRequestDto.builder()
                .commentContents("댓글 내용")
                .commentWriter("테스터")
                .commentPasscode("pass1234")
                .captchaToken("invalid-captcha") // 유효하지 않은 캡차
                .build();

        // 서비스 Mocking: RecipientInvalidDataException 발생
        when(recipientCommentService.insertComment(
                eq(letterSeq),
                any(RecipientCommentRequestDto.class),
                eq(requestDto.getCaptchaToken())
        )).thenThrow(new RecipientInvalidDataException("캡차 인증에 실패했습니다.")); // 특정 예외 발생

        // when
        ResultActions actions = mockMvc.perform(post("/recipientLetters/{letterSeq}/comments", letterSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isBadRequest()) // HttpStatus.BAD_REQUEST (400)
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("캡차 인증에 실패했습니다."))
                .andDo(print());

        // 서비스 메서드 호출 검증
        verify(recipientCommentService).insertComment(
                eq(letterSeq),
                any(RecipientCommentRequestDto.class),
                eq(requestDto.getCaptchaToken())
        );
    }

    /**---

            ## 2. 댓글 수정 API (`PUT /{letterSeq}/comments/{commentSeq}`) 테스트

    ### 성공 케이스: 댓글 수정 성공

    ```java*/
    @Test
    public void updateComment_success() throws Exception {
        // given
        int letterSeq = 1;
        int commentSeq = 101;
        RecipientCommentRequestDto requestDto = RecipientCommentRequestDto.builder()
                .commentContents("수정된 댓글 내용입니다.")
                .commentWriter("수정자")
                .commentPasscode("pass1234") // 실제 유효성 검사 규칙에 맞게 설정
                .captchaToken("valid-captcha")
                .build();

        RecipientCommentResponseDto mockResponseDto = RecipientCommentResponseDto.builder()
                .commentSeq(commentSeq)
                .letterSeq(letterSeq) // 수정 시 letterSeq는 DTO에 포함되지 않지만, 응답 DTO에는 있을 수 있음
                .commentContents(requestDto.getCommentContents())
                .commentWriter(requestDto.getCommentWriter())
                .writeTime(LocalDateTime.now()) // 수정 시간은 새로 찍힐 수 있음
                .build();

        // 서비스 Mocking
        when(recipientCommentService.updateComment(
                eq(commentSeq),
                eq(requestDto.getCommentContents()),
                eq(requestDto.getCommentWriter()),
                eq(requestDto.getCommentPasscode()),
                eq(requestDto.getCaptchaToken())
        )).thenReturn(mockResponseDto);

        // when
        ResultActions actions = mockMvc.perform(put("/recipientLetters/{letterSeq}/comments/{commentSeq}", letterSeq, commentSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("댓글이 성공적으로 수정되었습니다."))
                .andExpect(jsonPath("$.data.commentSeq").value(mockResponseDto.getCommentSeq()))
                .andExpect(jsonPath("$.data.commentContents").value(mockResponseDto.getCommentContents()))
                .andDo(print());

        // 서비스 메서드 호출 검증
        verify(recipientCommentService).updateComment(
                eq(commentSeq),
                eq(requestDto.getCommentContents()),
                eq(requestDto.getCommentWriter()),
                eq(requestDto.getCommentPasscode()),
                eq(requestDto.getCaptchaToken())
        );
    }

    /**---

            ### 실패 케이스: 댓글 수정 - 댓글을 찾을 수 없음

    ```java*/
    @Test
    public void updateComment_notFound() throws Exception {
        // given
        int letterSeq = 1;
        int nonExistentCommentSeq = 999; // 존재하지 않는 댓글 시퀀스
        RecipientCommentRequestDto requestDto = RecipientCommentRequestDto.builder()
                .commentContents("수정 내용")
                .commentWriter("수정자")
                .commentPasscode("pass1234")
                .captchaToken("valid-captcha")
                .build();

        // 서비스 Mocking: RecipientNotFoundException 발생
        when(recipientCommentService.updateComment(
                eq(nonExistentCommentSeq), // 존재하지 않는 댓글 시퀀스
                anyString(), anyString(), anyString(), anyString()
        )).thenThrow(new RecipientNotFoundException("댓글을 찾을 수 없습니다."));

        // when
        ResultActions actions = mockMvc.perform(put("/recipientLetters/{letterSeq}/comments/{commentSeq}", letterSeq, nonExistentCommentSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isNotFound()) // HttpStatus.NOT_FOUND (404)
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다."))
                .andDo(print());

        // 서비스 메서드 호출 검증
        verify(recipientCommentService).updateComment(
                eq(nonExistentCommentSeq),
                anyString(), anyString(), anyString(), anyString()
        );
    }

    /**---

            ### 실패 케이스: 댓글 수정 - 권한 없음 (비밀번호 불일치)

    ```java*/
    @Test
    public void updateComment_unauthorized() throws Exception {
        // given
        int letterSeq = 1;
        int commentSeq = 101;
        RecipientCommentRequestDto requestDto = RecipientCommentRequestDto.builder()
                .commentContents("수정 내용")
                .commentWriter("수정자")
                // 유효성 검사 규칙을 통과하지만, 실제로는 비밀번호가 틀린 값으로 설정
                .commentPasscode("wrongPa55w0rd")
                .captchaToken("valid-captcha")
                .build();

        // 서비스 Mocking: RecipientInvalidPasscodeException 발생으로 변경!
        when(recipientCommentService.updateComment(
                eq(commentSeq),
                anyString(), anyString(), eq(requestDto.getCommentPasscode()), anyString()
        )).thenThrow(new RecipientInvalidPasscodeException("비밀번호가 일치하지 않습니다.")); // <-- RecipientInvalidPasscodeException으로 변경

        // when
        ResultActions actions = mockMvc.perform(put("/recipientLetters/{letterSeq}/comments/{commentSeq}", letterSeq, commentSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isUnauthorized()) // HttpStatus.UNAUTHORIZED (401)
                .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
                .andDo(print());

        // 서비스 메서드 호출 검증
        verify(recipientCommentService).updateComment(
                eq(commentSeq),
                anyString(), anyString(), eq(requestDto.getCommentPasscode()), anyString()
        );
    }

    /**---

            ## 3. 댓글 삭제 API (`DELETE /{letterSeq}/comments/{commentSeq}`) 테스트

    ### 성공 케이스: 댓글 삭제 성공

    ```java*/
    @Test
    public void deleteComment_success() throws Exception {
        // given
        int letterSeq = 1;
        int commentSeq = 101;
        CommentDeleteRequestDto requestDto = CommentDeleteRequestDto.builder()
                .commentPasscode("pass1234") // 실제 유효성 검사 규칙에 맞게 설정
                .captchaToken("valid-captcha")
                .build();

        // 서비스 Mocking: void 메서드이므로 doNothing() 사용
        doNothing().when(recipientCommentService).deleteComment(
                eq(letterSeq),
                eq(commentSeq),
                eq(requestDto.getCommentPasscode()),
                eq(requestDto.getCaptchaToken())
        );

        // when
        ResultActions actions = mockMvc.perform(delete("/recipientLetters/{letterSeq}/comments/{commentSeq}", letterSeq, commentSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("댓글이 성공적으로 삭제되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist()) // 삭제는 data가 없어야 함
                .andDo(print());

        // 서비스 메서드 호출 검증
        verify(recipientCommentService).deleteComment(
                eq(letterSeq),
                eq(commentSeq),
                eq(requestDto.getCommentPasscode()),
                eq(requestDto.getCaptchaToken())
        );
    }

    /**---

            ### 실패 케이스: 댓글 삭제 - 댓글을 찾을 수 없음

    ```java*/
    @Test
    public void deleteComment_notFound() throws Exception {
        // given
        int letterSeq = 1;
        int nonExistentCommentSeq = 999;
        CommentDeleteRequestDto requestDto = CommentDeleteRequestDto.builder()
                .commentPasscode("pass1234")
                .captchaToken("valid-captcha")
                .build();

        // 서비스 Mocking: RecipientNotFoundException 발생
        doThrow(new RecipientNotFoundException("댓글을 찾을 수 없습니다."))
                .when(recipientCommentService).deleteComment(
                        eq(letterSeq),
                        eq(nonExistentCommentSeq),
                        anyString(), anyString()
                );

        // when
        ResultActions actions = mockMvc.perform(delete("/recipientLetters/{letterSeq}/comments/{commentSeq}", letterSeq, nonExistentCommentSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다."))
                .andDo(print());

        // 서비스 메서드 호출 검증
        verify(recipientCommentService).deleteComment(
                eq(letterSeq),
                eq(nonExistentCommentSeq),
                anyString(), anyString()
        );
    }

    /**---

            ### 실패 케이스: 댓글 삭제 - 권한 없음 (비밀번호 불일치)

    ```java*/
    @Test
    public void deleteComment_unauthorized() throws Exception {
        // given
        int letterSeq = 1;
        int commentSeq = 101;
        CommentDeleteRequestDto requestDto = CommentDeleteRequestDto.builder()
                // 유효성 검사 규칙을 통과하는 비밀번호 (예: 8자 이상, 영문/숫자 포함)
                // 하지만 실제로는 "틀린" 비밀번호
                .commentPasscode("wrongPa55w0rd") // 유효성 검사를 통과하는 값으로 변경
                .captchaToken("valid-captcha")
                .build();

        // 서비스 Mocking: RecipientInvalidPasscodeException 발생으로 변경!
        doThrow(new RecipientInvalidPasscodeException("비밀번호가 일치하지 않습니다.")) // <-- RecipientInvalidPasscodeException으로 변경
                .when(recipientCommentService).deleteComment(
                        eq(letterSeq),
                        eq(commentSeq),
                        eq(requestDto.getCommentPasscode()),
                        anyString()
                );

        // when
        ResultActions actions = mockMvc.perform(delete("/recipientLetters/{letterSeq}/comments/{commentSeq}", letterSeq, commentSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isUnauthorized()) // HttpStatus.UNAUTHORIZED (401)
                .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
                .andDo(print());

        // 서비스 메서드 호출 검증
        verify(recipientCommentService).deleteComment(
                eq(letterSeq),
                eq(commentSeq),
                eq(requestDto.getCommentPasscode()),
                anyString()
        );
    }
}