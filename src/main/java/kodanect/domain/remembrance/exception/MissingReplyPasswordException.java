package kodanect.domain.remembrance.exception;

public class MissingReplyPasswordException extends RuntimeException {
    /* 댓글 비밀 번호가 비어있을 경우 */
    public MissingReplyPasswordException(String message) {
        super(message);
    }

    public MissingReplyPasswordException() {
        super("댓글 비밀번호는 비어있을 수 없습니다.");
    }
}
