package kodanect.domain.recipient.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.RECIPIENT_COMMENT_NOT_FOUND;

public class RecipientCommentNotFoundException extends AbstractCustomException {

    private final Integer commentSeq; // 존재하지 않는 댓글 ID

    public RecipientCommentNotFoundException(Integer commentSeq) {
        super(RECIPIENT_COMMENT_NOT_FOUND);
        this.commentSeq = commentSeq;
    }

    @Override
    public String getMessageKey() {
        return RECIPIENT_COMMENT_NOT_FOUND;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{commentSeq};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getMessage() {
        return String.format("[댓글 없음] commentId=%d", commentSeq);
    }
}