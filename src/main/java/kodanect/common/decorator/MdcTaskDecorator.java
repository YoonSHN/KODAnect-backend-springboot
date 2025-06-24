package kodanect.common.decorator;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * 비동기 작업 실행 시 MDC(Log context)를 전파하기 위한 데코레이터
 *
 * Spring의 {@link org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor} 또는
 * {@link org.springframework.scheduling.annotation.Async} 환경에서 사용됩니다.
 *
 * 주요 역할:
 * - 현재 요청 스레드의 MDC(Context Map)를 복사
 * - 비동기 실행 대상에 해당 MDC 설정
 * - 작업 후 MDC를 명시적으로 초기화
 *
 * 주의: MDC는 ThreadLocal 기반이므로, 반드시 실행 후 clear() 처리 필수
 */
public class MdcTaskDecorator implements TaskDecorator {

    /**
     * 현재 스레드의 MDC 컨텍스트를 캡처해 비동기 작업에 전달합니다.
     * 실행 후 MDC를 반드시 초기화하여 ThreadLocal 누수를 방지합니다.
     *
     * @param runnable 비동기 실행 대상
     * @return MDC context가 설정된 Runnable
     */
    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }

}
