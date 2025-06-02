package kodanect.domain.remembrance.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialReplyResponse {

    /* 댓글 일련번호 */
    private Integer replySeq;

    /* 댓글 작성 닉네임 */
    private String replyWriter;

    /* 댓글 내용 */
    private String replyContents;

    /* 댓글 등록일시 */
    private LocalDateTime replyWriteTime;
}

