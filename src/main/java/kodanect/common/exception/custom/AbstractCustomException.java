package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

/**
 * 모든 커스텀 예외의 추상 기반 클래스.
 * 공통적으로 메시지 키, 메시지 파라미터, HTTP 상태 코드를 정의하도록 강제함.
 * <p>
 * - 메시지 키와 파라미터는 다국어 처리(i18n)에 활용
 * - HTTP 상태 코드는 REST API 응답에 직접 매핑 가능
 */
public abstract class AbstractCustomException extends RuntimeException {

    protected AbstractCustomException(String message) {
        super(message);
    }

    protected AbstractCustomException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract String getMessageKey();

    public abstract Object[] getArguments();

    public abstract HttpStatus getStatus();
}
