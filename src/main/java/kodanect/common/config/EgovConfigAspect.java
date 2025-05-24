package kodanect.common.config;

import kodanect.common.exception.EgovAopExceptionTransfer;
import kodanect.common.exception.EgovExcepHndlr;
import org.egovframe.rte.fdl.cmmn.aspect.ExceptionTransfer;
import org.egovframe.rte.fdl.cmmn.exception.handler.ExceptionHandler;
import org.egovframe.rte.fdl.cmmn.exception.manager.DefaultExceptionHandleManager;
import org.egovframe.rte.fdl.cmmn.exception.manager.ExceptionHandlerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.AntPathMatcher;

/**
 * 예외 AOP 설정
 *
 * 전자정부 프레임워크의 AOP 예외 처리 체계를 Spring Boot에 등록
 * ExceptionHandler → ExceptionHandleManager → ExceptionTransfer → AOP 순으로 예외 흐름 구성
 *
 * 역할
 * - 서비스 레이어에서 발생한 예외를 공통 예외 핸들러로 위임
 * - 트랜잭션 범위 내에서 예외 전파 유지
 *
 * 특징
 * - 실제 예외 처리 책임은 ExceptionTransfer가 위임
 * - 특정 패키지 기준으로 예외 핸들러 적용
 */
@Configuration
@EnableAspectJAutoProxy
public class EgovConfigAspect {

	/**
	 * 사용자 정의 예외 핸들러 Bean
	 *
	 * 예외 발생 시 패키지명과 메시지를 로그로 출력
	 */
    @Bean
	public EgovExcepHndlr egovEgovExcepHndlr() {
		return new EgovExcepHndlr();
	}

	/**
	 * 예외 처리 매니저 Bean
	 *
	 * 지정된 서비스 구현체 패키지에서 발생한 예외를 EgovExcepHndlr로 위임 처리
	 * 패턴 매칭은 AntPathMatcher 기반으로 수행
	 *
	 * 적용 대상
	 * - donation.service.impl
	 * - heaven.service.impl
	 * - info.service.impl
	 * - notice.service.impl
	 * - organ.service.impl
	 * - recipient.service.impl
	 * - remembrance.service.impl
	 */
	@Bean
	public DefaultExceptionHandleManager defaultExceptionHandleManager(
			AntPathMatcher antPathMatcher, EgovExcepHndlr egovExcepHndlr) {
		DefaultExceptionHandleManager defaultExceptionHandleManager = new DefaultExceptionHandleManager();
		defaultExceptionHandleManager.setReqExpMatcher(antPathMatcher);
		defaultExceptionHandleManager.setPatterns(new String[]{
			"**donation.service.impl.*",
			"**heaven.service.impl.*",
			"**info.service.impl.*",
			"**notice.service.impl.*",
			"**organ.service.impl.*",
			"**recipient.service.impl.*",
			"**remembrance.service.impl.*"
		});
		defaultExceptionHandleManager.setHandlers(new ExceptionHandler[]{
			egovExcepHndlr
		});
		return defaultExceptionHandleManager;
	}

	/**
	 * 예외 전달기 Bean
	 *
	 * 예외 처리 매니저 목록을 받아 예외를 핸들러에게 위임 전달
	 */
	@Bean
	public ExceptionTransfer exceptionTransfer(
		@Qualifier("defaultExceptionHandleManager") DefaultExceptionHandleManager defaultExceptionHandleManager) {
		ExceptionTransfer exceptionTransfer = new ExceptionTransfer();
		exceptionTransfer.setExceptionHandlerService(new ExceptionHandlerService[] {
			defaultExceptionHandleManager
		});
		return exceptionTransfer;
	}

	/**
	 * AOP 예외 전파 Bean
	 *
	 * ExceptionTransfer 기반으로 예외를 AOP 방식으로 전파
	 */
	@Bean
	public EgovAopExceptionTransfer aopExceptionTransfer(ExceptionTransfer exceptionTransfer) {
		EgovAopExceptionTransfer egovAopExceptionTransfer = new EgovAopExceptionTransfer();
		egovAopExceptionTransfer.setExceptionTransfer(exceptionTransfer);
		return egovAopExceptionTransfer;
	}

}
