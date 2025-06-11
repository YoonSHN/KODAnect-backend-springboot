package kodanect.common.exception.config;

import io.sentry.Sentry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentryTestController {

    @GetMapping("/sentry-test")
    public String testSentry() {
        Sentry.configureScope(scope -> scope.setTag("env", "test"));
        Sentry.captureException(new RuntimeException("Sentry 연동 테스트"));
        return "Sentry 전송 완료 (확인 필요)";
    }
}
