package kodanect.domain.logging.controller;

import kodanect.common.exception.config.GlobalExcepHndlr;
import kodanect.domain.logging.exception.ActionLogJsonSerializationException;
import kodanect.domain.logging.exception.EmptyFrontendLogListException;
import kodanect.domain.logging.service.ActionLogService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link GlobalExcepHndlr}에서 정의된 예외 처리 로직에 대한 단위 테스트입니다.
 *
 * 테스트 대상: {@link ActionLogController}
 * 목적: 필수 값 누락, 비어있는 로그, JSON 직렬화 실패 시 예외 응답을 올바르게 처리하는지 검증
 */
@WebMvcTest(controllers = ActionLogController.class)
@Import(GlobalExcepHndlr.class)
class ActionLogExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActionLogService actionLogService;

    @MockBean
    private MessageSourceAccessor messageSource;

    /**
     * GIVEN: 요청 헤더에 필수 세션 ID가 누락되었을 때
     * WHEN: /action-logs 엔드포인트에 POST 요청을 보내면
     * THEN: 400 Bad Request와 "잘못된 요청입니다." 메시지가 응답되어야 한다.
     */
    @Test
    void shouldReturn400WhenSessionIdIsMissing() throws Exception {
        mockMvc.perform(post("/action-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontendLogs\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
    }

    /**
     * GIVEN: 프론트엔드 로그 리스트가 비어 있는 상태로 전달되면
     * WHEN: 서비스에서 {@link EmptyFrontendLogListException}을 던질 때
     * THEN: 400 Bad Request와 "잘못된 요청입니다." 메시지가 응답되어야 한다.
     */
    @Test
    void shouldReturn400WhenFrontendLogListIsEmpty() throws Exception {
        String sessionId = "session-empty-001";

        doThrow(new EmptyFrontendLogListException())
                .when(actionLogService).saveFrontendLog(eq(sessionId), eq(List.of()));

        mockMvc.perform(post("/action-logs")
                        .header("X-Session-Id", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontendLogs\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
    }

    /**
     * GIVEN: 로그 저장 시 JSON 직렬화 중 예외가 발생하면
     * WHEN: 서비스에서 {@link ActionLogJsonSerializationException}이 발생할 때
     * THEN: 500 Internal Server Error와 "로그 데이터를 저장하는 도중 오류가 발생했습니다." 메시지가 응답되어야 한다.
     */
    @Test
    void shouldReturn500WhenJsonSerializationFails() throws Exception {
        String sessionId = "session-error-001";

        doThrow(new ActionLogJsonSerializationException())
                .when(actionLogService).saveFrontendLog(eq(sessionId), any());

        when(messageSource.getMessage("log.save.success", new Object[]{})).thenReturn("성공 메시지");

        mockMvc.perform(post("/action-logs")
                        .header("X-Session-Id", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontendLogs\": [{\"eventType\": \"click\"}]}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("로그 데이터를 저장하는 도중 오류가 발생했습니다."));
    }

}