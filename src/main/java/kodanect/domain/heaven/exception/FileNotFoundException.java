package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.FILE_NOT_FOUND;

public class FileNotFoundException extends AbstractCustomException {

    private final transient Object file;

    public FileNotFoundException(Object file) {
        super(FILE_NOT_FOUND);
        this.file = file;
    }

    @Override
    public String getMessageKey() {
        return FILE_NOT_FOUND;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{file};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
