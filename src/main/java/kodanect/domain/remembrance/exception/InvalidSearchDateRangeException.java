package kodanect.domain.remembrance.exception;

public class InvalidSearchDateRangeException extends RuntimeException {
    /* 검색 종료일이 시작일 보다 미래일 경우 */
    public InvalidSearchDateRangeException(String message) {
        super(message);
    }

    public InvalidSearchDateRangeException() {
        super("검색 시작일과 종료일의 범위가 올바르지 않습니다.");
    }
}
