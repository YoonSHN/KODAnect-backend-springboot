package kodanect.domain.logging.buffer;

import kodanect.domain.logging.code.CrudCode;
import kodanect.domain.logging.dto.BackendLogDto;
import kodanect.domain.logging.key.UserActionKey;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 백엔드 로그를 세션 및 CRUD 단위로 버퍼링하여 일정 수 이상 쌓였을 때 배출하는 컴포넌트입니다.
 *
 * 내부적으로 {@code ConcurrentHashMap<UserActionKey, Queue<BackendLogDto>>} 구조를 사용하여
 * 동시성 환경에서도 안전하게 로그를 수집하고, 조건에 따라 배출할 수 있습니다.
 */
@Component
public class BackendLogBuffer {

    private final Map<UserActionKey, Queue<BackendLogDto>> buffer = new ConcurrentHashMap<>();

    /**
     * 로그를 버퍼에 추가합니다.
     * 로그는 세션 ID 및 HTTP 메서드를 기반으로 {@link CrudCode}로 변환되어 버퍼에 저장됩니다.
     *
     * @param sessionId 사용자 세션 ID
     * @param log       백엔드 로그
     */
    public void add(String sessionId, BackendLogDto log) {
        if (sessionId == null || sessionId.isBlank() || log == null) {
            return;
        }

        CrudCode crudCode = CrudCode.fromHttpMethod(log.getHttpMethod());
        UserActionKey key = new UserActionKey(sessionId, crudCode);

        buffer.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>()).add(log);
    }

    /**
     * 지정한 CRUD 코드에 해당하는 로그 중, 버퍼에 지정 수(threshold) 이상 쌓인 경우만 배출합니다.
     * 임계값 미만인 경우 해당 로그는 유지됩니다.
     *
     * @param code      배출 대상 CRUD 코드
     * @param threshold 로그 개수 임계값
     * @return 배출된 사용자 액션 키와 해당 로그 목록
     */
    public Map<UserActionKey, List<BackendLogDto>> drainIfThresholdMet(CrudCode code, int threshold) {
        Map<UserActionKey, List<BackendLogDto>> result = new HashMap<>();

        for (Map.Entry<UserActionKey, Queue<BackendLogDto>> entry : buffer.entrySet()) {
            UserActionKey key = entry.getKey();

            if (key.getCrudCode() != code) {
                continue;
            }

            Queue<BackendLogDto> queue = entry.getValue();

            if (queue.size() >= threshold) {
                List<BackendLogDto> drained = new ArrayList<>();

                while (!queue.isEmpty() && drained.size() < threshold) {
                    drained.add(queue.poll());
                }

                result.put(key, drained);
            }
        }

        return result;
    }

    /**
     * 버퍼에 쌓인 모든 로그를 비우고 반환합니다.
     *
     * @return 모든 사용자 액션 키와 해당 로그 목록
     */
    public Map<UserActionKey, List<BackendLogDto>> drainAll() {
        Map<UserActionKey, List<BackendLogDto>> result = new HashMap<>();

        for (Map.Entry<UserActionKey, Queue<BackendLogDto>> entry : buffer.entrySet()) {
            Queue<BackendLogDto> queue = entry.getValue();
            List<BackendLogDto> drained = new ArrayList<>();

            while (!queue.isEmpty()) {
                drained.add(queue.poll());
            }

            if (!drained.isEmpty()) {
                result.put(entry.getKey(), drained);
            }
        }

        buffer.clear();
        return result;
    }

}
