package kodanect.domain.recipient.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

// CommentNotFoundException.java
public class RecipientCommentNotFoundException extends AbstractCustomException {

    private static final String MESSAGE_KEY = "recipient.comment.notfound"; // 메시지 키 정의
    private final Object[] arguments;

    public RecipientCommentNotFoundException() {
        this("댓글을 찾을 수 없습니다.");
    }

    public RecipientCommentNotFoundException(String message) {
        super(message);
        this.arguments = new Object[0];
    }

    public RecipientCommentNotFoundException(String message, Object... arguments) {
        super(message);
        this.arguments = arguments;
    }

    @Override
    public String getMessageKey() {
        return MESSAGE_KEY;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

}
