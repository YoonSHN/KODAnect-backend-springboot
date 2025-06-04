package kodanect.domain.donation.exception;

public class InvalidPaginationRangeException extends RuntimeException{
    public InvalidPaginationRangeException(String message){
        super(message);
    }

}
