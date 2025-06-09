package config;

import io.sentry.Sentry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SentryTestRunner implements CommandLineRunner {
    @Override
    public void run(String... args) {
        try {
            throw new RuntimeException("Sentry 연동 테스트 예외");
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }
}
