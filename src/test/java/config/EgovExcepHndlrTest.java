package config;

import kodanect.common.exception.config.EgovExcepHndlr;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class EgovExcepHndlrTest {

    private final EgovExcepHndlr handler = new EgovExcepHndlr();

    @Test
    public void testRootCauseIsSameAsException() throws Exception {
        Exception ex = new RuntimeException("Top level exception");
        String result = invokeGetRootCauseMessage(ex);
        assertEquals("Top level exception", result);
    }

    @Test
    public void testSingleNestedCause() throws Exception {
        Exception ex = new RuntimeException("Wrapper", new IllegalArgumentException("Root cause message"));
        String result = invokeGetRootCauseMessage(ex);
        assertEquals("Root cause message", result);
    }

    @Test
    public void testMultipleNestedCauses() throws Exception {
        Exception ex = new RuntimeException("Wrapper",
                new IllegalStateException("Middle",
                        new NullPointerException("Deepest cause")
                )
        );
        String result = invokeGetRootCauseMessage(ex);
        assertEquals("Deepest cause", result);
    }

    private String invokeGetRootCauseMessage(Throwable ex) throws Exception {
        Method method = EgovExcepHndlr.class.getDeclaredMethod("getRootCauseMessage", Throwable.class);
        method.setAccessible(true); // private 접근 허용
        return (String) method.invoke(handler, ex);
    }
}
