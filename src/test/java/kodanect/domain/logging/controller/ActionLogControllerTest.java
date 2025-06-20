package kodanect.domain.logging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.domain.logging.dto.FrontendLogDto;
import kodanect.domain.logging.dto.FrontendLogRequestDto;
import kodanect.domain.logging.service.ActionLogService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link ActionLogController}의 REST API 동작을 검증하는 단위 테스트입니다.
 *
 * - HTTP 요청/응답 구조 확인
 * - 서비스 호출 여부 확인
 * - 응답 메시지 및 상태 코드 검증
 */
@RunWith(SpringRunner.class)
@WebMvcTest(ActionLogController.class)
public class ActionLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActionLogService service;

    @MockBean
    private MessageSourceAccessor messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 테스트 실행 전에 {@link MessageSourceAccessor} Mock 객체를 초기화하고,
     * 특정 메시지 키에 대한 반환값을 사전 설정합니다.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(messageSource.getMessage(eq("log.save.success"), any(Object[].class)))
                .thenReturn("로그를 성공적으로 저장했습니다.");
    }

    /**
     * GIVEN: 세션 ID와 프론트엔드 로그 데이터가 주어졌을 때
     * WHEN: /action-logs 엔드포인트에 POST 요청을 보내면
     * THEN: 서비스가 호출되고 200 OK 응답이 반환되어야 한다.
     */
    @Test
    public void collectFrontendLogs_shouldReturnOkAndCallServices() throws Exception {
        FrontendLogDto log = FrontendLogDto.builder().eventType("click").pageUrl("/home").build();
        FrontendLogRequestDto requestDto = new FrontendLogRequestDto(List.of(log));

        when(messageSource.getMessage(eq("log.save.success"), any(Object[].class)))
                .thenReturn("로그를 성공적으로 저장했습니다.");

        mockMvc.perform(post("/action-logs")
                        .cookie(new Cookie("sessionId", "session-abc"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그를 성공적으로 저장했습니다."));

        verify(service).saveFrontendLog(anyList());
        verify(service).saveBackendLog();
        verify(service).saveSystemInfo();
    }

}
