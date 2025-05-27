package kodanect.common.exception;

// InvalidCommentDataException.java (댓글 내용, 비밀번호 유효성 등)
public class InvalidCommentDataException extends RuntimeException {
  public InvalidCommentDataException(String message) {
    super(message);
  }
}
