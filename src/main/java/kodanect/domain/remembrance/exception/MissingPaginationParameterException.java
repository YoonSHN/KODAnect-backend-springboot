package kodanect.domain.remembrance.exception;

public class MissingPaginationParameterException extends RuntimeException {
    /* 페이지 입력 값이 누락될 경우 */
    public MissingPaginationParameterException(String message) {
        super(message);
    }

    public MissingPaginationParameterException() {
        super("페이지 번호 또는 페이지 크기가 입력되지 않았습니다.");
    }
}
