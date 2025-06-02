package kodanect.domain.remembrance.dto;

import kodanect.domain.remembrance.dto.common.ReplyContentHolder;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialReplyCreateRequest implements ReplyContentHolder {

    /* 게시글 일련번호 */
    private Integer donateSeq;

    /* 댓글 작성 닉네임 */
    private String replyWriter;

    /* 댓글 비밀번호 */
    private String replyPassword;

    /* 댓글 내용 */
    private String replyContents;
}
