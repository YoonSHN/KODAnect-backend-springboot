package kodanect.common.exception.config;

import kodanect.common.exception.custom.AbstractCustomException;
import kodanect.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice("kodanect.domain.heaven")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class HeavenExceptionHandler {

    private final MessageSourceAccessor messageSourceAccessor;

    @ExceptionHandler(AbstractCustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(AbstractCustomException e) {
        String message = messageSourceAccessor.getMessage(e.getMessageKey(), e.getArguments(), "하늘나라 편지 에러 발생");

        return ResponseEntity.status(e.getStatus()).body(ApiResponse.fail(e.getStatus(), message));
    }
}
