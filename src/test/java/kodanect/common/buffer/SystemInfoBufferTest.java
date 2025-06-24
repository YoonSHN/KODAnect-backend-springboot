package kodanect.common.buffer;

import kodanect.domain.logging.dto.SystemInfoDto;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SystemInfoBuffer} 클래스의 동작을 검증하는 단위 테스트입니다.
 *
 * 유효한 시스템 정보 추가, 중복 저장 방지, 조회, 삭제, 잘못된 입력 처리 등을 테스트합니다.
 */
public class SystemInfoBufferTest {

    private SystemInfoBuffer buffer;

    /**
     * 테스트 실행 전 SystemInfoBuffer 인스턴스를 초기화합니다.
     */
    @Before
    public void setUp() {
        buffer = new SystemInfoBuffer();
    }

    /**
     * GIVEN: 유효한 세션 ID와 시스템 정보가 주어졌을 때
     * WHEN: add()를 호출하면
     * THEN: get()으로 동일 정보가 조회되어야 한다.
     */
    @Test
    public void add_and_get_shouldStoreAndRetrieveSystemInfo() {
        String sessionId = "session-1";
        SystemInfoDto info = createSystemInfo();

        buffer.add(sessionId, info);
        Optional<SystemInfoDto> result = buffer.get(sessionId);

        assertThat(result).isPresent();
        assertThat(result.get().getBrowserName()).isEqualTo("Chrome");
    }

    /**
     * GIVEN: 동일한 세션 ID로 두 번 add() 호출 시
     * WHEN: 두 번째에 다른 정보를 전달하면
     * THEN: 최초 정보가 유지되어야 한다.
     */
    @Test
    public void add_shouldNotOverrideExistingValue() {
        String sessionId = "session-2";
        SystemInfoDto original = createSystemInfo();
        SystemInfoDto updated = SystemInfoDto.builder().browserName("Firefox").build();

        buffer.add(sessionId, original);
        buffer.add(sessionId, updated);

        Optional<SystemInfoDto> result = buffer.get(sessionId);

        assertThat(result).isPresent();
        assertThat(result.get().getBrowserName()).isEqualTo("Chrome");
    }

    /**
     * GIVEN: 유효한 시스템 정보가 버퍼에 저장되어 있을 때
     * WHEN: remove()를 호출하면
     * THEN: 해당 세션 ID의 정보는 더 이상 조회되지 않아야 한다.
     */
    @Test
    public void remove_shouldDeleteSystemInfo() {
        String sessionId = "session-3";
        buffer.add(sessionId, createSystemInfo());

        buffer.remove(sessionId);
        Optional<SystemInfoDto> result = buffer.get(sessionId);

        assertThat(result).isEmpty();
    }

    /**
     * Given: sessionId가 "Unknown"이거나 시스템 정보가 null인 경우
     * WHEN: add()를 호출하면
     * THEN: 버퍼에 아무것도 저장되지 않아야 한다.
     */
    @Test
    public void add_shouldIgnoreInvalidInput() {
        buffer.add("Unknown", createSystemInfo());
        buffer.add("session-4", null);

        assertThat(buffer.get("Unknown")).isEmpty();
        assertThat(buffer.get("session-4")).isEmpty();
    }

    private SystemInfoDto createSystemInfo() {
        return SystemInfoDto.builder()
                .browserName("Chrome")
                .browserVersion("120")
                .operatingSystem("Windows")
                .device("Desktop")
                .locale("ko-KR")
                .build();
    }

}
