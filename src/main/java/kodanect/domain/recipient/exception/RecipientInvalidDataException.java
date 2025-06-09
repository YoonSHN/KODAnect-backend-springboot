package kodanect.domain.recipient.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

// InvalidCommentDataException.java (댓글 내용, 비밀번호 유효성 등)
public class RecipientInvalidDataException extends AbstractCustomException {

    private static final String MESSAGE_KEY = "recipient.invalid.data"; // 메시지 키 정의
    private final Object[] arguments;

    public RecipientInvalidDataException() {
        this("유효하지 않은 요청 데이터입니다.");
    }

    public RecipientInvalidDataException(String message) {
        super(message);
        this.arguments = new Object[0];
    }

    public RecipientInvalidDataException(String message, Object... arguments) {
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
        return HttpStatus.BAD_REQUEST;
    }
}