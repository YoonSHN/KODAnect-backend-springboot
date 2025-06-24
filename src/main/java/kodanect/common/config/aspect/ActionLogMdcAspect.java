package kodanect.common.config.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.bitwalker.useragentutils.*;
import kodanect.common.exception.config.SecureLogger;
import kodanect.common.constant.MdcKey;
import kodanect.domain.logging.exception.ActionLogJsonSerializationException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 컨트롤러 계층 진입 시 사용자 요청 메타데이터를 MDC에 설정하는 AOP 컴포넌트
 *
 * 주요 기능:
 * - 세션 쿠키(sessionId)가 존재하는 요청에 한해 MDC 설정
 * - User-Agent 분석을 통해 브라우저, OS, 디바이스 정보 수집
 * - 클라이언트 IP, HTTP 메서드, 엔드포인트, 컨트롤러명, 메서드명, 파라미터, 타임스탬프 저장
 * - 수집된 정보를 SLF4J MDC에 등록
 * - 모든 작업이 완료된 후에는 MDC가 반드시 초기화됩니다.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class ActionLogMdcAspect {

    private static final String SESSION_ID_COOKIE_NAME = "sessionId";
    private static final SecureLogger log = SecureLogger.getLogger(ActionLogMdcAspect.class);
    private final ObjectMapper objectMapper;

    /**
     * 컨트롤러 메서드 실행 전후로 MDC 메타데이터를 설정하고 정리합니다.
     *
     * 세션 쿠키(sessionId)가 존재하는 요청에 대해서만 MDC를 설정합니다.
     *
     * @param joinPoint 현재 실행 중인 컨트롤러 메서드 조인 포인트
     * @return 원래의 메서드 실행 결과
     * @throws Throwable 내부 메서드 실행 중 발생한 예외
     */
    @Around("execution(* kodanect.domain..controller..*(..))")
    public Object injectMdcMetadata(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String sessionId = extractSessionIdFromCookie(request);

        if (sessionId == null || sessionId.isBlank()) {
            return joinPoint.proceed();
        }

        try {
            String ipAddress = orUnknown(extractClientIp(request));
            String httpMethod = orUnknown(request.getMethod());
            String endpoint = orUnknown(request.getRequestURI());
            String controllerClassName = joinPoint.getSignature().getDeclaringTypeName();
            String controllerSimpleName = joinPoint.getSignature().getDeclaringType().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            Map<String, String> params = extractParameters(joinPoint);
            String parametersJson = objectMapper.writeValueAsString(params);
            String userAgentString = request.getHeader("User-Agent");
            UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
            Version browserVersion = userAgent.getBrowserVersion();
            OperatingSystem os = userAgent.getOperatingSystem();
            String locale = orUnknown(request.getLocale().toLanguageTag());

            MDC.put(MdcKey.SESSION_ID, sessionId);
            MDC.put(MdcKey.IP_ADDRESS, ipAddress);
            MDC.put(MdcKey.HTTP_METHOD, httpMethod);
            MDC.put(MdcKey.ENDPOINT, endpoint);
            MDC.put(MdcKey.CONTROLLER, controllerClassName);
            MDC.put(MdcKey.METHOD, methodName);
            MDC.put(MdcKey.PARAMETERS, parametersJson);
            MDC.put(MdcKey.TIMESTAMP, Instant.now().toString());
            MDC.put(MdcKey.BROWSER_NAME, orUnknown(userAgent.getBrowser().getName()));
            MDC.put(MdcKey.BROWSER_VERSION, orUnknown(browserVersion != null ? browserVersion.getVersion() : null));
            MDC.put(MdcKey.OPERATING_SYSTEM, orUnknown(os != null ? os.getName() : null));
            MDC.put(MdcKey.DEVICE, orUnknown(os != null ? os.getDeviceType().getName() : null));
            MDC.put(MdcKey.LOCALE, locale);

            log.info("[{}] {}.{} 호출 (세션: {}, IP: {}, 파라미터: {})",
                    httpMethod, controllerSimpleName, methodName, sessionId, ipAddress, parametersJson);

            return joinPoint.proceed();
        } catch (JsonProcessingException e) {
            throw new ActionLogJsonSerializationException("MDC 파라미터 추출");
        } finally {
            MDC.clear();
        }
    }

    /**
     * HttpServletRequest의 쿠키에서 sessionId 값을 추출합니다.
     *
     * @param request 현재 요청
     * @return sessionId 쿠키 값 (없으면 null)
     */
    private String extractSessionIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (SESSION_ID_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * AOP 조인 포인트로부터 메서드 파라미터 이름과 값을 추출합니다.
     *
     * - HttpServletRequest 타입의 파라미터는 제외되지 않고 toString 처리됩니다.
     * - 각 파라미터 값은 문자열로 변환되며, null 값은 "null", toString 실패 시 "NON_STRINGIFIABLE"로 대체됩니다.
     *
     * @param joinPoint 현재 실행 중인 컨트롤러 메서드 조인 포인트
     * @return 파라미터 이름과 문자열 값의 Map
     */
    private Map<String, String> extractParameters(ProceedingJoinPoint joinPoint) {
        Map<String, String> paramMap = new HashMap<>();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] names = signature.getParameterNames();
        Object[] values = joinPoint.getArgs();

        for (int i = 0; i < names.length; i++) {
            Object value = values[i];

            String stringValue;

            try {
                stringValue = (value != null) ? value.toString() : "null";
            } catch (Exception e) {
                stringValue = "NON_STRINGIFIABLE";
            }

            paramMap.put(names[i], stringValue);
        }
        return paramMap;
    }

    /**
     * 클라이언트의 IP 주소를 추출합니다.
     *
     * X-Forwarded-For 헤더가 존재하면 해당 값을 우선 사용하고,
     * 없을 경우 request의 remote address를 사용합니다.
     *
     * @param request HttpServletRequest 객체
     * @return 추출된 IP 주소 문자열
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    /**
     * null이거나 공백 문자열일 경우 "Unknown"을 반환합니다.
     *
     * @param value 원래의 값
     * @return 유효한 문자열 또는 "Unknown"
     */
    private String orUnknown(Object value) {
        return (value != null && !value.toString().isBlank()) ? value.toString() : "Unknown";
    }

}
