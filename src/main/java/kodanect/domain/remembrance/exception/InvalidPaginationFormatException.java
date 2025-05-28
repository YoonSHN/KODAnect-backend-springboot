package kodanect.domain.remembrance.exception;

public class InvalidPaginationFormatException extends RuntimeException {
    /* 숫자가 아닌 값이 들어왔을 경우 */
    public InvalidPaginationFormatException(String message) {
        super(message);
    }

    public InvalidPaginationFormatException() {
        super("페이지 번호 또는 페이지 크기는 숫자 형식이어야 합니다.");
    }
}
