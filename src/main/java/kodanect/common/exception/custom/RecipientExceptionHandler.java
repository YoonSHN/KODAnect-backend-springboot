package kodanect.common.exception.custom;

import kodanect.common.response.ApiResponse;
import kodanect.domain.recipient.exception.RecipientCommentNotFoundException;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice(basePackages = "kodanect.domain.recipient")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RecipientExceptionHandler {
    public RecipientExceptionHandler() {
        log.info(">>> RecipientExceptionHandler loaded");
    }

    /**
     * 리소스를 찾을 수 없거나 이미 삭제된 경우의 예외 처리 (404 Not Found)
     * RecipientNotFoundException, CommentNotFoundException
     */
    @ExceptionHandler({RecipientNotFoundException.class, RecipientCommentNotFoundException.class})
    public ResponseEntity<ApiResponse<String>> handleNotFoundCustomException(RuntimeException ex) {
        log.warn("Resource Not Found (404 Not Found): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /**
     * 비밀번호 불일치와 같은 접근 권한 예외 처리 (403 Forbidden)
     * InvalidPasscodeException
     */
    @ExceptionHandler(RecipientInvalidPasscodeException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidPasscodeException(RecipientInvalidPasscodeException ex) {
        log.warn("Access Forbidden (403 Forbidden): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 403 Forbidden이 더 적절할 수 있습니다. UNATHORIZED는 인증 실패.
                .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    /**
     * 유효하지 않은 요청 데이터 또는 비즈니스 로직 상의 문제 예외 처리 (400 Bad Request)
     * RecipientInvalidDataException
     */
    @ExceptionHandler(RecipientInvalidDataException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidDataException(RecipientInvalidDataException ex) {
        log.warn("Bad Request (400 Bad Request): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    /**
     * Integer 변환 실패 등 숫자 관련 잘못된 입력 처리 (400 Bad Request)
     * InvalidIntegerConversionException
     */
    @ExceptionHandler(InvalidIntegerConversionException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidIntegerConversionException(InvalidIntegerConversionException ex) {
        log.warn("Bad Request (400): Integer 변환 실패 - {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, ex.getMessage()));
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

        log.warn("Validation failed (400): {}", errorMessage.toString().trim());

        try {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMessage.toString().trim()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        log.warn("BindException (400): {}", errorMessage.toString().trim());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMessage.toString().trim()));
    }

    /**
     * 쿼리 파라미터 타입 불일치 시 발생 (예: int year = "abc")
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String errorMsg = String.format("요청 파라미터 [%s]에 잘못된 값이 들어왔습니다. '%s'는 [%s] 타입이어야 합니다.",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

        log.warn("타입 불일치 (400): {}", errorMsg);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMsg));
    }
}
