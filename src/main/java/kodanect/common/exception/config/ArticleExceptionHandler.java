package kodanect.common.exception.config;

import kodanect.common.exception.custom.ArticleNotFoundException;
import kodanect.common.exception.custom.InvalidBoardOptionException;
import kodanect.common.response.ApiResponse;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {
    kodanect.domain.article.controller.ArticleController.class
})
public class ArticleExceptionHandler {

    private final MessageSourceAccessor messageSourceAccessor;

    public ArticleExceptionHandler(MessageSourceAccessor messageSourceAccessor) {
        this.messageSourceAccessor = messageSourceAccessor;
    }

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


    @ExceptionHandler(InvalidBoardOptionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBoardOption(InvalidBoardOptionException ex) {
        String message = messageSourceAccessor.getMessage(
                ex.getMessageKey(),
                ex.getArguments(),
                "유효하지 않은 게시판 옵션입니다."
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, message));
    }
}
