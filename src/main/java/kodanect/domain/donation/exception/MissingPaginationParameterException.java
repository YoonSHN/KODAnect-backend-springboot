package kodanect.domain.donation.exception;

public class MissingPaginationParameterException extends RuntimeException{
    public MissingPaginationParameterException(String message){
        super(message);
    }
}
