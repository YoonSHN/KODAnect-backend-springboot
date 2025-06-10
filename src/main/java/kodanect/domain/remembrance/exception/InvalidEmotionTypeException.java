package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.EMOTION_INVALID;

/** 이모지 이름 형식이 맞지 않을 경우 발생하는 예외 */
public class InvalidEmotionTypeException extends AbstractCustomException {

    private final String emotion;

    public InvalidEmotionTypeException(String emotion) {
        super(EMOTION_INVALID);
        this.emotion = emotion;
    }

    @Override
    public String getMessageKey() {
        return EMOTION_INVALID;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{emotion};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
