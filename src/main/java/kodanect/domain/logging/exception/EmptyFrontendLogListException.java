package kodanect.domain.logging.exception;

/**
 * 프론트엔드 로그 요청에서 로그 리스트가 비어 있을 경우 발생하는 예외입니다.
 *
 * {@link kodanect.domain.logging.dto.FrontendLogRequestDto} 내 로그 데이터가 없을 때 사용됩니다.
 */
public class EmptyFrontendLogListException extends RuntimeException {

    public EmptyFrontendLogListException() {
        super("Frontend log list must not be null or empty.");
    }

}
