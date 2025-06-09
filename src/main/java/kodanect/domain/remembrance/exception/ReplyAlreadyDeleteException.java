package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.REPLY_ALREADY_DELETED;

public class ReplyAlreadyDeleteException extends AbstractCustomException {
    /* 이미 삭제된 댓글일 경우 */

    private final Integer replySeq;

    public ReplyAlreadyDeleteException(Integer replySeq) {
        super(REPLY_ALREADY_DELETED);
        this.replySeq = replySeq;
    }

    @Override
    public String getMessageKey() {
        return REPLY_ALREADY_DELETED;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{replySeq};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
