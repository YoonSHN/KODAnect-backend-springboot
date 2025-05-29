package kodanect.common.exception.config;

import kodanect.common.response.ApiResponse;
import kodanect.domain.remembrance.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class GlobalExcepHndlr {

    /**
     * 400 예외 처리
     *
     * 잘못된 입력 발생 시 400 응답 반환
     * */
    @ExceptionHandler({
        InvalidDonateSeqException.class,
        InvalidEmotionTypeException.class,
        InvalidPaginationRangeException.class,
        InvalidReplySeqException.class,
        InvalidSearchDateFormatException.class,
        InvalidSearchDateRangeException.class,
        MissingReplyContentException.class,
        MissingReplyPasswordException.class,
        MissingReplyWriterException.class,
        MissingSearchDateParameterException.class,
        ReplyIdMismatchException.class,
        ReplyPostMismatchException.class
    })public ResponseEntity<ApiResponse<Void>> handleBadRequest() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."));
    }

    /**
     * 403 예외 처리
     *
     * 권한 오류 발생 시 403 응답 반환
     * */
    @ExceptionHandler(ReplyPasswordMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden() {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(HttpStatus.FORBIDDEN, "비밀번호가 일치하지 않습니다."));
    }

    /**
     * 404 예외 처리
     *
     * 매핑되지 않은 URI 요청에 대해 404 응답 반환
     */
    @ExceptionHandler({
        MemorialNotFoundException.class,
        MemorialReplyNotFoundException.class,
        NoHandlerFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleNotFound() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."));
    }

    /**
     * 409 예외 처리
     *
     * 충돌 발생 시 409 응답 반환
     */
    @ExceptionHandler(ReplyAlreadyDeleteException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict() {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(HttpStatus.CONFLICT, "해당 항목은 이미 삭제되었습니다."));
    }

    /**
     * 500 예외 처리
     *
     * 처리되지 않은 런타임 예외에 대해 500 응답 반환
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleInternalServerError() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."));
    }

    /**
     * 500 예외 처리
     *
     * 처리되지 않은 메세지키 미응답시 500 응답 반환
     */
    @ExceptionHandler(NoSuchMessageException.class)
    public ResponseEntity<ApiResponse<?>> handleNoMessage(NoSuchMessageException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, "메시지 키 없음"));
    }

}
