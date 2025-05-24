package kodanect.common.exception;

import lombok.Setter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.egovframe.rte.fdl.cmmn.aspect.ExceptionTransfer;

/**
 * 예외 AOP 트랜스퍼
 *
 * kodanect.domain 하위 service.impl 패키지에서 발생한 예외를 AOP 기반으로 가로채 ExceptionTransfer로 전달
 *
 * 역할
 * - service.impl 패키지 내 클래스의 메서드 실행 중 발생한 예외 감지
 * - 감지된 예외를 ExceptionTransfer로 전달
 *
 * 특징
 * - AfterThrowing 기반 예외 처리 방식 적용
 * - 트랜잭션 범위 내 예외 흐름 유지
 */
@Setter
@Aspect
public class EgovAopExceptionTransfer {

	private ExceptionTransfer exceptionTransfer;

	/**
	 * 예외 처리 대상 포인트컷
	 *
	 * kodanect.domain 하위의 모든 service.impl 패키지 내 클래스의 메서드 실행 기준으로 지정
	 */
	@Pointcut("execution(* kodanect.domain..service.impl..*(..))")
	private void exceptionTransferService() {}

	/**
	 * 예외 발생 후 처리
	 *
	 * 지정된 포인트컷에서 예외 발생 시 ExceptionTransfer로 예외 전달
	 */
	@AfterThrowing(pointcut="exceptionTransferService()", throwing="ex")
	public void doAfterThrowingExceptionTransferService(JoinPoint thisJoinPoint, Exception ex) throws Exception {
		exceptionTransfer.transfer(thisJoinPoint, ex);
	}

}
