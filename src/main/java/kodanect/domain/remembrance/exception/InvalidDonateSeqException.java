package kodanect.domain.remembrance.exception;

public class InvalidDonateSeqException extends RuntimeException {
    /* 유효하지 않은 게시글 번호를 요청할 경우 */
    public InvalidDonateSeqException(String message) {
        super(message);
    }

    public InvalidDonateSeqException() {
        super("게시글 번호 입력 형식이 올바르지 않습니다.");
    }
}
