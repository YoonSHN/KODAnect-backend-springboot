package kodanect.domain.remembrance.exception;

public class MissingSearchDateParameterException extends RuntimeException {
    /* 검색 시작일 또는 종료일이 누락 됐을 경우 */
    public MissingSearchDateParameterException(String message) {
        super(message);
    }

    public MissingSearchDateParameterException() {
        super("검색 시작일 또는 종료일이 입력되지 않았습니다.");
    }
}
