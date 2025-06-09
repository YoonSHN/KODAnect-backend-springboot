package kodanect.domain.recipient.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

public class RecipientInvalidPasscodeException extends AbstractCustomException {

    private static final String MESSAGE_KEY = "recipient.invalid.passcode"; // 메시지 키 정의
    private final Object[] arguments;

    public RecipientInvalidPasscodeException() {
        this("비밀번호가 일치하지 않습니다.");
    }

    public RecipientInvalidPasscodeException(String message) {
        super(message);
        this.arguments = new Object[0];
    }

    public RecipientInvalidPasscodeException(String message, Object... arguments) {
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
        return HttpStatus.UNAUTHORIZED;
    }
}
