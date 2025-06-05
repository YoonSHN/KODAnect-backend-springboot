package kodanect.domain.recipient.exception;

public class RecipientInvalidPasscodeException extends IllegalArgumentException {
    public RecipientInvalidPasscodeException(String message) {
        super(message);
    }
}
