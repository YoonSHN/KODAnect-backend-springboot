package kodanect.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE}) // 클래스 레벨에 적용
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RecipientConditionalValidator.class) // 이 애노테이션의 유효성 검사를 수행할 Validator 지정
@Documented
public @interface RecipientConditionalValidation {

    String message() default "조건부 유효성 검사 실패"; // 기본 메시지
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String conditionalProperty(); // 조건을 검사할 필드 이름 (예: "organCode")
    String expectedValue();      // conditionalProperty의 예상 값 (예: "ORGAN000")
    String requiredProperty();   // 조건이 충족될 때 필수가 될 필드 이름 (예: "organEtc")
}
