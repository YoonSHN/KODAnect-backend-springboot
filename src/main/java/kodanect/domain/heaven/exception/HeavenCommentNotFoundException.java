package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.HEAVEN_COMMENT_NOT_FOUND;

public class HeavenCommentNotFoundException extends AbstractCustomException {

    private final Integer commentSeq;

    public HeavenCommentNotFoundException(Integer commentSeq) {
        super(HEAVEN_COMMENT_NOT_FOUND);
        this.commentSeq = commentSeq;
    }

    @Override
    public String getMessageKey() {
        return HEAVEN_COMMENT_NOT_FOUND;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{commentSeq};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
