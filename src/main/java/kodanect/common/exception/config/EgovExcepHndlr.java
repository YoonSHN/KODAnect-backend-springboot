package kodanect.common.exception.config;

import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.exception.handler.ExceptionHandler;
import org.slf4j.MDC;

/**
 * 예외 처리 핸들러
 *
 * 서비스 레이어에서 발생한 예외를 감지하여 내부 로깅 용도로 사용
 */
@Slf4j
public class EgovExcepHndlr implements ExceptionHandler {

	@Override
	public void occur(Exception ex, String packageName) {
		String exceptionName = ex.getClass().getSimpleName();
		String threadName = Thread.currentThread().getName();

		String className = safeGetMDC("class");
		String methodName = safeGetMDC("method");
		String rootCauseMessage = getRootCauseMessage(ex);

		log.error("▼▼▼ 예외 발생 ===================================================");
		log.error("패키지명        : {}", packageName);
		log.error("예외 클래스      : {}", exceptionName);
		log.error("예외 메시지      : {}", ex.getMessage());
		log.error("Root Cause      : {}", rootCauseMessage);
		log.error("클래스          : {}", className);
		log.error("메서드          : {}", methodName);
		log.error("스레드          : {}", threadName);
		log.error("스택 트레이스   : ", ex);
		log.error("▲▲▲ 예외 처리 종료 ===============================================");
	}

	private String getRootCauseMessage(Throwable throwable) {
		Throwable root = throwable;
		while (root.getCause() != null) {
			root = root.getCause();
		}
		return root.getMessage() != null ? root.getMessage() : "(null)";
	}

	private String safeGetMDC(String key) {
		String value = MDC.get(key);
		return value != null ? value : "(MDC 미설정)";
	}
}
