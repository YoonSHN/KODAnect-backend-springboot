package kodanect.domain.remembrance.exception;

public class ReplyPasswordMismatchException extends RuntimeException {
    /* 댓글 비밀번호가 일치하지 않을 경우 */
    public ReplyPasswordMismatchException(String message) {
        super(message);
    }

    public ReplyPasswordMismatchException() {
        super("댓글 비밀번호가 일치하지 않습니다.");
    }
}
