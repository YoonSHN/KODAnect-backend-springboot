package kodanect.domain.remembrance.exception;

public class InvalidPaginationRangeException extends RuntimeException {
    /* 페이지 범위가 잘못 됐을 경우 */
    public InvalidPaginationRangeException(String message) {
        super(message);
    }

    public InvalidPaginationRangeException() {
        super("페이지 번호와 크기는 1 이상의 값이어야 합니다.");
    }
}
