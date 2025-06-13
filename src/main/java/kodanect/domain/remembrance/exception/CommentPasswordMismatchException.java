package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.COMMENT_PASSWORD_MISMATCH;

/** 댓글 비밀번호가 일치하지 않을 경우 발생하는 예외 */
public class CommentPasswordMismatchException extends AbstractCustomException {

    private final Integer commentSeq;

    public CommentPasswordMismatchException(Integer commentSeq) {
        super(COMMENT_PASSWORD_MISMATCH);
        this.commentSeq = commentSeq;
    }

    @Override
    public String getMessageKey() {
        return COMMENT_PASSWORD_MISMATCH;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{commentSeq};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }

}
