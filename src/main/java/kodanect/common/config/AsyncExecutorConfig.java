package kodanect.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;



/**
 * 비동기 작업(예: 로그 저장 등)을 처리하기 위한 Executor 설정 클래스 필요하면 추가 등록해서 사용하시면 됩니다.
 *
 * 현재 인스턴스 성능을 고려해서 제한적인 스레드 사용 성능이 부족해 병렬처리보단 작업분리목적
 */
@Configuration
public class AsyncExecutorConfig {

    private static final int LOG_EXECUTOR_CORE_POOL_SIZE = 2;
    private static final int LOG_EXECUTOR_MAX_POOL_SIZE = 2;
    private static final int LOG_EXECUTOR_QUEUE_CAPACITY = 100;

    @Bean("logExecutor")
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(LOG_EXECUTOR_CORE_POOL_SIZE);
        executor.setMaxPoolSize(LOG_EXECUTOR_MAX_POOL_SIZE);
        executor.setQueueCapacity(LOG_EXECUTOR_QUEUE_CAPACITY);
        executor.setThreadNamePrefix("log-worker-");
        executor.initialize();
        return executor;
    }
}
