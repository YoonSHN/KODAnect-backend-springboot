package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.CONTENTS_INVALID;

public class InvalidContentsException extends AbstractCustomException {

    private final Integer donateSeq;
    private final String donorName;
    private final String genderFlag;
    private final String donateDate;
    private final Integer donateAge;

    public InvalidContentsException(Integer donateSeq, String donorName, String genderFlag, String donateDate, Integer donateAge) {
        super(CONTENTS_INVALID);
        this.donateSeq = donateSeq;
        this.donorName = donorName;
        this.genderFlag = genderFlag;
        this.donateDate = donateDate;
        this.donateAge = donateAge;
    }

    @Override
    public String getMessageKey() {
        return CONTENTS_INVALID;
    }

    @Override
    public Object[] getArguments() {
        return new Object[] {donateSeq, donorName, genderFlag, donateDate, donateAge};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
