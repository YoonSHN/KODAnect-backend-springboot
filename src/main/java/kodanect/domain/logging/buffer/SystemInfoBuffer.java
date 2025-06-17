package kodanect.domain.logging.buffer;

import kodanect.domain.logging.dto.SystemInfoDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 세션 ID를 기준으로 사용자 시스템 정보를 저장하는 버퍼 컴포넌트입니다.
 *
 * 프론트엔드 로그와 백엔드 로그가 수집되는 동안 사용자의 시스템 정보 ({@link SystemInfoDto})를 세션 단위로 임시 저장합니다.
 * 동시성 환경에서도 안전하게 접근할 수 있도록 {@link ConcurrentHashMap}을 사용합니다.
 * </p>
 */
@Component
public class SystemInfoBuffer {

    private final Map<String, SystemInfoDto> buffer = new ConcurrentHashMap<>();

    /**
     * 시스템 정보를 세션 ID 기준으로 저장합니다.
     * 이미 해당 세션 ID에 정보가 존재할 경우 기존 값을 유지합니다.
     *
     * @param sessionId 세션 ID
     * @param systemInfo 사용자 시스템 정보
     */
    public void add(String sessionId, SystemInfoDto systemInfo) {
        if(sessionId == null || sessionId.isBlank() || systemInfo == null) {
            return;
        }

        buffer.putIfAbsent(sessionId, systemInfo);
    }

    /**
     * 세션 ID에 해당하는 시스템 정보를 조회합니다.
     *
     * @param sessionId 조회 대상 세션 ID
     * @return 존재할 경우 시스템 정보, 없으면 Optional.empty()
     */
    public Optional<SystemInfoDto> get(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(buffer.get(sessionId));
    }

    /**
     * 버퍼에서 해당 세션 ID의 정보를 제거합니다.
     *
     * @param sessionId 삭제할 세션 ID
     */
    public void remove(String sessionId) {
        buffer.remove(sessionId);
    }

}
