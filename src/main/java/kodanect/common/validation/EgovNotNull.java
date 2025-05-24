package kodanect.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 커스텀 유효성 검사 애너테이션
 *
 * 대상 필드가 null 또는 빈 문자열인 경우 검증 실패로 처리
 *
 * 역할
 * - 유효성 검사 조건 정의
 * - 검증 메시지 및 그룹 지정 기능 제공
 *
 * 특징
 * - EgovValidation을 통한 로직 위임 구조
 * - 문자열 타입 필드에 적용
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EgovValidation.class)
@Documented
public @interface EgovNotNull {

    /**
     * 검증 실패 시 기본 메시지
     */
    String message() default ""; // 유효성 검사 false시 반환할 기본 메시지

    /**
     * 검증 그룹 지정
     */
    Class<?>[] groups() default { }; // 어노테이션을 적용할 특정 상황(예를 들어 특정 Class 시 어노테이션 동작)

    /**
     * 검증 대상에 대한 메타 정보 정의
     */
    Class<? extends Payload>[] payload() default { }; // 심각한 정도 등 메타 데이터를 정의해 넣을 수 있음

}
