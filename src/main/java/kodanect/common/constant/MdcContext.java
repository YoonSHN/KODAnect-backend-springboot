package kodanect.common.constant;

import org.slf4j.MDC;

import java.util.Optional;

/**
 * SLF4J MDC에 저장된 컨텍스트 정보를 읽기 전용으로 제공하는 유틸리티 클래스입니다.
 *
 * 모든 키에 대해 null-safe 하게 접근할 수 있으며, 값이 존재하지 않을 경우 "Unknown"을 반환합니다.
 */
public final class MdcContext {

    /**
     * 인스턴스화를 방지하기 위한 private 생성자
     * 호출 시 예외를 발생시킵니다.
     */
    private MdcContext() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 지정한 MDC 키의 값을 반환합니다.
     * 값이 없거나 null이면 "Unknown"을 반환합니다.
     *
     * @param key MDC key
     * @return 저장된 값 또는 "Unknown"
     */
    public static String get(String key) {
        return Optional.ofNullable(MDC.get(key)).orElse("Unknown");
    }

    /**
     * @return 클라이언트 IP 주소
     */
    public static String getIpAddress() {
        return get(MdcKey.IP_ADDRESS);
    }

    /**
     * @return 사용자 세션 ID
     */
    public static String getSessionId() {
        return get(MdcKey.SESSION_ID);
    }

    /**
     * @return HTTP 메서드
     */
    public static String getHttpMethod() {
        return get(MdcKey.HTTP_METHOD);
    }

    /**
     * @return 요청된 엔드포인트
     */
    public static String getEndpoint() {
        return get(MdcKey.ENDPOINT);
    }

    /**
     * @return 실행된 컨트롤러 클래스 이름
     */
    public static String getController() {
        return get(MdcKey.CONTROLLER);
    }

    /**
     * @return 실행된 메서드 이름
     */
    public static String getMethod() {
        return get(MdcKey.METHOD);
    }

    /**
     * @return 요청 파라미터
     */
    public static String getParameters() {
        return get(MdcKey.PARAMETERS);
    }

    /**
     * @return 요청 발생 시각
     */
    public static String getTimestamp() {
        return get(MdcKey.TIMESTAMP);
    }

    /**
     * @return 브라우저 이름
     */
    public static String getBrowserName() {
        return get(MdcKey.BROWSER_NAME);
    }

    /**
     * @return 브라우저 버전
     */
    public static String getBrowserVersion() {
        return get(MdcKey.BROWSER_VERSION);
    }

    /**
     * @return 운영 체제 이름
     */
    public static String getOperatingSystem() {
        return get(MdcKey.OPERATING_SYSTEM);
    }

    /**
     * @return 디바이스 종류
     */
    public static String getDevice() {
        return get(MdcKey.DEVICE);
    }

    /**
     * @return 사용자 로케일
     */
    public static String getLocale() {
        return get(MdcKey.LOCALE);
    }

}
