package kodanect.domain.logging.flusher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.common.buffer.BackendLogBuffer;
import kodanect.common.buffer.FrontendLogBuffer;
import kodanect.common.buffer.SystemInfoBuffer;
import kodanect.common.constant.CrudCode;
import kodanect.domain.logging.context.ActionLogContext;
import kodanect.common.constant.MdcContext;
import kodanect.domain.logging.dto.BackendLogDto;
import kodanect.domain.logging.dto.FrontendLogDto;
import kodanect.domain.logging.dto.SystemInfoDto;
import kodanect.domain.logging.entity.ActionLog;
import kodanect.domain.logging.exception.ActionLogJsonSerializationException;
import kodanect.common.constant.UserActionKey;
import kodanect.domain.logging.repository.ActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 사용자 로그를 버퍼로부터 수집하고 {@link ActionLog} 엔티티로 변환하여 저장하는 컴포넌트입니다.
 *
 * 프론트엔드/백엔드 로그, 시스템 정보는 세션 ID 및 CRUD 코드 단위로 분리되어 저장되며,
 * 특정 조건(CRUD별 임계값 초과 또는 전체 배출 등)에 따라 집계 및 저장이 수행됩니다.
 */
@Service
@RequiredArgsConstructor
public class ActionLogFlusher {

    private final FrontendLogBuffer frontendBuffer;
    private final BackendLogBuffer backendBuffer;
    private final SystemInfoBuffer systemInfoBuffer;
    private final ActionLogRepository actionLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * 지정된 CRUD 코드에 해당하는 로그가 임계값 이상 쌓인 경우에만 저장합니다.
     *
     * @param crudCode 대상 CRUD 코드
     * @param threshold 배출 임계값 (로그 건수)
     */
    public void flushByCrudCode(CrudCode crudCode, int threshold) {
        Map<UserActionKey, List<FrontendLogDto>> feMap = frontendBuffer.drainIfThresholdMet(crudCode, threshold);
        Map<UserActionKey, List<BackendLogDto>> beMap = backendBuffer.drainIfThresholdMet(crudCode, threshold);

        Set<UserActionKey> targetKeys = new HashSet<>();
        targetKeys.addAll(feMap.keySet());
        targetKeys.addAll(beMap.keySet());

        flushByKeys(targetKeys, feMap, beMap, crudCode);
    }

    /**
     * 모든 버퍼에 있는 로그를 비우고 저장합니다.
     */
    public void flushAll() {
        Map<UserActionKey, List<FrontendLogDto>> feMap = frontendBuffer.drainAll();
        Map<UserActionKey, List<BackendLogDto>> beMap = backendBuffer.drainAll();

        Set<UserActionKey> allKeys = new HashSet<>();
        allKeys.addAll(feMap.keySet());
        allKeys.addAll(beMap.keySet());

        flushByKeys(allKeys, feMap, beMap, null);
    }

    /**
     * 주어진 키 집합을 기반으로 로그를 집계하고, 엔티티로 변환 후 저장합니다.
     *
     * @param keys 로그 키 집합 (세션 ID + CRUD 기준)
     * @param feMap 프론트엔드 로그 맵
     * @param beMap 백엔드 로그 맵
     * @param forcedCrudCode 강제로 설정할 CRUD 코드 (null인 경우 key 기준 사용)
     */
    private void flushByKeys(Set<UserActionKey> keys,
                             Map<UserActionKey, List<FrontendLogDto>> feMap,
                             Map<UserActionKey, List<BackendLogDto>> beMap,
                             CrudCode forcedCrudCode) {

        List<ActionLog> logsToSave = new ArrayList<>();

        for (UserActionKey key : keys) {
            String sessionId = key.getSessionId();
            CrudCode crudCode = (forcedCrudCode != null) ? forcedCrudCode : key.getCrudCode();

            List<FrontendLogDto> feList = feMap.getOrDefault(key, List.of());
            List<BackendLogDto> beList = beMap.getOrDefault(key, List.of());
            SystemInfoDto systemInfo = systemInfoBuffer.get(sessionId).orElse(null);

            ActionLogContext context = ActionLogContext.builder()
                    .sessionId(sessionId)
                    .frontendLogs(feList)
                    .backendLogs(beList)
                    .systemInfo(systemInfo)
                    .build();

            try {
                String logText = objectMapper.writeValueAsString(context);

                String urlName = extractUrlName(feList, beList);
                String ipAddr = MdcContext.getIpAddress();

                logsToSave.add(ActionLog.builder()
                        .urlName(urlName)
                        .crudCode(crudCode.name())
                        .ipAddr(ipAddr)
                        .logText(logText)
                        .build());

            } catch (JsonProcessingException e) {
                throw new ActionLogJsonSerializationException("로그 엔티티 직렬화");
            }

            systemInfoBuffer.remove(sessionId);
        }

        if (!logsToSave.isEmpty()) {
            actionLogRepository.saveAll(logsToSave);
        }
    }

    /**
     * 로그에서 대표 URL을 추출합니다.
     *
     * 프론트엔드 로그가 존재하면 해당 페이지 URL을,
     * 없을 경우 백엔드 로그의 endpoint를 반환합니다.
     *
     * @param frontendLogs 프론트 로그 목록
     * @param backendLogs 백엔드 로그 목록
     * @return URL 문자열 (없으면 "unknown")
     */
    private String extractUrlName(List<FrontendLogDto> frontendLogs, List<BackendLogDto> backendLogs) {
        if (!frontendLogs.isEmpty()) {
            return frontendLogs.get(0).getPageUrl();
        }
        if (!backendLogs.isEmpty()) {
            return backendLogs.get(0).getEndpoint();
        }
        return "unknown";
    }

}
