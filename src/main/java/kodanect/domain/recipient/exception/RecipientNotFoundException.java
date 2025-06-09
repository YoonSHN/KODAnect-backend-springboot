package kodanect.domain.recipient.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

public class RecipientNotFoundException extends AbstractCustomException {

    private static final String MESSAGE_KEY = "recipient.notfound"; // 메시지 키 정의
    private final transient Object[] arguments; // 메시지에 포함될 인자들을 저장할 필드

    // 가장 인자가 많은 생성자가 모든 초기화 로직을 담당합니다.
    public RecipientNotFoundException(String message, Object... arguments) {
        super(message); // 부모 클래스 생성자 호출
        this.arguments = (arguments != null) ? arguments : new Object[0]; // arguments가 null이 아니면 사용, 아니면 빈 배열
    }

    // 메시지만 받는 생성자는 인자를 받는 생성자를 호출합니다.
    public RecipientNotFoundException(String message) {
        this(message, new Object[0]); // 인자가 없는 경우 빈 배열을 전달
    }

    // 기본 생성자는 미리 정의된 메시지를 사용하여 메시지만 받는 생성자를 호출합니다.
    public RecipientNotFoundException() {
        this("게시글을 찾을 수 없습니다."); // 기본 메시지로 메시지 전달 생성자 호출
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
        return HttpStatus.NOT_FOUND;
    }
}
