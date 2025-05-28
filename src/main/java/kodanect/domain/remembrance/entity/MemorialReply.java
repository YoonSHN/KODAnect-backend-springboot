package kodanect.domain.remembrance.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "tb25_401_memorial_reply")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialReply {

    /* 상수 */
    /* 댓글 작성 닉네임 길이 */
    private static final int REPLY_WRITER_MAX_LENGTH = 150;
    /* 댓글 작성 비밀번호 길이 */
    private static final int REPLY_PASSWORD_MAX_LENGTH = 60;
    /* 댓글 내용 길이 */
    private static final int REPLY_CONTENTS_MAX_LENGTH = 3000;
    /* 댓글 작성자 아이디 길이 */
    private static final int REPLY_WRITER_ID_MAX_LENGTH = 60;
    /* 댓글 수정자 아이디 길이 */
    private static final int REPLY_MODIFIER_ID_MAX_LENGTH = 60;
    /* 삭제 여부 플래그 길이 */
    private static final int FLAG_LENGTH = 1;

    /* 디폴트 값 */
    /* 삭제 여부 기본값 (N) */
    private static final String DEFAULT_DEL_FLAG = "N";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /* 댓글 일련번호 */
    private Integer replySeq;

    /* 기증자 일련번호 */
    @Column(nullable = false)
    private Integer donateSeq;

    /* 댓글 작성 닉네임 */
    @Column(nullable = true, length = REPLY_WRITER_MAX_LENGTH)
    private String replyWriter;

    /* 댓글 작성 비밀번호*/
    @Column(nullable = true, length = REPLY_PASSWORD_MAX_LENGTH)
    private String replyPassword;

    /* 댓글 내용 */
    @Column(nullable = true, length = REPLY_CONTENTS_MAX_LENGTH)
    private String replyContents;

    /* 댓글 등록일시 */
    @Column(updatable = false)
    private LocalDateTime replyWriteTime;

    /* 댓글 작성자 아이디 */
    @Column(nullable = true, length = REPLY_WRITER_ID_MAX_LENGTH)
    private String replyWriterId;

    /* 댓글 수정시간 */
    private LocalDateTime replyModifyTime;

    /* 댓글 수정자 아이디 */
    @Column(nullable = true, length = REPLY_MODIFIER_ID_MAX_LENGTH)
    private String replyModifierId;

    /* 삭제 여부 */
    @Column(nullable = false, length = FLAG_LENGTH)
    @Builder.Default private String delFlag = DEFAULT_DEL_FLAG;

    @PrePersist
    public void prePersist() {
        this.replyWriteTime = LocalDateTime.now();
    }

    public void setDonateSeq(int donateSeq) {
        this.donateSeq = donateSeq;
    }

    public void setReplyContents(String replyContents) {
        this.replyContents = replyContents;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }
}

