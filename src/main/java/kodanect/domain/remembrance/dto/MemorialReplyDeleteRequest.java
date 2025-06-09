package kodanect.domain.remembrance.dto;

import kodanect.domain.remembrance.dto.common.BlankGroup;
import kodanect.domain.remembrance.dto.common.PatternGroup;
import lombok.*;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static kodanect.common.exception.config.MessageKeys.REPLY_PASSWORD_EMPTY;
import static kodanect.common.exception.config.MessageKeys.REPLY_PASSWORD_INVALID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@GroupSequence({MemorialReplyDeleteRequest.class, BlankGroup.class, PatternGroup.class})
public class MemorialReplyDeleteRequest {

    /* 댓글 비밀번호 */
    @NotBlank(message = REPLY_PASSWORD_EMPTY, groups = BlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9]{8,16}$", message = REPLY_PASSWORD_INVALID, groups = Pattern.class)
    private String replyPassword;
}
