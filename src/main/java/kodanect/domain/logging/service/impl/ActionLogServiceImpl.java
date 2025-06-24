package kodanect.domain.logging.service.impl;

import kodanect.common.buffer.BackendLogBuffer;
import kodanect.common.buffer.FrontendLogBuffer;
import kodanect.common.constant.MdcContext;
import kodanect.common.buffer.SystemInfoBuffer;
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
 * 사용자 액션 로그 데이터를 세션 기준으로 버퍼에 저장합니다.
 *
 * - 프론트엔드 로그: 동기 저장
 * - 백엔드 로그: AOP에서 주입된 MDC 정보를 기반으로 비동기 저장
 * - 시스템 정보: AOP에서 주입된 MDC 정보를 기반으로 비동기 저장
 *
 * 모든 로그는 MDC의 sessionId를 기준으로 구분됩니다.
 */
@Service
@RequiredArgsConstructor
public class ActionLogServiceImpl implements ActionLogService {

    private final FrontendLogBuffer frontendLogBuffer;
    private final BackendLogBuffer backendLogBuffer;
    private final SystemInfoBuffer systemInfoBuffer;

    /**
     * 프론트엔드 로그를 버퍼에 저장합니다.
     * 세션 ID는 MDC에서 자동으로 추출됩니다.
     *
     * @param logs 프론트엔드 로그 목록
     */
    @Override
    public void saveFrontendLog(List<FrontendLogDto> logs) {
        String sessionId = MdcContext.getSessionId();

        frontendLogBuffer.add(sessionId, logs);
    }

    /**
     * 백엔드 로그를 MDC에서 추출한 정보로 생성하고 버퍼에 저장합니다.
     * 비동기 방식으로 실행됩니다.
     */
    @Async("logExecutor")
    public void saveBackendLog() {
        String sessionId = MdcContext.getSessionId();

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
     * 시스템 정보를 MDC에서 추출한 값으로 생성하고 버퍼에 저장합니다.
     * 비동기 방식으로 실행됩니다.
     */
    @Async("logExecutor")
    public void saveSystemInfo() {
        String sessionId = MdcContext.getSessionId();

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
