package kodanect.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 기능을 활성화하는 설정 클래스입니다.
 * 이 클래스는 Spring Data JPA의 @CreatedDate, @LastModifiedDate 등의 어노테이션이
 * 올바르게 동작하도록 AuditingEntityListener를 등록합니다.
 */
@Configuration // 이 클래스가 스프링 설정 클래스임을 명시합니다.
@EnableJpaAuditing // JPA Auditing 기능을 활성화합니다.
public class JpaAuditingConfig {
}
