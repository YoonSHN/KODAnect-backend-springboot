package kodanect.common.constant;

/**
 * SLF4J MDC에 사용되는 키 값들을 정의한 상수 클래스입니다.
 *
 * 이 키들은 로그 필드 식별 및 필터링/추적용으로 사용됩니다.
 *
 */
public final class MdcKey {

    /**
     * 클라이언트 IP 주소
     */
    public static final String IP_ADDRESS = "ipAddress";

    /**
     * 사용자 세션 ID
     */
    public static final String SESSION_ID = "sessionId";

    /**
     * HTTP 메서드
     */
    public static final String HTTP_METHOD = "httpMethod";

    /**
     * 요청된 엔드포인트
     */
    public static final String ENDPOINT = "endpoint";

    /**
     * 실행된 컨트롤러 클래스 이름
     */
    public static final String CONTROLLER = "controller";

    /**
     * 실행된 메서드 이름
     */
    public static final String METHOD = "method";

    /**
     * 요청 파라미터
     */
    public static final String PARAMETERS = "parameters";

    /**
     * 요청 발생 시각
     */
    public static final String TIMESTAMP = "timestamp";

    /**
     * 브라우저 이름
     */
    public static final String BROWSER_NAME = "browserName";

    /**
     * 브라우저 버전
     */
    public static final String BROWSER_VERSION = "browserVersion";

    /**
     * 운영 체제 이름
     */
    public static final String OPERATING_SYSTEM = "operatingSystem";

    /**
     * 디바이스 종류
     */
    public static final String DEVICE = "device";

    /**
     * 사용자 로케일
     */
    public static final String LOCALE = "locale";

    /**
     * 인스턴스화를 방지하기 위한 private 생성자
     * 호출 시 예외를 발생시킵니다.
     */
    private MdcKey() {
        throw new UnsupportedOperationException("Utility class");
    }

}
