package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.HEAVEN_NOT_FOUND;

public class HeavenNotFoundException extends AbstractCustomException {

    private final Integer letterSeq;

    public HeavenNotFoundException(Integer letterSeq) {
        super(HEAVEN_NOT_FOUND);
        this.letterSeq = letterSeq;
    }

    @Override
    public String getMessageKey() {
        return HEAVEN_NOT_FOUND;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{letterSeq};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
