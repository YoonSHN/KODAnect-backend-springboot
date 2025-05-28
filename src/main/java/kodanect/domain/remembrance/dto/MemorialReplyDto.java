package kodanect.domain.remembrance.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialReplyDto {

    /* 댓글 일련번호 */
    private Integer replySeq;

    /* 기증자 일련번호 */
    private Integer donateSeq;

    /* 댓글 작성 닉네임 */
    private String replyWriter;

    /* 댓글 작성 비밀번호*/
    private String replyPassword;

    /* 댓글 내용 */
    private String replyContents;

    /* 댓글 등록일시 */
    private LocalDateTime replyWriteTime;

    /* 댓글 작성자 아이디 */
    private String replyWriterId;

    /* 댓글 수정시간 */
    private LocalDateTime replyModifyTime;

    /* 댓글 수정자 아이디 */
    private String replyModifierId;

    /* 삭제 여부 */
    private String delFlag;
}

