package kodanect.domain.recipient.dto;

import kodanect.common.validation.RecipientConditionalValidation;
import kodanect.domain.recipient.entity.RecipientEntity; // DTO에서 Entity로 변환하기 위해 임포트

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

// 게시물 생성 및 수정 요청에 사용될 DTO
@RecipientConditionalValidation(
        conditionalProperty = "organCode",
        expectedValue = "ORGAN000",
        requiredProperty = "organEtc",
        message = "직접입력 선택 시 기타 장기를 입력해야 합니다."
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientRequestDto {

    // 장기 구분 코드
    @Pattern(regexp = "^ORGAN(00\\d|01[0-4])$", message = "유효하지 않은 장기 코드입니다.")
    private String organCode;

    // 기타 장기 (조건부 유효성 검사는 @RecipientConditionalValidation 에서 담당)
    @Size(max = 30, message = "기타 장기는 30자(한글) 이하여야 합니다.")
    private String organEtc;

    // 스토리 제목
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 50, message = "제목은 50자 이하여야 합니다.")
    private String letterTitle;

    // 수혜 연도
    @Pattern(regexp = "^\\d{4}$", message = "기증받은 년도는 4자리 숫자여야 합니다.")
    @Min(value = 1995, message = "기증받은 년도는 1995년에서 2030년 사이의 값이어야 합니다.")
    @Max(value = 2030, message = "기증받은 년도는 1995년에서 2030년 사이의 값이어야 합니다.")
    private String recipientYear;

    // 편지 작성자
    @Size(max = 10, message = "작성자는 10자(한글) 이하여야 합니다.")
    @NotBlank(message = "작성자는 필수 입력 항목입니다.")
    @Pattern(regexp = "^[a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ\\s]*$", message = "작성자는 한글과 영문만 입력 가능합니다.") // 한글, 영문, 공백 허용
    private String letterWriter;

    // 편지 익명여부
    @Pattern(regexp = "[YN]", message = "익명 여부는 'Y' 또는 'N'이어야 합니다.")
    private String anonymityFlag;

    // 편지 내용
    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String letterContents;

    // 게시물 비밀번호 (영문, 숫자 포함 8자 이상)
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$", message = "비밀번호는 영문, 숫자를 포함하여 8자 이상이어야 합니다.")
    private String letterPasscode;

    // RequestDto를 Entity로 변환하는 헬퍼 메서드 (등록 시)
    public RecipientEntity toEntity() {
        return RecipientEntity.builder()
                .organCode(this.organCode)
                .organEtc(this.organEtc)
                .letterTitle(this.letterTitle)
                .recipientYear(this.recipientYear)
                .letterWriter(this.letterWriter)
                .anonymityFlag(this.anonymityFlag)
                .letterContents(this.letterContents)
                .letterPasscode(this.letterPasscode)
                // writerId, modifierId, delFlag, readCount 등은 서비스 계층에서 처리
                .fileName(null)
                .orgFileName(null)
                .imageUrl(null)
                .build();
    }
}
