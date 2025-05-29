package kodanect.domain.remembrance.dto;

import kodanect.domain.remembrance.dto.common.ReplyAuthRequest;
import kodanect.domain.remembrance.dto.common.ReplyContentHolder;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialReplyUpdateRequest implements ReplyAuthRequest, ReplyContentHolder {

    /* 게시글 일련번호 */
    private Integer donateSeq;

    /* 댓글 일련번호 */
    private Integer replySeq;

    /* 댓글 비밀번호 */
    private String replyPassword;

    /* 댓글 내용 */
    private String replyContents;
}
