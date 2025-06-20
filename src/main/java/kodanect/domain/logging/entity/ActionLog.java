package kodanect.domain.logging.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 액션 로그 엔티티
 *
 * 사용자 액션 로그를 DB에 저장하기 위한 매핑 클래스
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb25_940_action_log")
@ToString
public class ActionLog {

    /**
     * 로그 순번
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_seq")
    private Integer logSeq;

    /**
     * URL 명
     */
    @Column(name = "url_name", nullable = false, length = 600)
    private String urlName;

    /**
     * CRUD 코드
     */
    @Column(name = "crud_code", length = 10)
    private String crudCode;

    /**
     * IP 주소
     */
    @Column(name = "ip_addr", length = 60)
    private String ipAddr;

    /**
     * 로그 내용
     */
    @Column(name = "log_text", columnDefinition = "TEXT")
    private String logText;

    /**
     * 생성 일시
     */
    @CreationTimestamp
    @Column(name = "write_time", nullable = false, updatable = false)
    private LocalDateTime writeTime;

}
