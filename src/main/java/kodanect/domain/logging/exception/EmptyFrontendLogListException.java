package kodanect.domain.logging.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.FRONTEND_LOG_LIST_EMPTY;

/**
 * 프론트엔드 로그 요청에서 로그 리스트가 비어 있을 경우 발생하는 예외입니다.
 *
 * {@link kodanect.domain.logging.dto.FrontendLogRequestDto} 내 로그 데이터가 없을 때 사용됩니다.
 */
public class EmptyFrontendLogListException extends AbstractCustomException {

    public EmptyFrontendLogListException() {
        super(FRONTEND_LOG_LIST_EMPTY);
    }

    @Override
    public String getMessage() {
        return "[프론트엔드 로그 오류] 로그 리스트가 비어 있습니다. 최소 1개 이상의 로그가 필요합니다.";
    }

    @Override
    public String getMessageKey() {
        return FRONTEND_LOG_LIST_EMPTY;
    }

    @Override
    public Object[] getArguments() {
        return new Object[0];
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
