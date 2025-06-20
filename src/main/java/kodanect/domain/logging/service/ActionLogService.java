package kodanect.domain.logging.service;

import kodanect.domain.logging.dto.FrontendLogDto;

import java.util.List;

/**
 * 사용자 액션 로그를 저장하기 위한 서비스 인터페이스입니다.
 *
 * 프론트엔드 로그, 백엔드 로그, 시스템 정보를 각각의 버퍼에 저장하며,
 * 동기 또는 비동기 방식으로 처리됩니다.
 *
 * 세션 ID는 MDC에서 자동으로 추출되며, 클라이언트는 별도로 세션을 전달하지 않아도 됩니다.
 */
public interface ActionLogService {

    /**
     * 프론트엔드 로그를 세션 단위로 버퍼에 저장합니다.
     * 세션 ID는 AOP를 통해 MDC에 저장된 값을 사용합니다.
     *
     * @param logs 프론트엔드 로그 목록
     */
    void saveFrontendLog(List<FrontendLogDto> logs);

    /**
     * 백엔드 로그를 MDC 정보를 기반으로 생성하여 비동기로 저장합니다.
     * 세션 ID 및 기타 메타데이터는 모두 MDC에서 추출됩니다.
     */
    void saveBackendLog();

    /**
     * 사용자 시스템 정보를 MDC 정보를 기반으로 생성하여 비동기로 저장합니다.
     * 세션 ID 및 환경 정보는 MDC에서 추출됩니다.
     */
    void saveSystemInfo();

}
