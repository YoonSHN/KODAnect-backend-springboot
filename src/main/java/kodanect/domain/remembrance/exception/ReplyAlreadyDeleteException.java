package kodanect.domain.remembrance.exception;

public class ReplyAlreadyDeleteException extends RuntimeException {
    /* 이미 삭제된 댓글일 경우 */
    public ReplyAlreadyDeleteException(String message) {
        super(message);
    }

    public ReplyAlreadyDeleteException() {
        super("이미 삭제된 댓글 입니다.");
    }
}
