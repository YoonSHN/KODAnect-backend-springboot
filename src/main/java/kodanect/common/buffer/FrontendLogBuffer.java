package kodanect.common.buffer;

import kodanect.common.constant.CrudCode;
import kodanect.domain.logging.dto.FrontendLogDto;
import kodanect.common.constant.UserActionKey;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 프론트엔드 로그를 세션 ID 및 CRUD 단위로 버퍼링하는 컴포넌트입니다.
 *
 * - 버퍼 구조: {@code Map<UserActionKey, Queue<FrontendLogDto>>}
 * - 이벤트 타입을 기반으로 {@link CrudCode}를 분류하여 그룹핑합니다.
 * - 동시성 안전을 위해 {@link ConcurrentHashMap}과 {@link ConcurrentLinkedQueue} 사용합니다.
 * - 특정 임계치(threshold)를 넘은 경우 또는 전체 배출 시 로그를 추출합니다.
 */
@Component
public class FrontendLogBuffer {

    private static final String UNKNOWN_SESSION_ID = "Unknown";
    private final Map<UserActionKey, Queue<FrontendLogDto>> buffer = new ConcurrentHashMap<>();

    /**
     * 프론트엔드 로그를 버퍼에 추가합니다.
     *
     * 세션 ID와 이벤트 타입을 기반으로 {@link CrudCode}를 추출하여,
     * {@link UserActionKey} 단위로 로그를 누적 저장합니다.
     *
     * @param sessionId 사용자 세션 ID
     * @param logs      프론트엔드 로그 리스트
     */
    public void add(String sessionId, List<FrontendLogDto> logs) {
        if (UNKNOWN_SESSION_ID.equals(sessionId) || logs == null || logs.isEmpty()) {
            return;
        }

        for (FrontendLogDto log : logs) {
            CrudCode crudCode = CrudCode.fromEventType(log.getEventType());
            UserActionKey key = new UserActionKey(sessionId, crudCode);

            buffer.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>()).add(log);
        }
    }

    /**
     * 주어진 CRUD 코드에 해당하는 키 중, 로그 개수가 임계값 이상인 것만 추출합니다.
     *
     * 조건을 만족하는 항목만 추출하며, 그렇지 않은 항목은 유지됩니다.
     *
     * @param code      필터링할 CRUD 코드
     * @param threshold 로그 배출 임계값
     * @return 배출된 사용자 액션 키 및 해당 로그 목록
     */
    public Map<UserActionKey, List<FrontendLogDto>> drainIfThresholdMet(CrudCode code, int threshold) {
        Map<UserActionKey, List<FrontendLogDto>> result = new HashMap<>();

        for (Map.Entry<UserActionKey, Queue<FrontendLogDto>> entry : buffer.entrySet()) {
            UserActionKey key = entry.getKey();

            if (key.getCrudCode() != code) {
                continue;
            }

            Queue<FrontendLogDto> queue = entry.getValue();

            if (queue.size() >= threshold) {
                List<FrontendLogDto> drained = new ArrayList<>();

                while (!queue.isEmpty() && drained.size() < threshold) {
                    drained.add(queue.poll());
                }

                result.put(key, drained);
            }
        }

        return result;
    }

    /**
     * 버퍼에 저장된 모든 로그를 배출하고 버퍼를 초기화합니다.
     *
     * @return 모든 사용자 액션 키 및 해당 로그 목록
     */
    public Map<UserActionKey, List<FrontendLogDto>> drainAll() {
        Map<UserActionKey, List<FrontendLogDto>> result = new HashMap<>();

        for (Map.Entry<UserActionKey, Queue<FrontendLogDto>> entry : buffer.entrySet()) {
            Queue<FrontendLogDto> queue = entry.getValue();
            List<FrontendLogDto> drained = new ArrayList<>();

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
