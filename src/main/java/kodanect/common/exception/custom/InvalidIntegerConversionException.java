package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

public class InvalidIntegerConversionException extends AbstractCustomException {

    private static final String MESSAGE_KEY = "common.invalid.integer.conversion";
    private final String input;

    public InvalidIntegerConversionException(String input) {
        super(MESSAGE_KEY);
        this.input = input;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{input};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getMessage() {
        return String.format("[숫자 변환 오류] input=%s", input);
    }

    @Override
    public String getMessageKey() {
        return MESSAGE_KEY;
    }
}
