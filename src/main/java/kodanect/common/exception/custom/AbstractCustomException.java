package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

public abstract class AbstractCustomException extends RuntimeException {

    protected AbstractCustomException(String message) {
        super(message);
    }

    public abstract String getMessageKey();

    public abstract Object[] getArguments();

    public abstract HttpStatus getStatus();
}
