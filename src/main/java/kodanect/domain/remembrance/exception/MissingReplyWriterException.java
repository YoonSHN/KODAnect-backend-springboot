package kodanect.domain.remembrance.exception;

public class MissingReplyWriterException extends RuntimeException {
    /* 댓글 작성자가 비어있을 경우 */
    public MissingReplyWriterException(String message) {
        super(message);
    }

    public MissingReplyWriterException() {
        super("댓글 작성자는 비어있을 수 없습니다.");
    }
}
