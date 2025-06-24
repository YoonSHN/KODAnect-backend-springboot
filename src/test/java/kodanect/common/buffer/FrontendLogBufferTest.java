package kodanect.common.buffer;

import kodanect.common.constant.CrudCode;
import kodanect.domain.logging.dto.FrontendLogDto;
import kodanect.common.constant.UserActionKey;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FrontendLogBuffer} 클래스의 동작을 검증하는 단위 테스트입니다.
 *
 * 로그 추가, 임계값 조건에 따른 배출, 전체 배출, 유효하지 않은 입력 처리 등을 테스트합니다.
 */
public class FrontendLogBufferTest {

    private FrontendLogBuffer buffer;

    /**
     * 테스트 실행 전 FrontendLogBuffer 인스턴스를 초기화합니다.
     */
    @Before
    public void setUp() {
        buffer = new FrontendLogBuffer();
    }

    /**
     * Given: 하나의 세션 ID와 동일한 eventType의 로그 3개가 주어졌을 때
     * When: 버퍼에 add()로 추가하고 drainIfThresholdMet()를 호출하면
     * Then: 해당 CRUD에 대해 로그 3개가 올바르게 배출되어야 한다.
     */
    @Test
    public void drainIfThresholdMet_shouldDrainLogsByCrudAndThreshold() {
        String sessionId = "session-1";

        List<FrontendLogDto> logs = Arrays.asList(
                createLog("clickButton"),
                createLog("clickButton"),
                createLog("clickButton")
        );

        buffer.add(sessionId, logs);

        Map<UserActionKey, List<FrontendLogDto>> result = buffer.drainIfThresholdMet(CrudCode.R, 3);

        assertThat(result).hasSize(1);
        Map.Entry<UserActionKey, List<FrontendLogDto>> entry = result.entrySet().iterator().next();
        assertThat(entry.getKey().getSessionId()).isEqualTo(sessionId);
        assertThat(entry.getKey().getCrudCode()).isEqualTo(CrudCode.R);
        assertThat(entry.getValue()).hasSize(3);
    }

    /**
     * Given: 두 개의 세션 ID에 각각 다른 이벤트 타입 로그가 추가되었을 때
     * When: drainAll()을 호출하면
     * Then: 모든 세션의 로그가 모두 비워지고 반환되어야 한다.
     */
    @Test
    public void drainAll_shouldReturnAllLogsAndClearBuffer() {
        buffer.add("session-A", Collections.singletonList(createLog("clickLink")));
        buffer.add("session-B", Collections.singletonList(createLog("createPost")));

        Map<UserActionKey, List<FrontendLogDto>> allLogs = buffer.drainAll();

        assertThat(allLogs).hasSize(2);
        assertThat(allLogs.values()).allSatisfy(logList -> assertThat(logList).hasSize(1));

        assertThat(buffer.drainAll()).isEmpty();
    }

    /**
     * Given: sessionId가 "Unknown"이거나 로그 리스트가 null 또는 비어 있는 경우
     * When: add()를 호출하면
     * Then: 로그는 버퍼에 추가되지 않아야 한다.
     */
    @Test
    public void add_shouldIgnoreInvalidInput() {
        buffer.add("Unknown", Collections.singletonList(createLog("clickButton")));
        buffer.add("valid-session", Collections.emptyList());
        buffer.add("valid-session", null);

        Map<UserActionKey, List<FrontendLogDto>> result = buffer.drainAll();
        assertThat(result).isEmpty();
    }

    private FrontendLogDto createLog(String eventType) {
        return FrontendLogDto.builder()
                .eventType(eventType)
                .elementId("btn-1")
                .pageUrl("/test")
                .referrerUrl("/prev")
                .timestamp("2024-01-01T00:00:00Z")
                .build();
    }

}
