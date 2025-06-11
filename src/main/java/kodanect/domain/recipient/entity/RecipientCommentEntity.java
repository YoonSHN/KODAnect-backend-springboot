package kodanect.domain.recipient.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb25_431_recipient_letter_comment")
@EntityListeners(AuditingEntityListener.class)
public class RecipientCommentEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_seq", nullable = false) // 명시적 컬럼명 지정 (DB와 매핑)
    private int commentSeq;

    // 수혜자 편지 일련번호 (부모 엔티티 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "letter_seq", nullable = false)
    private RecipientEntity letterSeq;

    // 작성자 이름
    @Column(name = "comment_writer", length = 150) // DB varchar(150)
    private String commentWriter;

    // 댓글 비밀번호
    @Column(name = "comment_passcode", length = 60, nullable = false) // DB varchar(60)
    private String commentPasscode;


    @Lob // TEXT 타입 매핑
    @Column(name = "contents", columnDefinition = "TEXT", nullable = false) // DB 컬럼명 'contents' 명시
    private String commentContents;

    @CreatedDate
    @Column(name = "write_time", nullable = false, updatable = false)
    private LocalDateTime writeTime;

    @Transient
    private String writerId;

    @LastModifiedDate
    @Column(name = "modify_time")
    private LocalDateTime modifyTime;

    @Transient
    private String modifierId;

    @Column(name = "del_flag", length = 1, nullable = false)
    @Builder.Default
    private String delFlag = "N";


    // 편의 메서드
    public void softDelete() {
        this.delFlag = "Y";
    }

    public boolean checkPasscode(String inputPasscode) {
        return this.commentPasscode != null && this.commentPasscode.equals(inputPasscode);
    }
}
