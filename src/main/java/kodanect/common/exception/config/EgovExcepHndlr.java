package kodanect.common.exception.config;

import org.egovframe.rte.fdl.cmmn.exception.handler.ExceptionHandler;
import org.slf4j.MDC;

import java.util.Objects;

/**
 * 예외 처리 핸들러
 *
 * 서비스 레이어에서 발생한 예외를 감지하여 내부 로깅 용도로 사용
 *
 * 보안 대응:
 * - SecureLogger를 통해 CRLF (\r, \n) 삽입 방지
 * - 로그 출력 시 사용자 입력값 정제 자동 수행
 */
public class EgovExcepHndlr implements ExceptionHandler {

	private static final SecureLogger log = SecureLogger.getLogger(EgovExcepHndlr.class);

	@Override
	public void occur(Exception ex, String packageName) {
		final String exName      = ex.getClass().getSimpleName();
		final String threadName  = Thread.currentThread().getName();
		final String className   = getMDCOrDefault("class");
		final String methodName  = getMDCOrDefault("method");
		final String message     = Objects.toString(ex.getMessage(), "예외 메시지 없음");
		final String rootCause   = getRootCauseMessage(ex);
		final String pkg         = Objects.toString(packageName, "알 수 없음");

		log.error("[Exception] {}.{}() threw {}: {}", className, methodName, exName, message);

		log.error("─────────────────────────────────────");
		log.error("패키지명     : {}", pkg);
		log.error("예외타입     : {}", exName);
		log.error("메시지       : {}", message);
		log.error("Root Cause   : {}", rootCause);
		log.error("발생 위치    : {}.{}()", className, methodName);
		log.error("스레드       : {}", threadName);
		log.error("─────────────────────────────────────");

		// 전체 스택 트레이스
		log.error("스택 트레이스 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓", ex);
	}

	/**
	 * 최상위 원인 메시지 반환
	 */
	private String getRootCauseMessage(Throwable t) {
		while (t.getCause() != null) {
			t = t.getCause();
		}
		return Objects.toString(t.getMessage(), "원인 없음");
	}

	/**
	 * MDC 값 조회 또는 기본값 반환
	 */
	private String getMDCOrDefault(String key) {
		return Objects.toString(MDC.get(key), "MDC 없음 (" + key + ")");
	}
}
