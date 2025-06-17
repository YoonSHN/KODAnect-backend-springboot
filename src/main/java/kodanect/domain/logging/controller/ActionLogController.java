package kodanect.domain.logging.controller;

import kodanect.common.response.ApiResponse;
import kodanect.domain.logging.dto.FrontendLogRequestDto;
import kodanect.domain.logging.service.ActionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 액션 로그 수집을 담당하는 REST 컨트롤러입니다.
 *
 * 클라이언트에서 전달한 프론트엔드 로그를 수신하고,
 * 내부적으로 백엔드 로그 및 시스템 정보를 함께 저장 처리합니다.
 *
 * - 세션 단위로 로그를 분리하여 저장합니다.
 * - {@code X-Session-Id} 헤더를 기반으로 식별합니다.
 */
@RestController
@RequiredArgsConstructor
public class ActionLogController {

    private final MessageSourceAccessor messageSource;
    private final ActionLogService service;

    /**
     * 프론트엔드 로그 수집 엔드포인트입니다.
     *
     * 클라이언트에서 수집한 이벤트 로그를 전달받아 저장하며,
     * 같은 세션의 백엔드 로그 및 시스템 정보도 함께 기록됩니다.
     *
     * @param sessionId {@code X-Session-Id} 요청 헤더 값 (세션 식별자)
     * @param requestDto 프론트엔드 로그 요청 DTO
     * @return 처리 결과를 담은 {@link ApiResponse} 응답 객체
     */
    @PostMapping("/action-logs")
    public ResponseEntity<ApiResponse<Void>> collectFrontendLogs(
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestBody FrontendLogRequestDto requestDto
    ) {
        String message = messageSource.getMessage("log.save.success", new Object[]{});

        service.saveFrontendLog(sessionId, requestDto.getFrontendLogs());
        service.saveBackendLog(sessionId);
        service.saveSystemInfo(sessionId);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message));
    }

}
