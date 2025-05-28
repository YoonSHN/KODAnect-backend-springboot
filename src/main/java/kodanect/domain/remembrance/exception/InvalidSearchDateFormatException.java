package kodanect.domain.remembrance.exception;

public class InvalidSearchDateFormatException extends RuntimeException {
    /* 검색 시작일 또는 종료일의 형식이 올바르지 않을 경우 */
    public InvalidSearchDateFormatException(String message) {
        super(message);
    }

    public InvalidSearchDateFormatException() {
        super("검색 시작일 또는 종료일의 형식이 올바르지 않습니다.");
    }
}
