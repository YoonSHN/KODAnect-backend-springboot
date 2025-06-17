package kodanect.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 로그 인젝션 방지를 위한 유틸리티 클래스
 *
 * 기능:
 * - 문자열 내 CRLF (\r, \n) 제거
 * - 객체를 JSON 문자열로 직렬화 후 CRLF 제거
 *
 */
public class LogSanitizerUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private LogSanitizerUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 문자열 내 \r, \n 제거
     *
     * @param input 사용자 입력 등 로그 출력 전 문자열
     * @return 정제된 문자열
     */
    public static String sanitize(String input) {
        return input == null ? null : input.replace("\r", "").replace("\n", "");
    }

    /**
     * 객체를 JSON 문자열로 직렬화 후 CRLF 제거
     *
     * @param obj 직렬화할 객체
     * @return 정제된 JSON 문자열, 또는 직렬화 실패 시 오류 메시지 문자열
     */
    public static Object sanitizeObject(Object obj) {
        if (obj instanceof String string) {
            return sanitize(string);
        }
        try {
            String json = objectMapper.writeValueAsString(obj);
            return sanitize(json);
        } catch (JsonProcessingException e) {
            return "Error serializing object";
        }
    }
}
