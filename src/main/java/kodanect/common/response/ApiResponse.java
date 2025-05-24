package kodanect.common.response;

import lombok.Getter;

/**
 * API 응답의 표준 형태를 정의하는 클래스입니다.
 *
 * 응답 성공 여부, 상태 코드, 메시지, 응답 데이터를 포함하며,
 * 응답 일관성 유지를 위해 정적 팩토리 메서드로 인스턴스를 생성합니다.
 */

/**
 * API 응답 포맷
 *
 * 역할
 * - 응답 성공 여부, 상태 코드, 메시지, 데이터 포함
 * - 클라이언트 응답 포맷 통일
 *
 * 특징
 * - 정적 팩토리 메서드 기반 생성 방식 제공
 * - 성공/실패 응답 분리 생성 지원
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

    /**
     * 성공 응답 생성 (데이터 없음)
     */
    public static <T> ApiResponse<T> success(int code, String message) {
        return new ApiResponse<>(true, code, message, null);
    }

    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>(true, code, message, data);
    }

    /**
     * 실패 응답 생성 (데이터 없음)
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    /**
     * 실패 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> fail(int code, String message, T data) {
        return new ApiResponse<>(false, code, message, data);
    }
}
