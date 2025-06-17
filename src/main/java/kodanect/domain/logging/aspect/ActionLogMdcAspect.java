package kodanect.domain.logging.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.bitwalker.useragentutils.*;
import kodanect.domain.logging.constant.MdcKey;
import kodanect.domain.logging.exception.ActionLogJsonSerializationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 컨트롤러 계층의 진입 지점에서 사용자 요청에 대한 메타데이터를 MDC에 설정하는 AOP 컴포넌트
 *
 * 주요 기능:
 * - X-Session-Id가 존재하는 요청에 한해 동작
 * - User-Agent 분석을 통해 브라우저, OS, 디바이스 정보 수집
 * - 클라이언트 IP, HTTP 메서드, 엔드포인트, 컨트롤러, 메서드명, 파라미터, 타임스탬프 저장
 * - 수집된 정보를 SLF4J MDC에 등록함
 *
 * 작업 완료 후 MDC는 반드시 초기화됩니다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ActionLogMdcAspect {

    private final ObjectMapper objectMapper;

    /**
     * 컨트롤러 메서드 실행 전후로 MDC 메타데이터를 설정하고 정리합니다.
     *
     * X-Session-Id 헤더가 존재하는 요청에 대해서만 MDC를 설정합니다.
     *
     * @param joinPoint 현재 실행 중인 컨트롤러 메서드 조인 포인트
     * @return 원래의 메서드 실행 결과
     * @throws Throwable 내부 메서드 실행 중 발생하는 예외
     */
    @Around("execution(* kodanect.domain..controller..*(..))")
    public Object injectMdcMetadata(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String sessionId = request.getHeader("X-Session-Id");

        if (sessionId == null || sessionId.isBlank()) {
            return joinPoint.proceed();
        }

        try {
            String userAgentString = request.getHeader("User-Agent");
            UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
            Version browserVersion = userAgent.getBrowserVersion();
            OperatingSystem os = userAgent.getOperatingSystem();

            Map<String, Object> params = extractParameters(joinPoint);

            MDC.put(MdcKey.IP_ADDRESS, orUnknown(extractClientIp(request)));
            MDC.put(MdcKey.SESSION_ID, sessionId);
            MDC.put(MdcKey.HTTP_METHOD, orUnknown(request.getMethod()));
            MDC.put(MdcKey.ENDPOINT, orUnknown(request.getRequestURI()));
            MDC.put(MdcKey.CONTROLLER, orUnknown(joinPoint.getSignature().getDeclaringTypeName()));
            MDC.put(MdcKey.METHOD, orUnknown(joinPoint.getSignature().getName()));
            MDC.put(MdcKey.PARAMETERS, objectMapper.writeValueAsString(params));
            MDC.put(MdcKey.TIMESTAMP, Instant.now().toString());
            MDC.put(MdcKey.BROWSER_NAME, orUnknown(userAgent.getBrowser().getName()));
            MDC.put(MdcKey.BROWSER_VERSION, orUnknown(browserVersion != null ? browserVersion.getVersion() : null));
            MDC.put(MdcKey.OPERATING_SYSTEM, orUnknown(os != null ? os.getName() : null));
            MDC.put(MdcKey.DEVICE, orUnknown(os != null ? os.getDeviceType().getName() : null));
            MDC.put(MdcKey.LOCALE, orUnknown(request.getLocale().toLanguageTag()));

            return joinPoint.proceed();
        } catch (JsonProcessingException e) {
            throw new ActionLogJsonSerializationException();
        } finally {
            MDC.clear();
        }
    }

    /**
     * AOP 조인 포인트의 메서드 파라미터 이름과 값을 추출합니다.
     * HttpServletRequest 타입의 파라미터는 제외됩니다.
     *
     * @param joinPoint 현재 실행 중인 컨트롤러 메서드 조인 포인트
     * @return 파라미터 이름과 값의 Map
     */
    private Map<String, Object> extractParameters(ProceedingJoinPoint joinPoint) {
        Map<String, Object> paramMap = new HashMap<>();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] names = signature.getParameterNames();
        Object[] values = joinPoint.getArgs();

        for (int i = 0; i < names.length; i++) {
            Object value = values[i];
            if (value instanceof HttpServletRequest) {
                continue;
            }
            paramMap.put(names[i], value);
        }
        return paramMap;
    }

    /**
     * 클라이언트의 IP 주소를 추출합니다.
     *
     * X-Forwarded-For 헤더가 존재하면 해당 값을 우선 사용하고,
     * 없으면 request의 remote address를 사용합니다.
     *
     * @param request HttpServletRequest 객체
     * @return 추출된 IP 주소 문자열
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    /**
     * null 또는 공백 문자열일 경우 "Unknown"을 반환합니다.
     *
     * @param value 원래의 값
     * @return 유효한 문자열 또는 "Unknown"
     */
    private String orUnknown(Object value) {
        return (value != null && !value.toString().isBlank()) ? value.toString() : "Unknown";
    }

}
