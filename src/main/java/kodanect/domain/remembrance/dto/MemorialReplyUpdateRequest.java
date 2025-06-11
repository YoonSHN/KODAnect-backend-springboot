package kodanect.domain.remembrance.dto;

import kodanect.domain.remembrance.dto.common.BlankGroup;
import kodanect.domain.remembrance.dto.common.PatternGroup;
import lombok.*;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static kodanect.common.exception.config.MessageKeys.REPLY_CONTENTS_EMPTY;
import static kodanect.common.exception.config.MessageKeys.REPLY_WRITER_EMPTY;
import static kodanect.common.exception.config.MessageKeys.REPLY_WRITER_INVALID;

/**
 *
 * 기증자 추모관 댓글 수정 요청 dto
 *
 * <p>replyPassword : 댓글 비밀번호</p>
 * <p>replyContents : 댓글 내용</p>
 * <p>replyWriter : 댓글 작성자 닉네임</p>
 *
 * */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@GroupSequence({MemorialReplyUpdateRequest.class, BlankGroup.class, PatternGroup.class})
public class MemorialReplyUpdateRequest {

    /* 댓글 내용 */
    @NotBlank(message = REPLY_CONTENTS_EMPTY, groups = BlankGroup.class)
    private String replyContents;

    /* 댓글 작성자 닉네임 */
    @NotBlank(message = REPLY_WRITER_EMPTY, groups = BlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z가-힣\\s]{1,30}$", message = REPLY_WRITER_INVALID, groups = PatternGroup.class)
    private String replyWriter;
}
