package kodanect.common.exception.custom;

import kodanect.common.exception.config.MessageKeys;
import org.springframework.http.HttpStatus;

public class InvalidIntegerConversionException extends AbstractCustomException {

    private final String input; // input 값을 저장

    public InvalidIntegerConversionException(String input) {
        super(MessageKeys.COMMON_INVALID_INTEGER_CONVERSION);
        this.input = input;
    }

    @Override
    public String getMessageKey() {
        return MessageKeys.COMMON_INVALID_INTEGER_CONVERSION;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{input}; // input 값을 메시지 파라미터로 반환
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
