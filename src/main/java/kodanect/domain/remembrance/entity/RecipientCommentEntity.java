package kodanect.domain.remembrance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb25_431_recipient_letter_comment")
public class RecipientCommentEntity {

    private static final long serialVersionUID = 1L;

    // 수혜자 편지 댓글 일련번호, AutoIncrement
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentSeq; // 기본형 int 대신 Integer 사용 권장

    // 수혜자 편지 일련번호 (부모 엔티티 참조)
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 (성능 최적화)
    @JoinColumn(name = "letter_seq", nullable = false)
    private RecipientEntity letter;

    // 작성자 이름
    private String commentWriter;
    // 댓글 비밀번호 (Request 시 필요, Response 시 제외)
    private String commentPasscode;
    // 댓글 내용
    private String contents;
    // 생성 일시
    @Column(nullable = false, updatable = false)
    private LocalDateTime writeTime;
    // 생성자 아이디
    private String writerId;
    // 수정 일시
    // @JsonFormat은 Entity에는 불필요
    private LocalDateTime modifyTime;
    // 수정자 아이디
    private String modifierId;
    // 삭제 여부
    @Column(nullable = false, columnDefinition = "CHAR(1)")
    @Builder.Default
    private char delFlag = 'N';

    @PrePersist
    public void prePersist() {
        this.writeTime = LocalDateTime.now();
        this.modifyTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.modifyTime = LocalDateTime.now();
    }
}
