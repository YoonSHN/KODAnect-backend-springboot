package kodanect.common.exception;

// InvalidCommentDataException.java (댓글 내용, 비밀번호 유효성 등)
public class InvalidRecipientDataException extends RuntimeException {
  public InvalidRecipientDataException(String message) {
    super(message);
  }
}
