package kodanect.domain.remembrance.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

import static kodanect.common.exception.config.MessageKeys.REPLY_CONTENTS_EMPTY;
import static kodanect.common.exception.config.MessageKeys.REPLY_WRITER_EMPTY;

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
public class MemorialReplyUpdateRequest {

    /* 댓글 내용 */
    @NotBlank(message = REPLY_CONTENTS_EMPTY)
    private String replyContents;

    /* 댓글 작성자 닉네임 */
    @NotBlank(message = REPLY_WRITER_EMPTY)
    private String replyWriter;
}
