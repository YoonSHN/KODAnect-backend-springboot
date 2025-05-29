package kodanect.domain.recipient.exception;

import kodanect.common.exception.custom.InvalidIntegerConversionException;
import kodanect.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "kodanect.domain.recipient")

public class RecipientExceptionHandler {

    /**
     * 리소스를 찾을 수 없거나 이미 삭제된 경우의 예외 처리 (404 Not Found)
     * RecipientNotFoundException, CommentNotFoundException
     */
    @ExceptionHandler({RecipientNotFoundException.class, CommentNotFoundException.class})
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
    @ExceptionHandler(InvalidPasscodeException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidPasscodeException(InvalidPasscodeException ex) {
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
    public ResponseEntity<ApiResponse<String>> handleInvalidCommentDataException(RecipientInvalidDataException ex) {
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
}
