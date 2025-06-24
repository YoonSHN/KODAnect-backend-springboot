package kodanect.common.decorator;

import org.junit.Test;
import org.slf4j.MDC;

import static org.junit.Assert.*;

/**
 * {@link MdcTaskDecorator} 클래스의 동작을 검증하는 단위 테스트 클래스입니다.
 *
 * 주요 검증 항목:
 * - MDC(ContextMap)가 비동기 작업에 전파되는지
 * - 작업 실행 이후 MDC가 정상적으로 초기화되는지
 */
public class MdcTaskDecoratorTest {

    /**
     * Given: 현재 스레드의 MDC에 "requestId" 값이 설정되어 있는 상황에서
     * When: 해당 컨텍스트를 복사한 Runnable을 데코레이터로 감싸 실행하면
     * Then: 실행 중에는 동일한 MDC 값이 유지되고, 실행 완료 후에는 MDC가 초기화되어야 한다.
     */
    @Test
    public void testMdcContextIsCopiedAndCleared() {
        MDC.put("requestId", "test-1234");

        Runnable originalTask = () -> {
            assertEquals("test-1234", MDC.get("requestId"));
        };

        MdcTaskDecorator decorator = new MdcTaskDecorator();
        Runnable decorated = decorator.decorate(originalTask);

        decorated.run();

        assertNull(MDC.get("requestId"));
    }

}
