package kodanect.common.exception.custom;

import lombok.Getter;

import static kodanect.common.exception.config.MessageKeys.INVALID_BOARD_OPTION;

@Getter
public class InvalidBoardOptionException extends RuntimeException {

    private final String paramValue;

    public InvalidBoardOptionException(String paramValue) {
        super(INVALID_BOARD_OPTION);
        this.paramValue = paramValue;
    }

    public String getMessageKey() {
        return INVALID_BOARD_OPTION;
    }

    public Object[] getArguments() {
        return new Object[]{paramValue};
    }
}
