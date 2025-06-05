package kodanect.domain.remembrance.exception;

public class MemorialReplyNotFoundException extends RuntimeException {
    /* 기증자 추모관 댓글을 못 찾았을 때 */
    public MemorialReplyNotFoundException(String message) {
        super(message);
    }

    public MemorialReplyNotFoundException() {
        super("게시글 댓글을 찾을 수 없습니다.");
    }
}
