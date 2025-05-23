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
@Table(name = "tb25_430_recipient_letter")
public class RecipientEntity {

    private static final long serialVersionUID = 1L;

    // 수혜자 편지 일련번호, AutoIncrement
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer letterSeq;
    // 권역 코드
    private String organCode;
    // 기타 장기
    private String organEtc;
    // 스토리 제목
    @Column(name = "story_title")
    private String letterTitle;
    // 수혜 연도
    private String recipientYear;
    // 편지 비밀번호 (Request 시 필요)
    private String letterPasscode;
    // 편지 작성자
    private String letterWriter;
    // 편지 익명여부
    @Column(name = "anonymity_flag", columnDefinition = "CHAR(1)")
    private char anonymityFlag;
    // 조회 건수 (Request 시에는 0으로 초기화되거나 무시)
    @Builder.Default
    private int readCount = 0;
    // 편지 내용
    private String letterContents;
    // 이미지 파일 명
    private String fileName;
    // 이미지 원본 파일 명
    private String orgFileName;
    // 생성 일시 (Request 시에는 클라이언트에서 보내지 않음)
    @Column(nullable = false, updatable = false)
    private LocalDateTime writeTime;
    // 생성자 아이디
    private String writerId;
    // 수정 일시 (Request 시에는 클라이언트에서 보내지 않음)
    private LocalDateTime modifyTime;
    // 수정자 아이디
    private String modifierId;
    // 삭제 여부 (Request 시에는 클라이언트에서 보내지 않음)
    @Column(name = "del_flag", columnDefinition = "CHAR(1)")
    @Builder.Default
    private char delFlag = 'N';

    // 검색 키워드용
    @Transient
    private String searchKeyword;

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
