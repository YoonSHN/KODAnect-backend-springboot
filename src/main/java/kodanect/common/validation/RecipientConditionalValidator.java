package kodanect.common.validation;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * ConditionalValidation 애노테이션에 대한 유효성 검사 로직 구현.
 * 특정 필드의 값이 특정 값일 때, 다른 필드가 비어있지 않은지 검사합니다.
 */
public class RecipientConditionalValidator implements ConstraintValidator<RecipientConditionalValidation, Object> {

    private String conditionalProperty;
    private String expectedValue;
    private String requiredProperty;
    private String message;

    @Override
    public void initialize(RecipientConditionalValidation constraintAnnotation) {
        this.conditionalProperty = constraintAnnotation.conditionalProperty();
        this.expectedValue = constraintAnnotation.expectedValue();
        this.requiredProperty = constraintAnnotation.requiredProperty();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        // 검사할 객체가 null이면 유효성 검사를 통과시킴 (다른 @NotNull 등으로 처리될 수 있음)
        if (object == null) {
            return true;
        }

        // 객체의 필드 값에 접근하기 위해 Spring의 BeanWrapper 사용
        // BeanWrapper는 reflection을 사용하여 객체의 속성에 쉽게 접근할 수 있도록 돕습니다.
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(object);

        // 조건이 되는 필드 (예: organCode)의 실제 값 가져오기
        Object conditionalValue = beanWrapper.getPropertyValue(conditionalProperty);

        // 조건이 충족되는지 확인 (conditionalProperty의 값이 expectedValue와 같은지)
        // null 체크와 String 변환을 안전하게 처리
        if (conditionalValue != null && expectedValue.equals(String.valueOf(conditionalValue))) {
            // 조건이 충족되었을 때, 필수가 될 필드 (예: organEtc)의 실제 값 가져오기
            Object requiredValue = beanWrapper.getPropertyValue(requiredProperty);

            // 필수가 될 필드가 null이거나 비어있는 문자열인 경우 유효성 검사 실패
            // StringUtils.hasText()는 null, 빈 문자열, 공백 문자열 모두를 체크합니다.
            if (!StringUtils.hasText(String.valueOf(requiredValue))) {
                // 기본 유효성 검사 메시지 비활성화
                context.disableDefaultConstraintViolation();
                // 새로운 유효성 검사 위반 메시지 추가
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(requiredProperty) // 어떤 필드에서 문제가 발생했는지 지정
                        .addConstraintViolation();
                return false; // 유효성 검사 실패
            }
        }
        return true; // 유효성 검사 통과
    }
}
