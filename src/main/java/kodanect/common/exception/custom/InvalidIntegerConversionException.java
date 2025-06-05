package kodanect.common.exception.custom;

public class InvalidIntegerConversionException extends RuntimeException{
    public InvalidIntegerConversionException(String message) {
        super(message);
    }

    public InvalidIntegerConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
