package kodanect.common.exception.config;

import kodanect.common.exception.custom.AbstractCustomException;
import kodanect.common.response.ApiResponse;
import kodanect.domain.logging.exception.ActionLogJsonSerializationException;
import kodanect.domain.logging.exception.EmptyFrontendLogListException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static kodanect.common.exception.config.MessageKeys.ACTION_LOG_JSON_SERIALIZATION_FAIL;
import static kodanect.common.exception.config.MessageKeys.FRONTEND_LOG_LIST_EMPTY;

/**
 * 액션 로그 관련 예외를 처리하는 핸들러 클래스
 * 대상 패키지: kodanect.domain.logging.*
 */
@RestControllerAdvice(basePackages = "kodanect.domain.logging")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ActionLogExceptionHandler {

    private static final SecureLogger log = SecureLogger.getLogger(ActionLogExceptionHandler.class);

    private final MessageSourceAccessor messageSourceAccessor;

    /**
     * JSON 직렬화 실패 시 예외 처리
     * - 로그 데이터를 문자열로 변환하지 못했을 경우
     */
    @ExceptionHandler(ActionLogJsonSerializationException.class)
    public ResponseEntity<ApiResponse<Void>> handleJsonSerializationFailure(ActionLogJsonSerializationException ex) {
        return handle(ex, ACTION_LOG_JSON_SERIALIZATION_FAIL, "JSON 직렬화에 실패했습니다.");
    }

    /**
     * 프론트엔드 로그 리스트가 비어 있을 경우 예외 처리
     * - 로그 요청 DTO에 데이터가 없는 경우
     */
    @ExceptionHandler(EmptyFrontendLogListException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmptyFrontendLogList(EmptyFrontendLogListException ex) {
        return handle(ex, FRONTEND_LOG_LIST_EMPTY, "프론트엔드 로그 리스트는 비어 있을 수 없습니다.");
    }

    /**
     * 공통 처리 메서드
     */
    private ResponseEntity<ApiResponse<Void>> handle(AbstractCustomException ex, String messageKey, String defaultMessage) {
        String resolvedMessage = messageSourceAccessor.getMessage(
                messageKey,
                ex.getArguments(),
                defaultMessage
        );

        log.warn("[액션 로그 예외] {} - {}: {}", ex.getClass().getSimpleName(), messageKey, resolvedMessage, ex);

        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.fail(ex.getStatus(), resolvedMessage));
    }

}
