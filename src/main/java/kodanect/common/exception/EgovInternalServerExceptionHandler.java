package kodanect.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.exception.handler.ExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class EgovInternalServerExceptionHandler implements ExceptionHandler {

	@Override
	public void occur(Exception ex, String packageName) {
		if (ex instanceof ResponseStatusException responseStatusException &&
				responseStatusException.getStatus() == HttpStatus.NOT_FOUND) {
			return;
		}

		log.error("##### [500 INTERNAL ERROR] 예외 발생 in Package: {}", packageName);
		log.error("##### 예외 메시지: {}", ex.getMessage(), ex);
	}

}
