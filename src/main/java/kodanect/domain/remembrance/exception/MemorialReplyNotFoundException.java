package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.REPLY_NOT_FOUND;

/** 기증자 추모관 댓글을 못 찾았을 경우 발생하는 예외 */
public class MemorialReplyNotFoundException extends AbstractCustomException {

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
