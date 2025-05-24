package kodanect.common.validation;

import org.springframework.util.ObjectUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * EgovNotNull 검증 로직 구현
 *
 * 문자열 필드가 null 또는 빈 문자열("")인 경우 검증 실패로 처리
 *
 * 역할
 * - EgovNotNull 애너테이션과 연동된 유효성 검사 수행
 * - ObjectUtils 기반 null/빈 문자열 여부 판단
 *
 * 특징
 * - 문자열 타입에 특화된 경량 검증기 구성
 * - ConstraintValidator 인터페이스 구현 방식 사용
 */
public class EgovValidation implements ConstraintValidator<EgovNotNull, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !ObjectUtils.isEmpty(value);
    }

}
