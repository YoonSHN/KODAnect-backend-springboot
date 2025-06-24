package kodanect.common.config.common;

import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.egovframe.rte.fdl.cmmn.trace.handler.DefaultTraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.handler.TraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.manager.DefaultTraceHandleManager;
import org.egovframe.rte.fdl.cmmn.trace.manager.TraceHandlerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.RestTemplate; // RestTemplate 임포트 추가

/**
 * 공통 설정
 *
 * 역할
 * - 메시지 리소스 처리
 * - AOP 예외/로깅 처리 패턴 지원
 * - 경로 패턴 매칭 유틸 등록
 *
 * 특징
 * - 전자정부 프레임워크 설정 구성 기반
 * - TraceHandler, LeaveaTrace 등 AOP 연동 처리 포함
 */
@Configuration
public class EgovConfigCommon {

	private static final int MESSAGE_CACHE_SECONDS = 60;

	/**
	 * 경로 매칭 유틸 Bean
	 *
	 * Ant 스타일 경로 매칭 지원
	 */
	@Bean
	public AntPathMatcher antPathMatcher() {
		return new AntPathMatcher();
	}

	/**
	 * 기본 트레이스 핸들러 Bean
	 *
	 * 예외 및 요청 흐름에 대한 로깅 처리 담당
	 */
	@Bean
	public DefaultTraceHandler defaultTraceHandler() {
		return new DefaultTraceHandler();
	}

	/**
	 * 메시지 리소스 Bean
	 *
	 * 공통 메시지 및 시스템 메시지 파일 로딩 설정
	 */
	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource = new ReloadableResourceBundleMessageSource();
		reloadableResourceBundleMessageSource.setBasenames(
				"classpath:/egovframework/message/message-common",
				"classpath:/org/egovframe/rte/fdl/idgnr/messages/idgnr",
				"classpath:/org/egovframe/rte/fdl/property/messages/properties");
		reloadableResourceBundleMessageSource.setDefaultEncoding("UTF-8");
		reloadableResourceBundleMessageSource.setCacheSeconds(MESSAGE_CACHE_SECONDS);
		reloadableResourceBundleMessageSource.setFallbackToSystemLocale(false);
		reloadableResourceBundleMessageSource.setUseCodeAsDefaultMessage(true);
		return reloadableResourceBundleMessageSource;
	}

	/**
	 * 메시지 접근기 Bean
	 *
	 * 코드에서 메시지 리소스를 쉽게 조회할 수 있도록 지원
	 */
	@Bean
	public MessageSourceAccessor messageSourceAccessor() {
		return new MessageSourceAccessor(this.messageSource());
	}

	/**
	 * 트레이스 핸들러 매니저 Bean
	 *
	 * 요청 경로 패턴과 핸들러를 바인딩하여 트레이스 로깅 처리 구성
	 */
	@Bean
	public DefaultTraceHandleManager traceHandlerService() {
		DefaultTraceHandleManager defaultTraceHandleManager = new DefaultTraceHandleManager();
		defaultTraceHandleManager.setReqExpMatcher(antPathMatcher());
		defaultTraceHandleManager.setPatterns(new String[]{"*"});
		defaultTraceHandleManager.setHandlers(new TraceHandler[]{defaultTraceHandler()});
		return defaultTraceHandleManager;
	}

	/**
	 * 트레이스 처리기 Bean
	 *
	 * TraceHandlerService 기반 트레이스 기능 활성화
	 */
	@Bean
	public LeaveaTrace leaveaTrace() {
		LeaveaTrace leaveaTrace = new LeaveaTrace();
		leaveaTrace.setTraceHandlerServices(new TraceHandlerService[]{traceHandlerService()});
		return leaveaTrace;
	}

	/**
	 * RestTemplate Bean
	 *
	 * HTTP 요청을 수행하기 위한 RestTemplate 인스턴스 등록
	 * hCaptcha 등 외부 API 호출에 사용될 수 있습니다.
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
