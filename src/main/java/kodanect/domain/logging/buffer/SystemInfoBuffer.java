package kodanect.domain.logging.buffer;

import kodanect.domain.logging.dto.SystemInfoDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 사용자 시스템 정보를 세션 ID를 기준으로 버퍼링하는 컴포넌트입니다.
 *
 * - 버퍼 구조: {@code Map<String, SystemInfoDto>}
 * - 동시성 안전을 위해 {@link ConcurrentHashMap} 사용합니다.
 * - 동일 세션 ID에 대해 한 번만 저장되며, 이후 덮어쓰기 방지합니다.
 * - 백엔드 로그와 함께 시스템 정보를 연결하기 위한 목적입니다.
 */
@Component
public class SystemInfoBuffer {

    private static final String UNKNOWN_SESSION_ID = "Unknown";
    private final Map<String, SystemInfoDto> buffer = new ConcurrentHashMap<>();

    /**
     * 시스템 정보를 버퍼에 추가합니다.
     *
     * 세션 ID 단위로 시스템 정보를 누적 저장합니다.
     * 이미 해당 세션 ID에 정보가 존재할 경우 기존 값을 유지하며 덮어쓰지 않습니다.
     *
     * @param sessionId   사용자 세션 ID
     * @param systemInfo  시스템 정보 객체
     */
    public void add(String sessionId, SystemInfoDto systemInfo) {
        if (UNKNOWN_SESSION_ID.equals(sessionId) || systemInfo == null) {
            return;
        }

        buffer.putIfAbsent(sessionId, systemInfo);
    }

    /**
     * 세션 ID에 해당하는 시스템 정보를 조회합니다.
     *
     * @param sessionId 조회 대상 세션 ID
     * @return 존재할 경우 {@link Optional}로 감싼 시스템 정보, 없을 경우 {@link Optional#empty()}
     */
    public Optional<SystemInfoDto> get(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(buffer.get(sessionId));
    }

    /**
     * 지정된 세션 ID의 시스템 정보를 버퍼에서 제거합니다.
     *
     * @param sessionId 삭제 대상 세션 ID
     */
    public void remove(String sessionId) {
        buffer.remove(sessionId);
    }

}
