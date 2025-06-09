package kodanect.domain.recipient.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

public class RecipientNotFoundException extends AbstractCustomException {

    private static final String MESSAGE_KEY = "recipient.notfound"; // 메시지 키 정의
    private final Object[] arguments; // 메시지에 포함될 인자들을 저장할 필드

    // 기본 생성자
    public RecipientNotFoundException() {
        this("게시글을 찾을 수 없습니다.");
    }

    // 메시지 전달 생성자
    public RecipientNotFoundException(String message) {
        super(message);
        this.arguments = new Object[0]; // 인자 없는 경우 빈 배열
    }

    // 메시지와 인자를 함께 전달하는 생성자 (예: recipient.notfound.id={0})
    public RecipientNotFoundException(String message, Object... arguments) {
        super(message);
        this.arguments = arguments;
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
