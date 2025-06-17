package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.HEAVEN_COMMENT_INFORM_MISMATCH;

public class HeavenCommentInformationMismatchException extends AbstractCustomException {

    private final Integer letterSeq;
    private final Integer commentSeq;

    public HeavenCommentInformationMismatchException(Integer letterSeq, Integer commentSeq) {
        super(HEAVEN_COMMENT_INFORM_MISMATCH);
        this.letterSeq = letterSeq;
        this.commentSeq = commentSeq;
    }

    @Override
    public String getMessageKey() {
        return HEAVEN_COMMENT_INFORM_MISMATCH;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{letterSeq, commentSeq};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
