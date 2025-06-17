package kodanect.domain.logging.repository;

import config.TestConfig;
import kodanect.domain.logging.entity.ActionLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ActionLogRepository}의 저장 및 조회 기능을 검증하는 JPA 테스트입니다.
 *
 * 엔티티 저장 후 조회 결과가 예상대로 반환되는지 확인합니다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class ActionLogRepositoryTest {

    @Autowired
    private ActionLogRepository repository;

    /**
     * 테스트 실행 전 샘플 로그 데이터를 저장합니다.
     */
    @BeforeEach
    void setup() {
        ActionLog log = ActionLog.builder()
                .ipAddr("192.168.0.1")
                .crudCode("R")
                .urlName("/test")
                .logText("testLogText")
                .build();

        repository.save(log);
    }

    /**
     * Given: 로그 엔티티가 하나 저장된 상태에서
     * When: findAll()을 호출하면
     * Then: 해당 로그가 정확히 조회되고 필드 값이 일치해야 한다
     */
    @Test
    void saveAndFindActionLog() {
        List<ActionLog> result = repository.findAll();

        assertThat(result).hasSize(1);
        ActionLog fetched = result.get(0);

        assertThat(fetched.getIpAddr()).isEqualTo("192.168.0.1");
        assertThat(fetched.getCrudCode()).isEqualTo("R");
        assertThat(fetched.getUrlName()).isEqualTo("/test");
        assertThat(fetched.getLogText()).isEqualTo("testLogText");
        assertThat(fetched.getWriteTime()).isNotNull();
    }

}
