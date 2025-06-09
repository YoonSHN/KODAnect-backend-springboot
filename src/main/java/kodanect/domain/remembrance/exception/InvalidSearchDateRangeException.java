package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.SEARCH_DATE_RANGE_INVALID;

public class InvalidSearchDateRangeException extends AbstractCustomException {
    /* 검색 종료일이 시작일 보다 미래일 경우 */

    private final String startDate;
    private final String endDate;

    public InvalidSearchDateRangeException(String startDate, String endDate) {
        super(SEARCH_DATE_RANGE_INVALID);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public String getMessageKey() {
        return SEARCH_DATE_RANGE_INVALID;
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
