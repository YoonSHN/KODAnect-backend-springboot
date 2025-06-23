package kodanect.domain.heaven.dto.request;

import kodanect.domain.remembrance.dto.common.BlankGroup;
import kodanect.domain.remembrance.dto.common.PatternGroup;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static kodanect.common.exception.config.MessageKeys.COMMENT_WRITER_EMPTY;
import static kodanect.common.exception.config.MessageKeys.COMMENT_WRITER_INVALID;
import static kodanect.common.exception.config.MessageKeys.COMMENT_PASSWORD_EMPTY;
import static kodanect.common.exception.config.MessageKeys.COMMENT_PASSWORD_INVALID;
import static kodanect.common.exception.config.MessageKeys.COMMENT_CONTENTS_EMPTY;

@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@Builder
public class HeavenCommentCreateRequest {

    /* 댓글 작성자 */
    @NotBlank(message = COMMENT_WRITER_EMPTY, groups = BlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z가-힣\\s]{1,10}$", message = COMMENT_WRITER_INVALID, groups = PatternGroup.class)
    private String commentWriter;

    /* 댓글 비밀번호 */
    @NotBlank(message = COMMENT_PASSWORD_EMPTY, groups = BlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9]{8,16}$", message = COMMENT_PASSWORD_INVALID, groups = PatternGroup.class)
    private String commentPasscode;

    /* 댓글 내용 */
    @NotBlank(message = COMMENT_CONTENTS_EMPTY, groups = BlankGroup.class)
    private String contents;
}
