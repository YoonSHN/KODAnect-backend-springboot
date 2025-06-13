package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.COMMENT_NOT_FOUND;

/** 기증자 추모관 댓글을 못 찾았을 경우 발생하는 예외 */
public class MemorialCommentNotFoundException extends AbstractCustomException {

    private final Integer commentSeq;

    public MemorialCommentNotFoundException(Integer commentSeq) {
        super(COMMENT_NOT_FOUND);
        this.commentSeq = commentSeq;
    }

    @Override
    public String getMessageKey() {
        return COMMENT_NOT_FOUND;
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
