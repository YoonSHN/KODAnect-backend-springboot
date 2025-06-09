package kodanect.domain.article.exception;

import kodanect.common.exception.custom.AbstractCustomException;
import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.INVALID_BOARD_CODE;

/**
 * 유효하지 않은 게시판 코드가 전달되었을 때 발생하는 예외.
 * 예: 존재하지 않는 boardCode로 API 호출 시.
 */
public class InvalidBoardCodeException extends AbstractCustomException {

    private final String boardCode;

    public InvalidBoardCodeException(String boardCode) {
        super(INVALID_BOARD_CODE);
        this.boardCode = boardCode;
    }

    public InvalidBoardCodeException(String boardCode, Throwable cause) {
        super(INVALID_BOARD_CODE, cause);
        this.boardCode = boardCode;
    }

    @Override
    public String getMessageKey() {
        return INVALID_BOARD_CODE;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{boardCode};
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
