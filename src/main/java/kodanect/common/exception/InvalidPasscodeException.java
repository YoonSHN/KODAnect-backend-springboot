package kodanect.common.exception;

public class InvalidPasscodeException extends IllegalArgumentException {
    public InvalidPasscodeException(String message) {
        super(message);
    }
}
