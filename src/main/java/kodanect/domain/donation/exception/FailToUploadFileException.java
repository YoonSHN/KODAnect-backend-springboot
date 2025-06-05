package kodanect.domain.donation.exception;

public class FailToUploadFileException extends RuntimeException{
    public FailToUploadFileException(String message){
        super(message);
    }
}
