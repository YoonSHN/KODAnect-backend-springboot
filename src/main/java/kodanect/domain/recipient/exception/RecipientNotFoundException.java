package kodanect.domain.recipient.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.RECIPIENT_NOT_FOUND;

public class RecipientNotFoundException extends AbstractCustomException {

    private final Integer letterSeq;

    public RecipientNotFoundException(String message, Integer letterSeq) {
        super(message);
        this.letterSeq = letterSeq;
    }

    @Override
    public String getMessageKey() {
        return RECIPIENT_NOT_FOUND;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{letterSeq};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getMessage() {
        return String.format("[수령 대상 없음] recipientId=%d", letterSeq);
    }
}