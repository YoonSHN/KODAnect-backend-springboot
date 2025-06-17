package kodanect.common.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * 서비스 계층 메서드 실행 시 클래스명, 메서드명을 MDC에 저장하는 로깅 AOP
 *
 * 목적:
 * - 로그 패턴에 클래스명 및 메서드명을 삽입하기 위한 MDC 설정
 * - 예외 발생 시 어느 클래스/메서드에서 발생했는지 로그 상에서 구분 가능
 *
 * 특징:
 * - 모든 서비스 로직(@Service) 내부 메서드 실행 전/후 자동 수행
 * - 반드시 `finally` 블록에서 MDC를 clear() 하여 누적 방지
 */
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* kodanect.domain..service..*(..))")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();

            MDC.put("class", className);
            MDC.put("method", methodName);

            return joinPoint.proceed();
        } finally {
            MDC.clear();
        }
    }
}
