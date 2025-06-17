package kodanect.domain.logging.scheduler;

import kodanect.domain.logging.code.CrudCode;
import kodanect.domain.logging.flusher.ActionLogFlusher;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * {@link ActionLogScheduler} 클래스의 스케줄러 동작을 검증하는 단위 테스트입니다.
 *
 * 각 스케줄 메서드가 {@link ActionLogFlusher}에 적절한 flush 요청을 전달하는지 확인합니다.
 */
public class ActionLogSchedulerTest {

    private ActionLogFlusher flusher;
    private ActionLogScheduler scheduler;

    /**
     * 테스트 실행 전 {@link ActionLogFlusher}를 Mock으로 생성하고
     * 이를 주입하여 {@link ActionLogScheduler} 인스턴스를 초기화합니다.
     */
    @Before
    public void setUp() {
        flusher = mock(ActionLogFlusher.class);
        scheduler = new ActionLogScheduler(flusher);
    }

    /**
     * GIVEN: READ 로그 배출 스케줄이 실행될 때
     * WHEN: flushReadLogs()가 호출되면
     * THEN: flusher.flushByCrudCode(CrudCode.R, 100) 이 호출되어야 한다.
     */
    @Test
    public void flushReadLogs_shouldDelegateToFlusherWithReadThreshold() {
        scheduler.flushReadLogs();
        verify(flusher).flushByCrudCode(CrudCode.R, 100);
    }

    /**
     * GIVEN: C/U/D/X 로그 배출 스케줄이 실행될 때
     * WHEN: flushOtherLogs()가 호출되면
     * THEN: 각 CRUD 코드에 대해 flusher.flushByCrudCode(code, 10) 이 호출되어야 한다.
     */
    @Test
    public void flushOtherLogs_shouldFlushEachCrudCodeWithOtherThreshold() {
        scheduler.flushOtherLogs();

        verify(flusher).flushByCrudCode(CrudCode.C, 10);
        verify(flusher).flushByCrudCode(CrudCode.U, 10);
        verify(flusher).flushByCrudCode(CrudCode.D, 10);
        verify(flusher).flushByCrudCode(CrudCode.X, 10);
    }

    /**
     * GIVEN: 전체 로그 강제 배출 스케줄이 실행될 때
     * WHEN: flushAllLogsForcefully()가 호출되면
     * THEN: flusher.flushAll() 이 호출되어야 한다.
     */
    @Test
    public void flushAllLogsForcefully_shouldCallFlusherFlushAll() {
        scheduler.flushAllLogsForcefully();
        verify(flusher).flushAll();
    }

}
