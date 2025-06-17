package kodanect.domain.logging.context;

import kodanect.domain.logging.dto.BackendLogDto;
import kodanect.domain.logging.dto.FrontendLogDto;
import kodanect.domain.logging.dto.SystemInfoDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 하나의 사용자 요청에서 발생한 다양한 로그 정보를 통합하여 전달하는 컨텍스트 클래스입니다.
 *
 * 프론트엔드 로그, 백엔드 로그, 시스템 정보, 세션 ID 등을 포함하며
 * 로그 저장 처리 시 이 객체 하나로 모든 데이터를 주고받을 수 있습니다.
 */
@Getter
@Builder
public class ActionLogContext {

    /**
     * 사용자 세션 ID
     */
    private String sessionId;

    /**
     * 프론트엔드 로그 목록
     */
    private List<FrontendLogDto> frontendLogs;

    /**
     * 백엔드 로그 목록
     */
    private List<BackendLogDto> backendLogs;

    /**
     * 클라이언트의 시스템 환경 정보
     */
    private SystemInfoDto systemInfo;

}
