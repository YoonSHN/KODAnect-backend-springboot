package kodanect.common.exception.config;

import kodanect.common.exception.custom.*;
import kodanect.common.response.ApiResponse;
import kodanect.domain.article.exception.ArticleNotFoundException;
import kodanect.domain.article.exception.InvalidBoardCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 게시판 관련 예외를 처리하는 핸들러 클래스.
 */
@RestControllerAdvice(assignableTypes = {
    kodanect.domain.article.controller.ArticleController.class
})
@RequiredArgsConstructor
public class ArticleExceptionHandler {

    private final MessageSourceAccessor messageSourceAccessor;

    /**
     * 게시글이 존재하지 않을 때 예외 처리
     */
    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleArticleNotFound(ArticleNotFoundException ex) {
        String message = messageSourceAccessor.getMessage(
                ex.getMessageKey(),
                ex.getArguments(),
                "게시글을 찾을 수 없습니다."
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND, message));
    }

    /**
     * 존재하지 않는 게시판 코드로 요청할 경우 예외 처리
     */
    @ExceptionHandler(InvalidBoardCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBoardCode(InvalidBoardCodeException ex) {
        String message = messageSourceAccessor.getMessage(
                ex.getMessageKey(),
                ex.getArguments(),
                "잘못된 게시판 코드입니다."
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, message));
    }

    /**
     * 파일 경로 접근이 허용되지 않는 경우 예외 처리
     */
    @ExceptionHandler(FileAccessViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileAccessViolation(FileAccessViolationException ex) {
        String message = messageSourceAccessor.getMessage(
                ex.getMessageKey(),
                ex.getArguments(),
                "파일 경로 접근이 거부되었습니다."
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(HttpStatus.FORBIDDEN, message));
    }

    /**
     * 요청한 파일이 존재하지 않을 경우 예외 처리
     */
    @ExceptionHandler(FileMissingException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileMissing(FileMissingException ex) {
        String message = messageSourceAccessor.getMessage(
                ex.getMessageKey(),
                ex.getArguments(),
                "파일을 찾을 수 없습니다."
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(HttpStatus.NOT_FOUND, message));
    }



}
