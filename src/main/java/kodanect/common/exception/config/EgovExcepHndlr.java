package kodanect.common.exception.config;

import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.exception.handler.ExceptionHandler;

/**
 * 예외 처리 핸들러
 *
 * 전자정부 프레임워크의 예외 처리 인터페이스 구현 클래스
 * AOP 설정(EgovConfigAspect)에 등록되어 예외 발생 시 로그 출력 용도로 사용
 *
 * 역할
 * - 서비스 레이어에서 발생한 예외의 패키지명과 메시지를 로그로 출력
 *
 * 특징
 * - 클라이언트 응답 처리와 무관
 * - 내부 로깅 용도로만 사용
 */
@Slf4j
public class EgovExcepHndlr implements ExceptionHandler {

	/**
	 * 예외 발생 시 호출
	 *
	 * 예외 메시지와 패키지명을 로그로 출력
	 */
	@Override
	public void occur(Exception ex, String packageName) {
		log.error("##### 예외 발생 패키지: {}", packageName);
		log.error("##### 예외 메시지: {}", ex.getMessage(), ex);
	}

}
