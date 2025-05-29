package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.ARTICLE_NOT_FOUND;

public class ArticleNotFoundException extends AbstractCustomException {

    public ArticleNotFoundException(Integer articleSeq) {
        super(ARTICLE_NOT_FOUND);
    }

    public String getMessageKey() {
        return ARTICLE_NOT_FOUND;
    }

    public Object[] getArguments() {
        return new Object[0];
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
