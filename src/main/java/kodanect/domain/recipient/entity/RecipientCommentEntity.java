package kodanect.domain.recipient.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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
    @NotBlank(message = "작성자는 필수 입력 항목입니다.")
    @Size(max = 30, message = "작성자는 10자 이하여야 합니다.") // (한글 10자 = 30바이트 고려)
    private String commentWriter;
    // 댓글 비밀번호 (Request 시 필요, Response 시 제외)
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$", message = "비밀번호는 영문 숫자 8자 이상 이어야 합니다.")
    private String commentPasscode;
    // 댓글 내용
    @Lob
    @Column(name = "contents", columnDefinition = "TEXT")
    @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
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
    @Column(name = "del_flag", nullable = false, columnDefinition = "CHAR(1)")
    @Builder.Default
    private String delFlag = "N";

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
