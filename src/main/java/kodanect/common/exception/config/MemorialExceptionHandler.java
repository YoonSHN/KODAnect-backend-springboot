package kodanect.common.exception.config;

import kodanect.common.exception.custom.AbstractCustomException;
import kodanect.common.response.ApiResponse;
import org.springframework.core.annotation.Order;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 *
 * 기증자 추모관 관련 예외를 처리하는 클래스
 *
 * */
@RestControllerAdvice(basePackages = "kodanect.domain.remembrance")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MemorialExceptionHandler {

    private final MessageSourceAccessor messageSourceAccessor;

    public MemorialExceptionHandler(MessageSourceAccessor messageSourceAccessor) {
        this.messageSourceAccessor = messageSourceAccessor;
    }

    /**
     *
     *AbstractCustomException 예외를 상속받은 기증자 추모관의 예외들을 처리하는 메서드
     *
     * */
    @ExceptionHandler(AbstractCustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(AbstractCustomException ex) {
        String message = messageSourceAccessor.getMessage(
                ex.getMessageKey(),
                ex.getArguments(),
                "잘못된 요청입니다."
        );
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.fail(ex.getStatus(), message));
    }

}
