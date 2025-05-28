package kodanect.domain.remembrance.exception;

public class MemorialNotFoundException extends RuntimeException {
    /* 기증자 추모관 게시물을 찾지 못했을 경우 */
    public MemorialNotFoundException(String message) {
        super(message);
    }

    public MemorialNotFoundException() {
        super("게시글을 찾을 수 없습니다.");
    }
}
