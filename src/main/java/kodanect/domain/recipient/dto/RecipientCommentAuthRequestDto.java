package kodanect.domain.recipient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientCommentAuthRequestDto {

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$", message = "비밀번호는 영문 숫자 8자 이상 이어야 합니다.")
    @Size(max = 60, message = "비밀번호는 최대 60자(바이트) 이하여야 합니다.") // DB varchar(60)에 맞춰 조정
    private String commentPasscode;
}
