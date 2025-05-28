package kodanect.common.exception.config;

import kodanect.common.exception.CommentNotFoundException;
import kodanect.common.exception.InvalidPasscodeException;
import kodanect.common.exception.RecipientInvalidDataException;
import kodanect.common.exception.RecipientNotFoundException;
import kodanect.common.exception.custom.InvalidIntegerConversionException;
import kodanect.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 전역 예외 핸들러
 *
 * 컨트롤러에서 발생하는 404, 500 예외를 ApiResponse 포맷으로 응답
 *
 * 역할
 * - 존재하지 않는 리소스 요청 처리
 * - 알 수 없는 서버 내부 예외 처리
 *
 * 특징
 * - 사용자 정의 예외는 처리하지 않음
 * - 컨트롤러 단에서 발생한 표준 오류 응답 전용
 */
@Slf4j
@RestControllerAdvice
public class GlobalExcepHndlr {

    /**
     * 404 예외 처리
     *
     * 매핑되지 않은 URI 요청에 대해 404 응답 반환
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound() {
        log.warn("404 Not Found: Requested resource not found.");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."));
    }

    /**
     * Bean Validation 실패 예외 처리 (MethodArgumentNotValidException)
     * @Valid 어노테이션을 통한 유효성 검사 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 모든 유효성 검사 오류 메시지를 결합
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation Error (400 Bad Request): {}", errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, "유효성 검사 실패: " + errorMessage));
    }

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
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    /**
     * 유효하지 않은 요청 데이터 또는 비즈니스 로직 상의 문제 예외 처리 (400 Bad Request)
     * InvalidCommentDataException
     */
    @ExceptionHandler(RecipientInvalidDataException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidCommentDataException(RecipientInvalidDataException ex) {
        log.warn("Bad Request (400 Bad Request): {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    /**
     * 500 예외 처리
     * 처리되지 않은 런타임 예외에 대해 500 응답 반환
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleInternalServerError() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."));
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
