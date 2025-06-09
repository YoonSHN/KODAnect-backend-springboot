package kodanect.domain.remembrance.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.PAGINATION_INVALID;

public class InvalidPaginationRangeException extends AbstractCustomException {
    /* 페이지 범위가 잘못 됐을 경우 */

    private final Integer cursor;
    private final int size;

    public InvalidPaginationRangeException(Integer cursor, int size) {
        super(PAGINATION_INVALID);
        this.cursor = cursor;
        this.size = size;
    }

    @Override
    public String getMessageKey() {
        return PAGINATION_INVALID;
    }

    @Override
    public Object[] getArguments() {
        return new Object[] {cursor, size};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
