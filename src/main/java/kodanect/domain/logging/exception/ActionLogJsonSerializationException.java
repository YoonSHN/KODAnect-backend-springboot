package kodanect.domain.logging.exception;

/**
 * 로그 데이터를 JSON 문자열로 직렬화하는 과정에서 발생하는 예외입니다.
 *
 * Jackson 등의 라이브러리를 사용한 변환 중 오류가 발생할 경우 사용됩니다.
 * ex) ActionLog 객체 → JSON 문자열 변환 실패
 */
public class ActionLogJsonSerializationException extends RuntimeException {

    public ActionLogJsonSerializationException() {
        super("Failed to convert log data to JSON string.");
    }

}
