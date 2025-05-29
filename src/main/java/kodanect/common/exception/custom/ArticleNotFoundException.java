package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.ARTICLE_NOT_FOUND;

public class ArticleNotFoundException extends AbstractCustomException {

    private final Integer articleSeq;

    public ArticleNotFoundException(Integer articleSeq) {
        super(ARTICLE_NOT_FOUND);
        this.articleSeq = articleSeq;
    }

    public String getMessageKey() {
        return ARTICLE_NOT_FOUND;
    }

    public Object[] getArguments() {
        return new Object[articleSeq];
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
