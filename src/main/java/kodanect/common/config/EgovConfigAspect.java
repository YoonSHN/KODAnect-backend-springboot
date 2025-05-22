package kodanect.common.config;

import kodanect.common.exception.EgovAopExceptionTransfer;
import kodanect.common.exception.EgovInternalServerExceptionHandler;
import kodanect.common.exception.EgovNotFoundExceptionHandler;
import org.egovframe.rte.fdl.cmmn.aspect.ExceptionTransfer;
import org.egovframe.rte.fdl.cmmn.exception.handler.ExceptionHandler;
import org.egovframe.rte.fdl.cmmn.exception.manager.DefaultExceptionHandleManager;
import org.egovframe.rte.fdl.cmmn.exception.manager.ExceptionHandlerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.AntPathMatcher;

@Configuration
@EnableAspectJAutoProxy
public class EgovConfigAspect {

	@Bean
	public EgovNotFoundExceptionHandler egovNotFoundExceptionHandler() {
		return new EgovNotFoundExceptionHandler();
	}

	@Bean
	public EgovInternalServerExceptionHandler egovInternalServerExceptionHandler() {
		return new EgovInternalServerExceptionHandler();
	}

	@Bean
	public DefaultExceptionHandleManager defaultExceptionHandleManager(
			AntPathMatcher antPathMatcher, EgovNotFoundExceptionHandler egovNotFoundExceptionHandler,
			EgovInternalServerExceptionHandler egovInternalServerExceptionHandler) {
		DefaultExceptionHandleManager defaultExceptionHandleManager = new DefaultExceptionHandleManager();
		defaultExceptionHandleManager.setReqExpMatcher(antPathMatcher);
		defaultExceptionHandleManager.setPatterns(new String[]{
			"**info.service.impl.*",
			"**notice.service.impl.*",
			"**organ.service.impl.*",
			"**remembrance.service.impl.*"
		});
		defaultExceptionHandleManager.setHandlers(new ExceptionHandler[]{
			egovNotFoundExceptionHandler,
			egovInternalServerExceptionHandler
		});
		return defaultExceptionHandleManager;
	}

	@Bean
	public ExceptionTransfer exceptionTransfer(
		@Qualifier("defaultExceptionHandleManager") DefaultExceptionHandleManager defaultExceptionHandleManager) {
		ExceptionTransfer exceptionTransfer = new ExceptionTransfer();
		exceptionTransfer.setExceptionHandlerService(new ExceptionHandlerService[] {
			defaultExceptionHandleManager
		});
		return exceptionTransfer;
	}

	@Bean
	public EgovAopExceptionTransfer aopExceptionTransfer(ExceptionTransfer exceptionTransfer) {
		EgovAopExceptionTransfer egovAopExceptionTransfer = new EgovAopExceptionTransfer();
		egovAopExceptionTransfer.setExceptionTransfer(exceptionTransfer);
		return egovAopExceptionTransfer;
	}

}
