package kodanect.domain.recipient.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.RECIPIENT_INVALID_DATA;

// InvalidCommentDataException.java (댓글 내용, 비밀번호 유효성 등)
public class RecipientInvalidDataException extends AbstractCustomException {

    private final String fieldName;

    public RecipientInvalidDataException(String fieldName) {
        super(RECIPIENT_INVALID_DATA);
        this.fieldName = fieldName;
    }

    @Override
    public String getMessageKey() {
        return RECIPIENT_INVALID_DATA;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{fieldName};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getMessage() {
        return String.format("[잘못된 데이터] fieldName=%s", fieldName);
    }
}