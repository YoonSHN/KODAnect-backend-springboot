package kodanect.domain.logging.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.domain.logging.dto.FrontendLogDto;
import kodanect.domain.logging.dto.FrontendLogRequestDto;
import kodanect.domain.logging.entity.ActionLog;
import kodanect.domain.logging.flusher.ActionLogFlusher;
import kodanect.domain.logging.repository.ActionLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link ActionLogController}의 전체 흐름을 검증하는 통합 테스트입니다.
 *
 * - 프론트엔드 로그가 정상적으로 수신되고
 * - 백엔드 AOP 로직과 버퍼 저장이 작동한 후
 * - {@link ActionLogFlusher}를 통해 DB에 최종 저장되는 과정을 확인합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ActionLogControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ActionLogRepository actionLogRepository;

    @Autowired
    ActionLogFlusher flusher;

    /**
     * GIVEN: 세션 ID와 프론트엔드 로그 3건이 준비되어 있고
     * WHEN: 클라이언트가 /action-logs 엔드포인트에 POST 요청을 보내고,
     *       로그 수집 후 flushAll()이 호출되면
     * THEN: ActionLog가 DB에 저장되며, logText에 "click" 문자열이 포함되어야 한다.
     */

    /**
     * GIVEN: 세션 ID와 프론트엔드 로그 3건이 준비되어 있고
     * WHEN: 클라이언트가 /action-logs 엔드포인트에 POST 요청을 보내고,
     *       로그 수집 후 flushAll()이 호출되면
     * THEN: 저장된 모든 ActionLog의 logText를 합쳤을 때,
     *       각각의 프론트엔드 이벤트 타입이 포함되어 있어야 한다.
     */
    @Test
    void fullFlow_shouldFlushAllLogsToDatabase() throws Exception {
        String sessionId = "session-full-123";

        FrontendLogDto log1 = buildFrontendLogDto("clickButton", "btn-1", "/page-1", "/referrer-1", "2025-06-16T23:00:00");
        FrontendLogDto log2 = buildFrontendLogDto("clickMenu", "menu-1", "/page-2", "/referrer-2", "2025-06-16T23:00:10");
        FrontendLogDto log3 = buildFrontendLogDto("clickTab", "tab-1", "/page-3", "/referrer-3", "2025-06-16T23:00:20");

        FrontendLogRequestDto request = new FrontendLogRequestDto(List.of(log1, log2, log3));

        mockMvc.perform(post("/action-logs")
                        .header("X-Session-Id", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        flusher.flushAll();

        List<ActionLog> savedLogs = actionLogRepository.findAll();
        String mergedLogText = savedLogs.stream()
                .map(ActionLog::getLogText)
                .collect(Collectors.joining());

        assertThat(mergedLogText)
                .contains("clickButton")
                .contains("clickMenu")
                .contains("clickTab");
    }

    /**
     * 테스트용 프론트엔드 로그 DTO를 생성합니다.
     *
     * @param eventType 이벤트 유형
     * @param elementId 요소 ID
     * @param pageUrl 페이지 URL
     * @param referrerUrl 이전 페이지 URL
     * @param timestamp 이벤트 발생 시각
     * @return 생성된 {@link FrontendLogDto}
     */
    private FrontendLogDto buildFrontendLogDto(
            String eventType,
            String elementId,
            String pageUrl,
            String referrerUrl,
            String timestamp
    ) {
        return FrontendLogDto.builder()
                .eventType(eventType)
                .elementId(elementId)
                .pageUrl(pageUrl)
                .referrerUrl(referrerUrl)
                .timestamp(timestamp)
                .build();
    }

}
