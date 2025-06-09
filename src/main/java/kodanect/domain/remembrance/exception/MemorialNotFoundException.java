package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.DONATE_NOT_FOUND;

public class MemorialNotFoundException extends AbstractCustomException {
    /* 기증자 추모관 게시물을 찾지 못했을 경우 */

    private final Integer donateSeq;

    public MemorialNotFoundException(Integer donateSeq) {
        super(DONATE_NOT_FOUND);
        this.donateSeq = donateSeq;
    }

    @Override
    public String getMessageKey() {
        return DONATE_NOT_FOUND;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{donateSeq};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

}
