package kodanect.common.exception.custom;

import static kodanect.common.exception.config.MessageKeys.ARTICLE_NOT_FOUND;

public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException() {
        super(ARTICLE_NOT_FOUND);
    }

    public String getMessageKey() {
        return ARTICLE_NOT_FOUND;
    }

    public Object[] getArguments() {
        return new Object[0];
    }
}
