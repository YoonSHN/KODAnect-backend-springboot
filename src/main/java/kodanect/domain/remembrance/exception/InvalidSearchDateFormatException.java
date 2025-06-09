package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.SEARCH_DATE_FORMAT_INVALID;

/** 검색 시작일 또는 종료일의 형식이 올바르지 않을 경우 발생하는 예외 */
public class InvalidSearchDateFormatException extends AbstractCustomException {

    private final String startDate;
    private final String endDate;

    public InvalidSearchDateFormatException(String startDate, String endDate) {
        super(SEARCH_DATE_FORMAT_INVALID);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public String getMessageKey() {
        return SEARCH_DATE_FORMAT_INVALID;
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
