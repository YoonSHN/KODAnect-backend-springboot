package kodanect.domain.logging.service.impl;

import kodanect.domain.logging.buffer.BackendLogBuffer;
import kodanect.domain.logging.buffer.FrontendLogBuffer;
import kodanect.domain.logging.context.MdcContext;
import kodanect.domain.logging.buffer.SystemInfoBuffer;
import kodanect.domain.logging.dto.BackendLogDto;
import kodanect.domain.logging.dto.FrontendLogDto;
import kodanect.domain.logging.dto.SystemInfoDto;
import kodanect.domain.logging.service.ActionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link ActionLogService}의 구현체로,
 * 사용자 액션 로그 데이터를 비동기 방식으로 버퍼에 저장합니다.
 *
 * - 프론트엔드 로그: 즉시 저장
 * - 백엔드 로그: MDC 기반 비동기 저장
 * - 시스템 정보: MDC 기반 비동기 저장
 */
@Service
@RequiredArgsConstructor
public class ActionLogServiceImpl implements ActionLogService {

    private final FrontendLogBuffer frontendLogBuffer;
    private final BackendLogBuffer backendLogBuffer;
    private final SystemInfoBuffer systemInfoBuffer;

    /**
     * 프론트엔드 로그를 세션 ID 기준으로 버퍼에 저장합니다.
     *
     * @param sessionId 사용자 세션 ID
     * @param logs 프론트엔드 로그 목록
     */
    @Override
    public void saveFrontendLog(String sessionId, List<FrontendLogDto> logs) {
        frontendLogBuffer.add(sessionId, logs);
    }

    /**
     * 백엔드 로그를 MDC에서 추출한 정보로 생성하고 버퍼에 저장합니다.
     *
     * @param sessionId 사용자 세션 ID
     */
    @Async("logExecutor")
    public void saveBackendLog(String sessionId) {
        BackendLogDto log = BackendLogDto.builder()
                .httpMethod(MdcContext.getHttpMethod())
                .endpoint(MdcContext.getEndpoint())
                .controller(MdcContext.getController())
                .method(MdcContext.getMethod())
                .parameters(MdcContext.getParameters())
                .timestamp(MdcContext.getTimestamp())
                .build();

        backendLogBuffer.add(sessionId, log);
    }

    /**
     * 시스템 정보를 MDC에서 추출한 정보로 생성하고 버퍼에 저장합니다.
     *
     * @param sessionId 사용자 세션 ID
     */
    @Async("logExecutor")
    public void saveSystemInfo(String sessionId) {
        SystemInfoDto systemInfo = SystemInfoDto.builder()
                .browserName(MdcContext.getBrowserName())
                .browserVersion(MdcContext.getBrowserVersion())
                .operatingSystem(MdcContext.getOperatingSystem())
                .device(MdcContext.getDevice())
                .locale(MdcContext.getLocale())
                .build();

        systemInfoBuffer.add(sessionId, systemInfo);
    }

}
