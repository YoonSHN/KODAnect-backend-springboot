package kodanect.common.config;

import kodanect.domain.logging.decorator.MdcTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 작업 처리를 위한 Executor 설정
 *
 * 비핵심 작업을 메인 쓰레드와 분리해 처리
 * 제한된 리소스 환경에서 작업 분리에 초점
 * 필요 시 작업 목적에 맞는 Executor 추가 정의 가능
 */
@Configuration
@EnableAsync
public class AsyncExecutorConfig {

    private static final int LOG_EXECUTOR_CORE_POOL_SIZE = 2;
    private static final int LOG_EXECUTOR_MAX_POOL_SIZE = 2;
    private static final int LOG_EXECUTOR_QUEUE_CAPACITY = 100;
    private static final int LOG_EXECUTOR_AWAIT_TERMINATION_SECONDS = 5;

    /**
     * 로그 비동기 처리 전용 Executor Bean
     *
     * 로그 식별을 위한 스레드 이름 prefix 설정
     * MDC context 전파를 위한 TaskDecorator 설정
     * 낮은 처리 빈도에 맞춰 최소 리소스로 구성
     * 큐 초과 시 작업 유실 방지를 위한 CallerRunsPolicy 적용
     * 스레드 종료 전 작업 완료 보장
     *
     * @return 로그 비동기 처리를 위한 Executor
     */
    @Bean("logExecutor")
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(LOG_EXECUTOR_CORE_POOL_SIZE);
        executor.setMaxPoolSize(LOG_EXECUTOR_MAX_POOL_SIZE);
        executor.setQueueCapacity(LOG_EXECUTOR_QUEUE_CAPACITY);
        executor.setThreadNamePrefix("log-worker-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(LOG_EXECUTOR_AWAIT_TERMINATION_SECONDS);
        executor.initialize();
        return executor;
    }

}
