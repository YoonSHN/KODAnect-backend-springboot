package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.REPLY_NOT_FOUND;

public class MemorialReplyNotFoundException extends AbstractCustomException {
    /* 기증자 추모관 댓글을 못 찾았을 때 */

    private final Integer replySeq;

    public MemorialReplyNotFoundException(Integer replySeq) {
        super(REPLY_NOT_FOUND);
        this.replySeq = replySeq;
    }

    @Override
    public String getMessageKey() {
        return REPLY_NOT_FOUND;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{replySeq};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

}
