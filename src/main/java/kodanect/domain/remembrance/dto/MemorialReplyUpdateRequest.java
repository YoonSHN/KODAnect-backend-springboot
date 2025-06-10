package kodanect.domain.remembrance.dto;

import kodanect.domain.remembrance.dto.common.BlankGroup;
import kodanect.domain.remembrance.dto.common.PatternGroup;
import lombok.*;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static kodanect.common.exception.config.MessageKeys.REPLY_PASSWORD_EMPTY;
import static kodanect.common.exception.config.MessageKeys.REPLY_PASSWORD_INVALID;
import static kodanect.common.exception.config.MessageKeys.REPLY_CONTENTS_EMPTY;

/**
 *
 * 기증자 추모관 댓글 수정 요청 dto
 *
 * <p>replyPassword : 댓글 비밀번호</p>
 * <p>replyContents : 댓글 내용</p>
 *
 * */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@GroupSequence({MemorialReplyUpdateRequest.class, BlankGroup.class, PatternGroup.class})
public class MemorialReplyUpdateRequest {

    /* 댓글 비밀번호 */
    @NotBlank(message = REPLY_PASSWORD_EMPTY, groups = BlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9]{8,16}$", message = REPLY_PASSWORD_INVALID, groups = PatternGroup.class)
    private String replyPassword;

    /* 댓글 내용 */
    @NotBlank(message = REPLY_CONTENTS_EMPTY, groups = BlankGroup.class)
    private String replyContents;
}
