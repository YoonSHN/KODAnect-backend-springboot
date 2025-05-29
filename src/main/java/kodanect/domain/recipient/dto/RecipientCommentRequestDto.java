package kodanect.domain.recipient.dto;

import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientCommentRequestDto {

    @NotNull(message = "게시물 일련번호는 필수 입력 항목입니다.")
    private Integer letterSeq; // 댓글이 달릴 게시물의 시퀀스

    @NotBlank(message = "작성자는 필수 입력 항목입니다.")
    @Size(max = 150, message = "작성자는 최대 150자(바이트) 이하여야 합니다.") // DB varchar(150)에 맞춰 조정
    private String commentWriter;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$", message = "비밀번호는 영문 숫자 8자 이상 이어야 합니다.")
    @Size(max = 60, message = "비밀번호는 최대 60자(바이트) 이하여야 합니다.") // DB varchar(60)에 맞춰 조정
    private String commentPasscode;

    @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
    private String commentContents;

    // CAPTCHA 토큰 (댓글 작성, 수정, 삭제 시 모두 필요)
    @NotBlank(message = "캡차 인증 토큰은 필수입니다.")
    private String captchaToken;


    // DTO를 Entity로 변환하는 메서드
    public RecipientCommentEntity toEntity() {
        // RecipientEntity는 여기서는 FK로만 사용되므로, 직접 생성하지 않고 letterSeq만 설정합니다.
        // 실제 저장 시에는 서비스 계층에서 letterSeq를 이용해 RecipientEntity를 조회하여 set해야 합니다.
        // 현재는 편의상 letterSeq를 가지는 RecipientEntity 객체만 만듭니다.
        RecipientEntity recipientEntity = RecipientEntity.builder()
                .letterSeq(this.letterSeq)
                .build();

        return RecipientCommentEntity.builder()
                .letterSeq(recipientEntity) // 임시 RecipientEntity 객체 설정, 실제 검증은 서비스에서
                .commentWriter(this.commentWriter)
                .commentPasscode(this.commentPasscode)
                .commentContents(this.commentContents)
                .build();
    }
}
