package kodanect.domain.logging.scheduler;

import kodanect.domain.logging.code.CrudCode;
import kodanect.domain.logging.flusher.ActionLogFlusher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 사용자 액션 로그를 주기적으로 배출(flush)하는 스케줄러 컴포넌트입니다.
 *
 * {@link ActionLogFlusher}를 활용해 프론트엔드, 백엔드, 시스템 로그 버퍼에
 * 쌓인 데이터를 주기적으로 집계 및 저장합니다.
 *
 * 로그 유형(CrudCode 기준)에 따라 다른 주기와 임계값을 설정하여
 * 로그 저장 빈도를 최적화하며, 일정 간격마다 전체 로그를 강제 배출하는 로직도 포함합니다.
 */
@Component
@RequiredArgsConstructor
public class ActionLogScheduler {

    private static final int TEN_MINUTES = 10 * 60 * 1000;
    private static final int TWENTY_MINUTES = 20 * 60 * 1000;
    private static final int THIRTY_MINUTES = 30 * 60 * 1000;

    private static final int TEN_SECONDS = 10 * 1000;
    private static final int TWENTY_SECONDS = 20 * 1000;
    private static final int THIRTY_SECONDS = 30 * 1000;

    private static final int READ_THRESHOLD = 100;
    private static final int OTHER_THRESHOLD = 10;

    private final ActionLogFlusher flusher;

    /**
     * 주기적으로 READ 로그(CrudCode.R)를 임계값 기준으로 배출합니다.
     *
     * flushByCrudCode() 호출을 통해 100건 이상 쌓인 로그만 저장 대상으로 간주합니다.
     */
    @Scheduled(fixedDelay = TEN_MINUTES, initialDelay = TEN_SECONDS)
    public void flushReadLogs() {
        flusher.flushByCrudCode(CrudCode.R, READ_THRESHOLD);
    }

    /**
     * 주기적으로 C/U/D/X 로그를 각각 임계값 기준으로 배출합니다.
     *
     * 각 CRUD 코드별로 로그 수가 10건 이상일 때만 저장 대상으로 처리합니다.
     */
    @Scheduled(fixedDelay = TWENTY_MINUTES, initialDelay = TWENTY_SECONDS)
    public void flushOtherLogs() {
        for (CrudCode code : List.of(CrudCode.C, CrudCode.U, CrudCode.D, CrudCode.X)) {
            flusher.flushByCrudCode(code, OTHER_THRESHOLD);
        }
    }

    /**
     * 일정 주기마다 임계값 조건과 무관하게 모든 로그를 강제 배출합니다.
     *
     * flushAll()은 모든 세션과 CRUD 유형을 포함한 로그를 저장합니다.
     * 시스템 재시작 등으로 인해 유실 가능성이 있는 로그를 주기적으로 수집하는 용도입니다.
     */
    @Scheduled(fixedDelay = THIRTY_MINUTES, initialDelay = THIRTY_SECONDS)
    public void flushAllLogsForcefully() {
        flusher.flushAll();
    }

}
