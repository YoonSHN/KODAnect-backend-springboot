package kodanect.domain.heaven.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import kodanect.domain.remembrance.entity.Memorial;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.HEAVEN_DONOR_INFORM_MISMATCH;

public class DonorInformationMismatchException extends AbstractCustomException {

    private final String donorName;
    private final transient Memorial memorial;

    public DonorInformationMismatchException(String donorName, Memorial memorial) {
        super(HEAVEN_DONOR_INFORM_MISMATCH);
        this.donorName = donorName;
        this.memorial = memorial;
    }

    @Override
    public String getMessageKey() {
        return HEAVEN_DONOR_INFORM_MISMATCH;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{donorName, memorial};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
