package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.FILE_SIZE_EXCEED;

public class FileSizeExceededException extends AbstractCustomException {

    private final long fileSize;

    public FileSizeExceededException(long fileSize) {
        super(FILE_SIZE_EXCEED);
        this.fileSize = fileSize;
    }

    @Override
    public String getMessageKey() {
        return FILE_SIZE_EXCEED;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{fileSize};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.PAYLOAD_TOO_LARGE;
    }
}
