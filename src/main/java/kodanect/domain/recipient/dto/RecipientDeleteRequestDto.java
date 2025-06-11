package kodanect.domain.recipient.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank; // 필요하다면 추가

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipientDeleteRequestDto {

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.") // 또는 @NotNull
    private String letterPasscode;

}
