package kodanect.domain.remembrance.exception;

public class InvalidEmotionTypeException extends RuntimeException {
    /* 이모지 이름 형식이 맞지 않을 경우 */
    public InvalidEmotionTypeException(String message) {
        super(message);
    }

    public InvalidEmotionTypeException() {
        super("이모지 이름 입력 형식이 올바르지 않습니다.");
    }
}
