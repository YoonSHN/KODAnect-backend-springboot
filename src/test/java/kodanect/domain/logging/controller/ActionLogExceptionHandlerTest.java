package kodanect.domain.logging.controller;

import kodanect.common.exception.config.ActionLogExceptionHandler;
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
 * {@link ActionLogExceptionHandler} 및 {@link GlobalExcepHndlr}에 정의된 예외 처리 로직의 단위 테스트입니다.
 *
 * 테스트 대상: {@link ActionLogController}
 * 목적: 비어 있는 로그, JSON 직렬화 실패 시 예외 응답이 올바르게 처리되는지를 검증합니다.
 */
@WebMvcTest(controllers = ActionLogController.class)
@Import({ ActionLogExceptionHandler.class, GlobalExcepHndlr.class })
class ActionLogExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActionLogService actionLogService;

    @MockBean
    private MessageSourceAccessor messageSource;

    /**
     * GIVEN: 프론트엔드 로그 리스트가 비어 있는 상태로 전달된 경우
     * WHEN: 서비스에서 {@link EmptyFrontendLogListException}이 발생하면
     * THEN: 400 Bad Request 상태 코드와 함께
     *       "프론트엔드 로그 리스트는 비어 있을 수 없습니다."라는 메시지가 반환되어야 한다.
     */
    @Test
    void shouldReturn400WhenFrontendLogListIsEmpty() throws Exception {
        doThrow(new EmptyFrontendLogListException())
                .when(actionLogService).saveFrontendLog(List.of());

        when(messageSource.getMessage(anyString(), any(Object[].class), anyString()))
                .thenReturn("프론트엔드 로그 리스트는 비어 있을 수 없습니다.");

        mockMvc.perform(post("/action-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontendLogs\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("프론트엔드 로그 리스트는 비어 있을 수 없습니다."));
    }

    /**
     * GIVEN: 로그 저장 중 JSON 직렬화 과정에서 예외가 발생한 경우
     * WHEN: 서비스에서 {@link ActionLogJsonSerializationException}이 발생하면
     * THEN: 500 Internal Server Error 상태 코드와 함께
     *       "데이터를 JSON으로 변환하는 데 실패했습니다."라는 메시지가 반환되어야 한다.
     */
    @Test
    void shouldReturn500WhenJsonSerializationFails() throws Exception {
        doThrow(new ActionLogJsonSerializationException("프론트엔드 로그 직렬화 중 오류 발생"))
                .when(actionLogService).saveFrontendLog(any());

        when(messageSource.getMessage(anyString(), any(Object[].class), anyString()))
                .thenReturn("JSON 직렬화에 실패했습니다.");

        mockMvc.perform(post("/action-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontendLogs\": [{\"eventType\": \"click\"}]}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("JSON 직렬화에 실패했습니다."));
    }

}
