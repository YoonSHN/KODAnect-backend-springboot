package kodanect.domain.logging.service;

import kodanect.domain.logging.dto.FrontendLogDto;

import java.util.List;

/**
 * 사용자 액션 로그를 저장하기 위한 서비스 인터페이스입니다.
 *
 * 프론트엔드 로그, 백엔드 로그, 시스템 정보를 각각의 버퍼에 저장하며,
 * 로그 저장 방식에 따라 동기 또는 비동기 처리를 제공합니다.
 */
public interface ActionLogService {

    /**
     * 프론트엔드 로그를 세션 ID 기준으로 버퍼에 저장합니다.
     *
     * @param sessionId 사용자 세션 ID
     * @param logs 프론트엔드 로그 목록
     */
    void saveFrontendLog(String sessionId, List<FrontendLogDto> logs);

    /**
     * 백엔드 로그를 MDC 정보를 기반으로 생성하여 비동기로 저장합니다.
     *
     * @param sessionId 사용자 세션 ID
     */
    void saveBackendLog(String sessionId);

    /**
     * 시스템 정보를 MDC 정보를 기반으로 생성하여 비동기로 저장합니다.
     *
     * @param sessionId 사용자 세션 ID
     */
    void saveSystemInfo(String sessionId);

}
