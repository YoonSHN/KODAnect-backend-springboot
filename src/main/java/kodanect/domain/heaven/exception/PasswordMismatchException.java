package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.HEAVEN_PASSWORD_MISMATCH;

public class PasswordMismatchException extends AbstractCustomException {

    private final String passcode;

    public PasswordMismatchException(String passcode) {
        super(HEAVEN_PASSWORD_MISMATCH);
        this.passcode = passcode;
    }

    @Override
    public String getMessageKey() {
        return HEAVEN_PASSWORD_MISMATCH;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{passcode};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
