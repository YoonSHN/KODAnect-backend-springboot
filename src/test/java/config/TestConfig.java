package config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;

@TestConfiguration
public class TestConfig {
    @Bean
    public MessageSourceAccessor messageSourceAccessor() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("message-common");
        return new MessageSourceAccessor(messageSource);
    }
}
