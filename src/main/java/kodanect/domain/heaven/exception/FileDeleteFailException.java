package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.FILE_SAVE_FAIL;

public class FileDeleteFailException extends AbstractCustomException {

    private final String fileName;

    public FileDeleteFailException(String fileName) {
        super(FILE_SAVE_FAIL);
        this.fileName = fileName;
    }

    @Override
    public String getMessageKey() {
        return FILE_SAVE_FAIL;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{fileName};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
