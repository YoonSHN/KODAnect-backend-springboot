package kodanect.domain.donation.exception;

public class ValidationFailedException extends RuntimeException{
    public ValidationFailedException(String message){
        super(message);
    }
}
