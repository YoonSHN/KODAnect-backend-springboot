package kodanect.domain.recipient.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
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
@Table(name = "tb25_430_recipient_letter")
@DynamicInsert // insert 시 null이 아닌 필드만 쿼리에 포함
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 활성화
public class RecipientEntity {

    private static final long serialVersionUID = 1L;

    // 수혜자 편지 일련번호, AutoIncrement
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "letter_seq", nullable = false)
    private Integer letterSeq;

    // 장기 구분 코드
    @Column(name = "organ_code", length = 10) // nullable = true 는 컬럼 정의에 있으면 충분
    private String organCode;

    // 기타 장기
    @Column(name = "organ_etc", length = 90)
    private String organEtc;

    // 스토리 제목
    @Column(name = "story_title", length = 150, nullable = false)
    private String letterTitle;

    // 수혜 연도
    @Column(name = "recipient_year", length = 4) // nullable = true 는 컬럼 정의에 있으면 충분
    private String recipientYear;

    // 편지 비밀번호
    @Column(name = "letter_passcode", length = 20, nullable = false)
    private String letterPasscode;

    // 편지 작성자
    @Column(name = "letter_writer", length = 30)
    private String letterWriter;

    // 편지 익명여부
    @Column(name = "anonymity_flag", length = 1) // nullable = true 는 컬럼 정의에 있으면 충분
    private String anonymityFlag;

    // 조회 건수 (Request 시에는 0으로 초기화되거나 무시)
    @Builder.Default
    @Column(name = "read_count")
    private int readCount = 0;

    // 편지 내용
    @Lob
    @Column(name = "letter_contents", columnDefinition = "TEXT", nullable = false)
    private String letterContents;

    // 이미지 파일 명
    @Column(name = "file_name", length = 600)
    private String fileName;

    // 이미지 원본 파일 명
    @Column(name = "org_file_name", length = 600)
    private String orgFileName;

    // 생성 일시
    @CreatedDate
    @Column(name = "write_time", nullable = false, updatable = false)
    private LocalDateTime writeTime;

    // 생성자 아이디
    @Column(name = "writer_id", length = 50)
    private String writerId;

    // 수정 일시
    @LastModifiedDate
    @Column(name = "modify_time")
    private LocalDateTime modifyTime;

    // 수정자 아이디
    @Column(name = "modifier_id", length = 50)
    private String modifierId;

    // 삭제 여부
    @Column(name = "del_flag", length = 1, nullable = false)
    @Builder.Default
    private String delFlag = "N";

    // 검색 키워드용 (Transient는 유지)
    @Transient
    private String searchKeyword;

    // 검색 타입용 (Transient는 유지)
    @Transient
    private String searchType;

    // 비즈니스 로직을 위한 메서드 (유지)
    public void incrementReadCount() {
        this.readCount = this.readCount + 1;
    }

    public void softDelete() {
        this.delFlag = "Y";
    }

    // 비밀번호 일치 여부 확인 (서비스에서 사용) (유지)
    public boolean checkPasscode(String inputPasscode) {
        return this.letterPasscode != null && this.letterPasscode.equals(inputPasscode);
    }
}
