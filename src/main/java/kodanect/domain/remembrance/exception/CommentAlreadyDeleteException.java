package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.COMMENT_ALREADY_DELETED;

/** 이미 삭제된 댓글일 경우 발생하는 예외 */
public class CommentAlreadyDeleteException extends AbstractCustomException {

    private final Integer commentSeq;

    public CommentAlreadyDeleteException(Integer commentSeq) {
        super(COMMENT_ALREADY_DELETED);
        this.commentSeq = commentSeq;
    }

    @Override
    public String getMessageKey() {
        return COMMENT_ALREADY_DELETED;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{commentSeq};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
