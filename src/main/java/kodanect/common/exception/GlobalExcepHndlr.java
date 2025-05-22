package kodanect.common.exception;


import kodanect.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 전역 예외 처리를 담당하는 핸들러 클래스입니다.
 *
 * 컨트롤러에서 발생하는 예외를 공통 포맷으로 변환하여 클라이언트에 응답하며,
 * 사용자 정의 예외, 잘못된 입력, 예상치 못한 예외에 대해 각각 처리합니다.
 */
// 여기서는 컨트롤러에서 쓰일 404 나 500 에러만 처리
@Slf4j
@RestControllerAdvice
public class GlobalExcepHndlr {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoHandlerFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(404, "요청한 리소스를 찾을 수 없습니다."));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = ex.getStatus();
        String message = ex.getReason() != null ? ex.getReason() : "요청 오류 발생";
        return ResponseEntity
                .status(status)
                .body(ApiResponse.fail(status.value(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleInternalServerError(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(500, "서버 내부 오류가 발생했습니다."));
    }

}
