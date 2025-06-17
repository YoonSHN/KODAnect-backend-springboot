package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import java.nio.file.Path;

import static kodanect.common.exception.config.MessageKeys.HEAVEN_FILE_SAVE_FAIL;

public class FileStorageException extends AbstractCustomException {

    private final transient Path path;
    private final String fileName;

    public FileStorageException(Path path, String fileName) {
        super(HEAVEN_FILE_SAVE_FAIL);
        this.path = path;
        this.fileName = fileName;
    }

    @Override
    public String getMessageKey() {
        return HEAVEN_FILE_SAVE_FAIL;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{path, fileName};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
