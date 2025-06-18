package kodanect.common.exception.config;

import kodanect.common.exception.custom.AbstractCustomException;
import kodanect.common.exception.custom.InvalidIntegerConversionException;
import kodanect.common.response.ApiResponse;
import kodanect.domain.recipient.exception.RecipientCommentNotFoundException;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackages = "kodanect.domain.recipient")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RecipientExceptionHandler {

    private static final SecureLogger log = SecureLogger.getLogger(RecipientExceptionHandler.class);
    private final MessageSourceAccessor messageSourceAccessor;  // MessageSourceAccessor 주입

    /**
     * 리소스를 찾을 수 없거나 이미 삭제된 경우의 예외 처리 (404 Not Found)
     * RecipientNotFoundException, CommentNotFoundException
     */
    @ExceptionHandler({RecipientNotFoundException.class, RecipientCommentNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFoundCustomException(AbstractCustomException ex) {
        String message = messageSourceAccessor.getMessage(
                ex.getMessageKey(),
                ex.getArguments(),
                ex.getMessage() // AbstractCustomException의 기본 메시지 사용
        );
        log.warn("Resource Not Found ({}): {}", ex.getStatus(), message);
        return ResponseEntity
                .status(ex.getStatus()) // 예외 객체에서 HTTP 상태 코드 가져오기
                .body(ApiResponse.fail(ex.getStatus(), message));
    }

    /**
     * 비밀번호 불일치와 같은 접근 권한 예외 처리 (403 Forbidden)
     * InvalidPasscodeException
     */
    @ExceptionHandler(RecipientInvalidPasscodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPasscodeException(RecipientInvalidPasscodeException ex) {
        String message = messageSourceAccessor.getMessage(
                ex.getMessageKey(),
                ex.getArguments(),
                ex.getMessage()
        );
        log.warn("Access Forbidden ({}): {}", ex.getStatus(), message);
        return ResponseEntity
                .status(ex.getStatus()) // 예외 객체에서 HTTP 상태 코드 가져오기 (401)
                .body(ApiResponse.fail(ex.getStatus(), message));
    }

    /**
     * 유효하지 않은 요청 데이터 또는 비즈니스 로직 상의 문제 예외 처리 (400 Bad Request)
     * RecipientInvalidDataException
     */
    @ExceptionHandler(RecipientInvalidDataException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidDataException(RecipientInvalidDataException ex) {
        String message = messageSourceAccessor.getMessage(
                ex.getMessageKey(),
                ex.getArguments(),
                ex.getMessage()
        );
        log.warn("Bad Request ({}): {}", ex.getStatus(), message);
        return ResponseEntity
                .status(ex.getStatus()) // 예외 객체에서 HTTP 상태 코드 가져오기
                .body(ApiResponse.fail(ex.getStatus(), message));
    }

    /**
     * Integer 변환 실패 등 숫자 관련 잘못된 입력 처리 (400 Bad Request)
     * InvalidIntegerConversionException
     */
    @ExceptionHandler(InvalidIntegerConversionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidIntegerConversionException(InvalidIntegerConversionException ex) {
        String message = messageSourceAccessor.getMessage(
                ex.getMessageKey(),
                ex.getArguments(),
                ex.getMessage()
        );
        log.warn("Bad Request ({}): Integer 변환 실패 - {}", ex.getStatus(), message);
        return ResponseEntity
                .status(ex.getStatus()) // 예외 객체에서 HTTP 상태 코드 가져오기
                .body(ApiResponse.fail(ex.getStatus(), message));
    }

    /**
     * 유효성 검증 실패 예외(MethodArgumentNotValidException)를 처리하는 핸들러 메서드입니다.
     * Spring MVC에서 @Valid 또는 @Validated를 사용한 @RequestBody 객체의 필드 유효성 검사에서 실패할 경우 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(fieldError.getDefaultMessage()).append(" ");
        }
        String finalMessage = errorMessage.toString().trim();
        log.warn("Validation failed (400): {}", errorMessage.toString().trim());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, finalMessage));
    }

    /**
     * @ModelAttribute 등에서 바인딩 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<String>> handleBindException(BindException ex) {
        StringBuilder errorMessage = new StringBuilder();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(fieldError.getDefaultMessage()).append(" ");
        }
        String finalMessage = errorMessage.toString().trim();
        log.warn("BindException (400): {}", errorMessage.toString().trim());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, finalMessage));
    }

    /**
     * 쿼리 파라미터 타입 불일치 시 발생 (예: int year = "abc")
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getName();
        String value = (ex.getValue() != null) ? ex.getValue().toString() : "null";

        // RequiredType이 null일 가능성을 더 안전하게 처리
        String requiredType = "알 수 없음";
        if (ex.getRequiredType() != null) {
            requiredType = ex.getRequiredType().getSimpleName();
        }

        String errorMsg = String.format(
                "요청 파라미터 [%s]에 잘못된 값이 들어왔습니다. '%s'는 [%s] 타입이어야 합니다.",
                paramName, value, requiredType
        );

        log.warn("타입 불일치 (400): {}", errorMsg);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMsg));
    }

}
