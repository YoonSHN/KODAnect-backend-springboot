package kodanect.domain.logging.repository;

import kodanect.domain.logging.entity.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * {@link ActionLog} 엔티티의 DB 접근을 담당하는 JPA 리포지토리입니다.
 *
 * 기본 CRUD 기능은 {@link JpaRepository}를 통해 자동 제공됩니다.
 */
public interface ActionLogRepository extends JpaRepository<ActionLog, Integer> {
}
