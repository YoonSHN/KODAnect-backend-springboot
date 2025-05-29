package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

import static kodanect.common.exception.config.MessageKeys.INVALID_BOARD_CODE;

public class InvalidBoardCodeException extends AbstractCustomException {

    private final String boardCode;

    public InvalidBoardCodeException(String boardCode) {
        super(INVALID_BOARD_CODE + boardCode);
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
