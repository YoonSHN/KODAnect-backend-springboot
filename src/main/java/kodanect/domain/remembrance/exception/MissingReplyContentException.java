package kodanect.domain.remembrance.exception;

public class MissingReplyContentException extends RuntimeException {
    /* 댓글 내용이 비어있을 경우 */
    public MissingReplyContentException(String message) {
        super(message);
    }

    public MissingReplyContentException() {
        super("댓글 내용은 비어있을 수 없습니다.");
    }
}
