package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.FILE_ACCESS_VIOLATION;

public class FileAccessViolationException extends AbstractCustomException {

    private final String path;

    public FileAccessViolationException(String path) {
        super("잘못된 파일 경로 접근: " + path);
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
