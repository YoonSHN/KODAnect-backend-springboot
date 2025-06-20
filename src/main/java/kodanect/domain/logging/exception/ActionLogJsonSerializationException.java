package kodanect.domain.logging.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.ACTION_LOG_JSON_SERIALIZATION_FAIL;

/**
 *  JSON 직렬화 도중 오류가 발생했을 때 사용되는 예외입니다.
 */
public class ActionLogJsonSerializationException extends AbstractCustomException {

    private final String context;

    public ActionLogJsonSerializationException(String context) {
        super(ACTION_LOG_JSON_SERIALIZATION_FAIL);
        this.context = context;
    }

    @Override
    public String getMessage() {
        return String.format("[JSON 직렬화 실패] 변환 대상: %s", context);
    }

    @Override
    public String getMessageKey() {
        return ACTION_LOG_JSON_SERIALIZATION_FAIL;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{ context };
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
