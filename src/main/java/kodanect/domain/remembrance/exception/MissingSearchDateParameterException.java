package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.SEARCH_DATE_MISSING;

/** 검색 시작일 또는 종료일이 누락 됐을 경우 발생하는 예외 */
public class MissingSearchDateParameterException extends AbstractCustomException {

    private final String startDate;
    private final String endDate;

    public MissingSearchDateParameterException(String startDate, String endDate) {
        super(SEARCH_DATE_MISSING);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public String getMessageKey() {
        return SEARCH_DATE_MISSING;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{startDate, endDate};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
