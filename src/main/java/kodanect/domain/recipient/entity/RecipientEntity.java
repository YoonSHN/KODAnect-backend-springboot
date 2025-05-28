package kodanect.domain.recipient.entity;

import kodanect.common.validation.RecipientConditionalValidation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@RecipientConditionalValidation(
        conditionalProperty = "organCode",
        expectedValue = "ORGAN000",
        requiredProperty = "organEtc",
        message = "직접입력 선택 시 기타 장기를 입력해야 합니다."
)
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
    @Column(name = "organ_code", length = 10, nullable = true)
    @Pattern(regexp = "^ORGAN(00[0-9]|01[0-4])$", message = "유효하지 않은 장기 코드입니다.")
    private String organCode;

    // 기타 장기
    @Column(name = "organ_etc", length = 90) // 한글 30자 = 90바이트, 30자 제한
    @Size(max = 30, message = "기타 장기는 30자(한글) 이하여야 합니다.")
    private String organEtc;

    // 스토리 제목
    @Column(name = "story_title", length = 150, nullable = false) // 한글 50자 = 150바이트, 50자 제한
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 50, message = "제목은 50자 이하여야 합니다.") // 글자 수 제한
    private String letterTitle;

    // 수혜 연도
    @Column(name = "recipient_year", length = 4, nullable = true)
    @Pattern(regexp = "^\\d{4}$", message = "기증받은 년도는 4자리 숫자여야 합니다.") // 숫자만 허용하는 패턴
    @Min(value = 1995, message = "기증받은 년도는 1995년에서 2030년 사이의 값이어야 합니다.")
    @Max(value = 2030, message = "기증받은 년도는 1995년에서 2030년 사이의 값이어야 합니다.")
    private String recipientYear;

    // 편지 비밀번호 (Request 시 필요)
    @Column(name = "letter_passcode", length = 20, nullable = false)
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")

    // 영문/숫자 8자 이상 (정규식 패턴)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$", message = "비밀번호는 영문 숫자 8자 이상 이어야 합니다.")
    private String letterPasscode;

    // 편지 작성자
    @Column(name = "letter_writer", length = 30) // 한글 10자 = 30바이트, 10자 제한
    @Size(max = 10, message = "작성자는 10자(한글) 이하여야 합니다.")
    @NotBlank(message = "작성자는 필수 입력 항목입니다.")
    private String letterWriter;

    // 편지 익명여부
    @Column(name = "anonymity_flag", length = 1, nullable = true)
    @Pattern(regexp = "[YN]", message = "익명 여부는 'Y' 또는 'N'이어야 합니다.")
    private String anonymityFlag;

    // 조회 건수 (Request 시에는 0으로 초기화되거나 무시)
    @Builder.Default
    @Column(name = "read_count")
    private int readCount = 0;

    // 편지 내용
    @Lob
    @Column(name = "letter_contents", columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String letterContents;

    // 이미지 파일 명
    @Column(name = "file_name", length = 600)
    @Size(max = 600, message = "파일명이 너무 깁니다. (최대 600자)")
    private String fileName;

    // 이미지 원본 파일 명
    @Column(name = "org_file_name", length = 600)
    @Size(max = 600, message = "원본 파일명이 너무 깁니다. (최대 600자)")
    private String orgFileName;

    // 생성 일시 (Request 시에는 클라이언트에서 보내지 않음)
    @CreatedDate
    @Column(name = "write_time", nullable = false, updatable = false)
    private LocalDateTime writeTime;

    // 생성자 아이디
    @Column(name = "writer_id", length = 50)
    private String writerId;

    // 수정 일시 (Request 시에는 클라이언트에서 보내지 않음)
    @LastModifiedDate
    @Column(name = "modify_time")
    private LocalDateTime modifyTime;

    // 수정자 아이디
    @Column(name = "modifier_id", length = 50)
    private String modifierId;

    // 삭제 여부 (Request 시에는 클라이언트에서 보내지 않음)
    @Column(name = "del_flag", length = 1, nullable = false)
    @Builder.Default
    private String delFlag = "N";

    // 검색 키워드용
    @Transient
    private String searchKeyword;

    // 검색 타입용
    @Transient
    private String searchType;

    // --- CAPTCHA 인증 토큰 추가 ---
    @Transient // JPA 매핑에서 제외
    private String captchaToken;

    // 비즈니스 로직을 위한 메서드
    public void incrementReadCount() {
        this.readCount = this.readCount + 1;
    }

    public void softDelete() {
        this.delFlag = "Y";
    }

    // 비밀번호 일치 여부 확인 (서비스에서 사용)
    public boolean checkPasscode(String inputPasscode) {
        return this.letterPasscode != null && this.letterPasscode.equals(inputPasscode);
    }
}
