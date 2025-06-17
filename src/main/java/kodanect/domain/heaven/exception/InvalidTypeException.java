package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.HEAVEN_TYPE_INVALID;

public class InvalidTypeException extends AbstractCustomException {

    private final String type;

    public InvalidTypeException(String type) {
        super(HEAVEN_TYPE_INVALID);
        this.type = type;
    }

    @Override
    public String getMessageKey() {
        return HEAVEN_TYPE_INVALID;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{type};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
