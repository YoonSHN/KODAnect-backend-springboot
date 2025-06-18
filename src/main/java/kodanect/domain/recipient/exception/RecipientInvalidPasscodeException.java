package kodanect.domain.recipient.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.RECIPIENT_INVALID_PASSCODE;

public class RecipientInvalidPasscodeException extends AbstractCustomException {

    private final Integer commentId;
    private final transient Object inputData;  // RecipientRequestDto 또는 RecipientCommentUpdateRequestDto를 담을 수 있도록 Object로 변경

    /**
     * 비밀번호 불일치 예외 생성자 (주로 비밀번호 검증 API에서 사용되며, 입력 데이터 반환이 필요 없을 때)
     *
     * @param commentId 게시물 ID (letterSeq) 또는 댓글 ID (commentSeq)
     */
    public RecipientInvalidPasscodeException(Integer commentId) {
        super(RECIPIENT_INVALID_PASSCODE);
        this.commentId = commentId;
        this.inputData = null;
    }

    // 비밀번호 불일치 메시지와 함께 사용자가 입력한 게시글 내용을 전달하는 생성자
    public RecipientInvalidPasscodeException(String message, Object inputData) {
        super(message);         // 전달받은 메시지를 사용 (RECIPIENT_INVALID_PASSCODE 메시지 키와는 별개)
        this.commentId = null;
        this.inputData = inputData;
    }

    @Override
    public String getMessageKey() {
        return RECIPIENT_INVALID_PASSCODE;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{commentId};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String getMessage() {
        // inputData가 있으면 특정 메시지를 반환하여 프론트엔드가 입력 데이터를 받을 수 있음을 알림
        if (inputData != null) {
            return "비밀번호가 일치하지 않습니다. (입력 데이터 포함)";
        }
        // inputData가 없으면 (즉, 단순 비밀번호 확인 실패 시) 리소스 ID를 포함한 메시지 반환
        return String.format("[비밀번호 불일치] 리소스 ID=%s", commentId);
    }

    // 사용자가 입력한 게시글 내용을 반환하는 getter
    public Object getInputData() {
        return inputData;
    }
}