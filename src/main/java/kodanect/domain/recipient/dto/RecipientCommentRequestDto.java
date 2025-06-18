package kodanect.domain.recipient.dto;

import kodanect.domain.recipient.entity.RecipientCommentEntity;
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
public class RecipientCommentRequestDto {

    @NotBlank(message = "작성자는 필수 입력 항목입니다.")
    @Size(max = 150, message = "작성자는 최대 150자(바이트) 이하여야 합니다.") // DB varchar(150)에 맞춰 조정
    private String commentWriter;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$", message = "비밀번호는 영문 숫자 8자 이상 이어야 합니다.")
    @Size(max = 60, message = "비밀번호는 최대 60자(바이트) 이하여야 합니다.") // DB varchar(60)에 맞춰 조정
    private String commentPasscode;

    @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
    private String contents;

    // DTO를 Entity로 변환하는 메서드
    public RecipientCommentEntity toEntity() {
        return RecipientCommentEntity.builder()
                .commentWriter(this.commentWriter)
                .commentPasscode(this.commentPasscode)
                .contents(this.contents)
                .build();
    }
}
