package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.INVALID_BOARD_CODE;

public class InvalidBoardCodeException extends AbstractCustomException {

    private final String boardCode;

    public InvalidBoardCodeException(String boardCode) {
        super("없는 게시판 코드: " + boardCode);
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
