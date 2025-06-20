package kodanect.common.exception.config;

import kodanect.common.exception.custom.*;
import kodanect.common.response.ApiResponse;
import kodanect.domain.article.exception.ArticleNotFoundException;
import kodanect.domain.article.exception.InvalidBoardCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static kodanect.common.exception.config.MessageKeys.ARTICLE_NOT_FOUND;
import static kodanect.common.exception.config.MessageKeys.INVALID_BOARD_CODE;
import static kodanect.common.exception.config.MessageKeys.FILE_ACCESS_VIOLATION;
import static kodanect.common.exception.config.MessageKeys.FILE_NOT_FOUND;

/**
 * 게시판 관련 예외를 처리하는 핸들러 클래스.
 */
@RestControllerAdvice(basePackages = "kodanect.domain.article")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ArticleExceptionHandler {

    private static final SecureLogger log = SecureLogger.getLogger(ArticleExceptionHandler.class);

    private final MessageSourceAccessor messageSourceAccessor;

    /**
     * 게시글이 존재하지 않을 때 예외 처리
     */
    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleArticleNotFound(ArticleNotFoundException ex) {
        return handle(ex, ARTICLE_NOT_FOUND, "게시글을 찾을 수 없습니다.");
    }

    /**
     * 존재하지 않는 게시판 코드로 요청할 경우 예외 처리
     */
    @ExceptionHandler(InvalidBoardCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBoardCode(InvalidBoardCodeException ex) {
        return handle(ex, INVALID_BOARD_CODE, "잘못된 게시판 코드입니다.");
    }

    /**
     * 파일 경로 접근이 허용되지 않는 경우 예외 처리
     */
    @ExceptionHandler(FileAccessViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileAccessViolation(FileAccessViolationException ex) {
        return handle(ex, FILE_ACCESS_VIOLATION, "파일 경로 접근이 거부되었습니다.");
    }

    /**
     * 요청한 파일이 존재하지 않을 경우 예외 처리
     */
    @ExceptionHandler(FileMissingException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileMissing(FileMissingException ex) {
        return handle(ex, FILE_NOT_FOUND, "파일을 찾을 수 없습니다.");
    }

    /**
     * 공통 처리 메서드
     */
    private ResponseEntity<ApiResponse<Void>> handle(AbstractCustomException ex, String messageKey, String defaultMessage) {
        String resolvedMessage = messageSourceAccessor.getMessage(
                messageKey,
                ex.getArguments(),
                defaultMessage
        );

        log.warn("[게시판 예외] {} - {}: {}", ex.getClass().getSimpleName(), messageKey, resolvedMessage, ex);

        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.fail(ex.getStatus(), resolvedMessage));
    }
}
