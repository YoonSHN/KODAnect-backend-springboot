package kodanect.domain.recipient.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.RECIPIENT_INVALID_PASSCODE;

public class RecipientInvalidPasscodeException extends AbstractCustomException {

    private final String commentId;

    public RecipientInvalidPasscodeException(String commentId) {
        super(RECIPIENT_INVALID_PASSCODE);
        this.commentId = commentId;
    }

    @Override
    public String getMessageKey() {
        return RECIPIENT_INVALID_PASSCODE;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{commentId};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String getMessage() {
        return String.format("[비밀번호 불일치] commentId=%s", commentId);
    }
}