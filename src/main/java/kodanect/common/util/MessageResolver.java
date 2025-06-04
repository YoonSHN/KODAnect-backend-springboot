package kodanect.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageResolver {
    private final MessageSourceAccessor messageSourceAccessor;

    public String get(String code) {
        return messageSourceAccessor.getMessage(code);
    }

    public String get(String code, Object... args) {
        return messageSourceAccessor.getMessage(code, args);
    }
}