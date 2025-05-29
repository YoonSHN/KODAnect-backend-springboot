package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.FILE_NOT_FOUND;

public class FileMissingException extends AbstractCustomException {

    private final String fileName;

    public FileMissingException(String fileName) {
        super("파일이 존재하지 않거나 읽을 수 없습니다: " + fileName);
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
