package kodanect.common.config.validation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Validator 설정
 *
 * Spring Validation에서 메시지 리소스를 사용하기 위한 Validator 설정 등록
 *
 * 역할
 * - javax.validation 기반 검증기 등록
 * - MessageSource 연동을 통해 다국어 메시지 출력 처리
 *
 * 특징
 * - LocalValidatorFactoryBean 기반 구성
 * - EgovConfigCommon의 messageSource Bean 주입 방식 사용
 */
@Configuration
public class EgovConfigValidation {

    /**
     * Validator Bean
     *
     * 메시지 리소스를 사용하는 LocalValidatorFactoryBean 등록
     */
    @Bean
    public Validator getValidator(@Qualifier("messageSource") MessageSource messageSource) {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setValidationMessageSource(messageSource);
        return localValidatorFactoryBean;
    }

}
