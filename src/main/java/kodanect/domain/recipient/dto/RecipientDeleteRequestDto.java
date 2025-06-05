package kodanect.domain.recipient.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank; // 필요하다면 추가

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipientDeleteRequestDto {

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.") // 또는 @NotNull
    private String letterPasscode;

    @NotBlank(message = "캡차 토큰은 필수 입력 항목입니다.") // 또는 @NotNull
    private String captchaToken;
}
