package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.FILE_ACCESS_VIOLATION;

public class FileAccessViolationException extends AbstractCustomException {

    private final String path;

    public FileAccessViolationException(String path) {
        super(FILE_ACCESS_VIOLATION + path);
        this.path = path;
    }

    @Override
    public String getMessageKey() {
        return FILE_ACCESS_VIOLATION;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{path};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
