package kodanect.domain.donation.exception;

public class InvalidPaginationFormatException extends RuntimeException{
    public InvalidPaginationFormatException(String message){
        super(message);
    }
}
