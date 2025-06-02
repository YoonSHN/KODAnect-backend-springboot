package kodanect.common.exception.config;

import kodanect.common.response.ApiResponse;
import kodanect.domain.donation.exception.BadRequestException;
import kodanect.domain.donation.exception.DonationNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

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
@Component
public class GlobalExcepHndlr {

    private final MessageSourceAccessor messageSourceAccessor;

    // 생성자를 통해 MessageSourceAccessor를 주입받습니다.
    public GlobalExcepHndlr(MessageSourceAccessor messageSourceAccessor) {
        this.messageSourceAccessor = messageSourceAccessor;
    }

    /**
     * 404 예외 처리 (Resource Not Found)
     * - 매핑되지 않은 URI 요청 또는 명시적으로 NOT_FOUND 예외를 던진 경우
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound() {
        String msg = messageSourceAccessor.getMessage("error.notfound", "요청한 리소스를 찾을 수 없습니다.");
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND, msg));
    }

    /**
     * 400 예외 처리: @Valid 검증 실패 시 BindException 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
        String message = "잘못된 입력값입니다.";
        if (ex.hasFieldErrors()) {
            message = ex.getFieldErrors().get(0).getDefaultMessage();  // 첫 번째 에러 메시지 사용
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(BadRequestException.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        log.warn("BadRequestException: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    /**
     * 400 예외 처리: @RequestBody @Valid 검증 실패 시 MethodArgumentNotValidException 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        // 가장 첫 번째 에러 메시지 키를 가져옴
        String defaultMsgKey = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        String resolvedMsg;

        try {
            // 키를 메시지 소스에서 해석
            resolvedMsg = messageSourceAccessor.getMessage(defaultMsgKey);
        }
        catch (Exception e) {
            // 메시지 소스에서 못 찾으면 그냥 키 문자열 그대로 사용
            resolvedMsg = defaultMsgKey;
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, resolvedMsg));
    }

    /**
     * 400 예외 처리: 서비스에서 throw new IllegalArgumentException("...") 한 경우
     * - ex.getMessage() 가 메시지 키라면 메시지 소스로부터 실제 문구를 찾아서 사용
     * - 메시지 키가 아닌 일반 한글 메시지라면 그대로 반환
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArg(IllegalArgumentException ex) {
        String keyOrMsg = ex.getMessage();
        // messageSourceAccessor 에 해당 키가 있는지 먼저 시도
        String msg;
        try {
            msg = messageSourceAccessor.getMessage(keyOrMsg);
        }
        catch (Exception e) {
            // 키가 없으면 ex.getMessage() 를 그대로 사용
            msg = keyOrMsg;
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, msg));
    }

    /**
     * 500 예외 처리: 나머지 모든 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleInternalServerError(Exception ex) {
        log.error("Unhandled exception: ", ex);
        String msg = messageSourceAccessor.getMessage("error.internal", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, msg));
    }

    @ExceptionHandler(DonationNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDonationNotFound(DonationNotFoundException ex) {
        String msg;
        try {
            msg = messageSourceAccessor.getMessage(ex.getMessage());
        }
        catch (Exception e) {
            msg = ex.getMessage(); // 메시지 키가 아니면 그대로
        }
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND, msg));
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Unhandled exception: ", ex);

        String message = messageSourceAccessor.getMessage("error.internal"); // ← 이 줄이 핵심
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, message));
    }
}