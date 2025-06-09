package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

public class InvalidIntegerConversionException extends AbstractCustomException {

    private static final String MESSAGE_KEY = "common.invalid.integer.conversion"; // 적절한 메시지 키 정의
    private final transient Object[] arguments;

    public InvalidIntegerConversionException() {
        this("유효하지 않은 숫자 형식입니다.");
    }

    public InvalidIntegerConversionException(String message) {
        super(message);
        this.arguments = new Object[0];
    }

    public InvalidIntegerConversionException(String message, Object... arguments) {
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
        return HttpStatus.BAD_REQUEST; // 400 Bad Request
    }
}
