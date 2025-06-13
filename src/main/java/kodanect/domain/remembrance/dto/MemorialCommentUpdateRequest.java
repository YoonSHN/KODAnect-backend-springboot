package kodanect.domain.remembrance.dto;

import kodanect.domain.remembrance.dto.common.BlankGroup;
import kodanect.domain.remembrance.dto.common.PatternGroup;
import lombok.*;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static kodanect.common.exception.config.MessageKeys.COMMENT_CONTENTS_EMPTY;
import static kodanect.common.exception.config.MessageKeys.COMMENT_WRITER_EMPTY;
import static kodanect.common.exception.config.MessageKeys.COMMENT_WRITER_INVALID;

/**
 *
 * 기증자 추모관 댓글 수정 요청 dto
 *
 * <p>contents : 댓글 내용</p>
 * <p>commentWriter : 댓글 작성자 닉네임</p>
 *
 * */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@GroupSequence({MemorialCommentUpdateRequest.class, BlankGroup.class, PatternGroup.class})
public class MemorialCommentUpdateRequest {

    /* 댓글 내용 */
    @NotBlank(message = COMMENT_CONTENTS_EMPTY, groups = BlankGroup.class)
    private String contents;

    /* 댓글 작성자 닉네임 */
    @NotBlank(message = COMMENT_WRITER_EMPTY, groups = BlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z가-힣\\s]{1,30}$", message = COMMENT_WRITER_INVALID, groups = PatternGroup.class)
    private String commentWriter;
}
