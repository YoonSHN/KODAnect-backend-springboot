package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.FILE_UNSUPPORTED_TYPE;

public class UnsupportedImageTypeException extends AbstractCustomException {

    private final String contentType;

    public UnsupportedImageTypeException(String contentType) {
        super(FILE_UNSUPPORTED_TYPE);
        this.contentType = contentType;
    }

    @Override
    public String getMessageKey() {
        return FILE_UNSUPPORTED_TYPE;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{contentType};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
