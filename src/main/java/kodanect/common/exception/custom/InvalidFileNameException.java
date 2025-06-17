package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.INVALID_FILE_NAME;

/**
 * 유효하지 않은 파일명이 입력된 경우 발생하는 예외.
 * 예: 경로 조작 가능성이 있는 파일명, 포맷 불일치 등
 */
public class InvalidFileNameException extends AbstractCustomException {

    private final String fileName;

    public InvalidFileNameException(String fileName) {
        super(INVALID_FILE_NAME);
        this.fileName = fileName;
    }

    public InvalidFileNameException(String fileName, Throwable cause) {
        super(INVALID_FILE_NAME, cause);
        this.fileName = fileName;
    }

    @Override
    public String getMessage() {
        return String.format("[입력 오류] 허용되지 않는 파일명: %s", fileName);
    }

    @Override
    public String getMessageKey() {
        return INVALID_FILE_NAME;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{ fileName };
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}

