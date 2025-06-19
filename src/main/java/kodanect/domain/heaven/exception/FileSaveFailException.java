package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.FILE_DELETE_FAIL;

public class FileSaveFailException extends AbstractCustomException {

    private final String fileName;

    public FileSaveFailException(String fileName) {
        super(FILE_DELETE_FAIL);
        this.fileName = fileName;
    }

    @Override
    public String getMessageKey() {
        return FILE_DELETE_FAIL;
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
