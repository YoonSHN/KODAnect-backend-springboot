package kodanect.domain.recipient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientCommentUpdateRequestDto {

    @NotBlank(message = "작성자는 필수 입력 항목입니다.")
    @Size(max = 150, message = "작성자는 최대 150자(바이트) 이하여야 합니다.") // DB varchar(150)에 맞춰 조정
    private String commentWriter;

    @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
    private String contents;
}
