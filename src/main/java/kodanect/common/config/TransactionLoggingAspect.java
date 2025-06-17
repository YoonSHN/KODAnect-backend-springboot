package kodanect.common.config;

import kodanect.common.exception.config.SecureLogger;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 트랜잭션 수행 시간 로깅 AOP
 *
 * 목적:
 * - @Transactional이 선언된 클래스 또는 메서드에 대해 트랜잭션 실행 시간을 로깅
 * - 예외 발생 시 에러 로그 출력
 *
 * 적용 대상:
 * - 클래스 또는 메서드에 @Transactional이 명시된 모든 빈
 *
 * 특징:
 * - 트랜잭션 시작/종료 시점의 로그 출력
 * - 예외 발생 시 해당 메서드 정보와 예외 메시지 함께 로깅
 */
@Aspect
@Component
@RequiredArgsConstructor
public class TransactionLoggingAspect {

    private static final SecureLogger log = SecureLogger.getLogger(TransactionLoggingAspect.class);

    /**
     * 트랜잭션 수행 전후 로그 출력
     *
     * @param joinPoint 트랜잭션 대상 메서드 정보
     * @return 원래 메서드의 실행 결과
     * @throws Throwable 원래 메서드에서 발생하는 예외 그대로 전달
     */
    @Around("@annotation(org.springframework.transaction.annotation.Transactional) || @within(org.springframework.transaction.annotation.Transactional)")
    public Object logTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = (joinPoint.getSignature() != null)
                ? joinPoint.getSignature().getName() : "UnknownMethod";

        Object target = joinPoint.getTarget();
        String className = target != null
                ? target.getClass().getSimpleName() : "UnknownClass";

        log.debug("Starting transaction for {}.{}", className, methodName);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("Transaction completed for {}.{} in {}ms", className, methodName, executionTime);
            return result;
        } catch (Exception e) {
            log.error("Transaction failed for {}.{} - {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}
