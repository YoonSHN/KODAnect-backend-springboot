package kodanect.domain.donation.controller.advice;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int BAD_REQUEST = 400;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errorMap = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing // 중복 키 충돌 방지
                ));

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "code", BAD_REQUEST,
                "message", "입력값이 유효하지 않습니다.",
                "errors", errorMap
        ));
    }

    // 추가적인 예외 처리도 여기에 작성할 수 있음
}