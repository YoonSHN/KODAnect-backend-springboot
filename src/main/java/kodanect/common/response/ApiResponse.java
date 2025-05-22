package kodanect.common.response;

import lombok.Getter;

/**
 * API 응답의 표준 형태를 정의하는 클래스입니다.
 *
 * 응답 성공 여부, 상태 코드, 메시지, 응답 데이터를 포함하며,
 * 응답 일관성 유지를 위해 정적 팩토리 메서드로 인스턴스를 생성합니다.
 */
@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final int code;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, int code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(int code, String message) {
        return new ApiResponse<>(true, code, message, null);
    }

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>(true, code, message, data);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    public static <T> ApiResponse<T> fail(int code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }
}
