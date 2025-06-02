package kodanect.domain.remembrance.exception;

public class InvalidReplySeqException extends RuntimeException {
    /* 댓글 번호가 유효하지 않을 경우 */
    public InvalidReplySeqException(String message) {
        super(message);
    }

    public InvalidReplySeqException() {
        super("댓글 번호 입력 형식이 올바르지 않습니다.");
    }
}
