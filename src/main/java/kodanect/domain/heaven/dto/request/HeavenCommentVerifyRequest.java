package kodanect.domain.heaven.dto.request;

import kodanect.domain.remembrance.dto.common.BlankGroup;
import kodanect.domain.remembrance.dto.common.PatternGroup;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static kodanect.common.exception.config.MessageKeys.COMMENT_PASSWORD_EMPTY;
import static kodanect.common.exception.config.MessageKeys.COMMENT_PASSWORD_INVALID;

@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@Builder
public class HeavenCommentVerifyRequest {

    /* 댓글 비밀번호 */
    @NotBlank(message = COMMENT_PASSWORD_EMPTY, groups = BlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9]{8,16}$", message = COMMENT_PASSWORD_INVALID, groups = PatternGroup.class)
    private String commentPasscode;
}
