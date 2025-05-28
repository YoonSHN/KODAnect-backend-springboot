package kodanect.domain.remembrance.exception;

public class ReplyIdMismatchException extends RuntimeException {
    /* 댓글 번호가 일치하지 않을 경우 */
    public ReplyIdMismatchException(String message) {
        super(message);
    }

    public ReplyIdMismatchException() {
        super("댓글과 게시글의 번호가 일치하지 않습니다.");
    }
}
