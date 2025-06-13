package kodanect.domain.donation.exception;

public class PasscodeMismatchException extends RuntimeException{
    public PasscodeMismatchException(String message){
        super(message);
    }
}
