package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

public class InvalidIntegerConversionException extends AbstractCustomException {

    private static final String MESSAGE_KEY = "common.invalid.integer.conversion"; // 적절한 메시지 키 정의
    private final transient Object[] arguments;

    // 가장 인자가 많은 생성자가 모든 초기화 로직을 담당합니다.
    public InvalidIntegerConversionException(String message, Object... arguments) {
        super(message); // 부모 클래스 생성자 호출
        this.arguments = (arguments != null) ? arguments : new Object[0]; // arguments가 null이 아니면 사용, 아니면 빈 배열
    }

    // 메시지만 받는 생성자는 인자를 받는 생성자를 호출합니다.
    public InvalidIntegerConversionException(String message) {
        this(message, new Object[0]); // 인자가 없는 경우 빈 배열을 전달
    }

    // 기본 생성자는 미리 정의된 메시지를 사용하여 메시지만 받는 생성자를 호출합니다.
    public InvalidIntegerConversionException() {
        this("유효하지 않은 숫자 형식입니다.");
    }

    @Override
    public String getMessageKey() {
        return MESSAGE_KEY;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST; // 400 Bad Request
    }
}
