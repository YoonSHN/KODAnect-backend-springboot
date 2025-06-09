package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.REPLY_PASSWORD_MISMATCH;

public class ReplyPasswordMismatchException extends AbstractCustomException {
    /* 댓글 비밀번호가 일치하지 않을 경우 */

    private final Integer replySeq;

    public ReplyPasswordMismatchException(Integer replySeq) {
        super(REPLY_PASSWORD_MISMATCH);
        this.replySeq = replySeq;
    }

    @Override
    public String getMessageKey() {
        return REPLY_PASSWORD_MISMATCH;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{replySeq};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }

}
