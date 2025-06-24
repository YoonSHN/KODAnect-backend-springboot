package kodanect.domain.logging.service.impl;

import kodanect.common.buffer.BackendLogBuffer;
import kodanect.common.buffer.FrontendLogBuffer;
import kodanect.common.buffer.SystemInfoBuffer;
import kodanect.domain.logging.dto.FrontendLogDto;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * {@link ActionLogServiceImpl} 클래스의 동작을 검증하는 단위 테스트입니다.
 *
 * 프론트엔드 로그, 백엔드 로그, 시스템 정보 저장 로직의 위임 여부와
 * MDC 기반 데이터 수집 기능을 검증합니다.
 */
public class ActionLogServiceImplTest {

    private FrontendLogBuffer frontendLogBuffer;
    private BackendLogBuffer backendLogBuffer;
    private SystemInfoBuffer systemInfoBuffer;

    private ActionLogServiceImpl service;

    /**
     * 테스트 실행 전 Mock 객체 초기화 및 서비스를 생성합니다.
     */
    @Before
    public void setUp() {
        frontendLogBuffer = mock(FrontendLogBuffer.class);
        backendLogBuffer = mock(BackendLogBuffer.class);
        systemInfoBuffer = mock(SystemInfoBuffer.class);

        service = new ActionLogServiceImpl(frontendLogBuffer, backendLogBuffer, systemInfoBuffer);
    }

    /**
     * GIVEN: 세션 ID와 프론트엔드 로그 리스트가 주어졌을 때
     * WHEN: saveFrontendLog()를 호출하면
     * THEN: FrontendLogBuffer의 add() 메서드가 호출되어야 한다.
     */
    @Test
    public void saveFrontendLog_shouldDelegateToBuffer() {
        String sessionId = "session-123";

        MDC.put("sessionId", sessionId);

        List<FrontendLogDto> logs = List.of(
                FrontendLogDto.builder().eventType("clickButton").build()
        );

        service.saveFrontendLog(logs);

        verify(frontendLogBuffer).add(sessionId, logs);
    }

    /**
     * GIVEN: MDC에 백엔드 로그 관련 정보가 채워져 있을 때
     * WHEN: saveBackendLog()를 호출하면
     * THEN: BackendLogBuffer에 해당 로그가 저장되어야 한다.
     */
    @Test
    public void saveBackendLog_shouldExtractFromMdcAndStore() {
        String sessionId = "session-456";

        MDC.put("sessionId", sessionId);
        MDC.put("httpMethod", "POST");
        MDC.put("endpoint", "/api/test");
        MDC.put("controller", "TestController");
        MDC.put("method", "testMethod");
        MDC.put("parameters", "{\"id\":1}");
        MDC.put("timestamp", "2025-06-16T00:00:00Z");

        service.saveBackendLog();

        verify(backendLogBuffer).add(eq(sessionId), argThat(log ->
                "POST".equals(log.getHttpMethod()) &&
                        "/api/test".equals(log.getEndpoint()) &&
                        "TestController".equals(log.getController()) &&
                        "testMethod".equals(log.getMethod()) &&
                        "{\"id\":1}".equals(log.getParameters()) &&
                        "2025-06-16T00:00:00Z".equals(log.getTimestamp())
        ));
    }

    /**
     * GIVEN: MDC에 시스템 정보가 채워져 있을 때
     * WHEN: saveSystemInfo()를 호출하면
     * THEN: SystemInfoBuffer에 해당 정보가 저장되어야 한다
     */
    @Test
    public void saveSystemInfo_shouldExtractFromMdcAndStore() {
        String sessionId = "session-789";

        MDC.put("sessionId", sessionId);
        MDC.put("browserName", "Chrome");
        MDC.put("browserVersion", "114.0");
        MDC.put("operatingSystem", "Mac OS");
        MDC.put("device", "Computer");
        MDC.put("locale", "ko-KR");

        service.saveSystemInfo();

        verify(systemInfoBuffer).add(eq(sessionId), argThat(info ->
                "Chrome".equals(info.getBrowserName()) &&
                        "114.0".equals(info.getBrowserVersion()) &&
                        "Mac OS".equals(info.getOperatingSystem()) &&
                        "Computer".equals(info.getDevice()) &&
                        "ko-KR".equals(info.getLocale())
        ));
    }

}
