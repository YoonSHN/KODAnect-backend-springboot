package kodanect.domain.article.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.ARTICLE_NOT_FOUND;

/**
 * 게시글이 존재하지 않을 때 발생하는 예외.
 * 예: 요청한 articleSeq로 DB 조회 결과가 없을 경우.
 */
public class ArticleNotFoundException extends AbstractCustomException {

    private final Integer articleSeq;

    public ArticleNotFoundException(Integer articleSeq) {
        super(ARTICLE_NOT_FOUND);
        this.articleSeq = articleSeq;
    }

    public ArticleNotFoundException(Integer articleSeq, Throwable cause) {
        super(ARTICLE_NOT_FOUND, cause);
        this.articleSeq = articleSeq;
    }

    @Override
    public String getMessageKey() {
        return ARTICLE_NOT_FOUND;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{articleSeq};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getMessage() {
        return String.format("[게시글 없음] articleSeq=%d", articleSeq);
    }
}
