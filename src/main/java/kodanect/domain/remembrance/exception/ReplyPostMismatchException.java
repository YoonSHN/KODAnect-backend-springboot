package kodanect.domain.remembrance.exception;

public class ReplyPostMismatchException extends RuntimeException {
    /* 댓글과 게시글의 도메인이 불일치 할 경우 */
    public ReplyPostMismatchException(String message) {
        super(message);
    }

    public ReplyPostMismatchException() {
        super("댓글과 게시글의 게시글 번호가 일치하지 않습니다.");
    }
}
