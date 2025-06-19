package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.FILE_NOT_FOUND;

public class FileNotFoundException extends AbstractCustomException {

    private final String fileName;

    public FileNotFoundException(String fileName) {
        super(FILE_NOT_FOUND);
        this.fileName = fileName;
    }

    @Override
    public String getMessageKey() {
        return FILE_NOT_FOUND;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{fileName};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
