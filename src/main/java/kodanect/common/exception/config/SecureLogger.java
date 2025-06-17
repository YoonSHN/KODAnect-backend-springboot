package kodanect.common.exception.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 로그 인젝션(CRLF) 방지를 위한 Logger 래퍼 유틸리티
 *
 * 줄바꿈 문자(\r, \n)를 제거하여 로그 인젝션 방지.
 * SLF4J 로그 레벨 전부 지원: trace, debug, info, warn, error
 */
public class SecureLogger {

    private final Logger delegate;

    private SecureLogger(Logger logger) {
        this.delegate = logger;
    }

    public static SecureLogger forTest(Logger mockLogger) {
        return new SecureLogger(mockLogger);
    }

    public static SecureLogger getLogger(Class<?> clazz) {
        return new SecureLogger(LoggerFactory.getLogger(clazz));
    }

    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    public void trace(String msg, Object... args) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(sanitize(msg), sanitizeArgs(args));
        }
    }

    public void debug(String msg, Object... args) {
        if (delegate.isDebugEnabled()) {
            delegate.debug(sanitize(msg), sanitizeArgs(args));
        }
    }

    public void info(String msg, Object... args) {
        if (delegate.isInfoEnabled()) {
            delegate.info(sanitize(msg), sanitizeArgs(args));
        }
    }

    public void warn(String msg, Object... args) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(sanitize(msg), sanitizeArgs(args));
        }
    }

    public void warn(String msg, Throwable t) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(sanitize(msg), t);
        }
    }

    public void error(String msg, Object... args) {
        if (delegate.isErrorEnabled()) {
            delegate.error(sanitize(msg), sanitizeArgs(args));
        }
    }

    public void error(String msg, Throwable t) {
        if (delegate.isErrorEnabled()) {
            delegate.error(sanitize(msg), t);
        }
    }

    /**
     * CRLF 문자 제거
     */
    private String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replaceAll("[\r\n\t]", "")
                .replaceAll("\\p{Cntrl}", "")
                .replaceAll("[{}]", "");
    }

    /**
     * 로그 args 배열을 순회하며 문자열이면 CRLF 제거
     */
    private Object[] sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return args;
        }
        Object[] sanitized = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof String string) {
                sanitized[i] = sanitize(string);
            } else {
                sanitized[i] = arg;
            }
        }
        return sanitized;
    }
}
