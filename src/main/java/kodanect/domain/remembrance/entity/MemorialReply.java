package kodanect.domain.remembrance.entity;

import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.exception.ReplyAlreadyDeleteException;
import kodanect.domain.remembrance.exception.ReplyPasswordMismatchException;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

/** 기증자 추모관 댓글 엔티티 클래스 */
@Entity(name = "MemorialReply")
@Table(name = "tb25_401_memorial_reply")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialReply {

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
    @Column(nullable = true, length = 150)
    private String replyWriter;

    /* 댓글 작성 비밀번호*/
    @Column(nullable = true, length = 60)
    private String replyPassword;

    /* 댓글 내용 */
    @Column(nullable = true, length = 3000)
    private String replyContents;

    /* 댓글 등록일시 */
    @Column(updatable = false)
    private LocalDateTime replyWriteTime;

    /* 댓글 작성자 아이디 */
    @Column(nullable = true, length = 60)
    private String replyWriterId;

    /* 댓글 수정시간 */
    private LocalDateTime replyModifyTime;

    /* 댓글 수정자 아이디 */
    @Column(nullable = true, length = 60)
    private String replyModifierId;

    /* 삭제 여부 */
    @Column(nullable = false, length = 1)
    @Builder.Default private String delFlag = DEFAULT_DEL_FLAG;

    @PrePersist
    public void prePersist() {
        this.replyWriteTime = LocalDateTime.now();
    }

    public void setDonateSeq(int donateSeq) {
        this.donateSeq = donateSeq;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public static MemorialReply of(MemorialReplyCreateRequest memorialReplyCreateRequest, Integer donateSeq) {
        return MemorialReply.builder()
                .donateSeq(donateSeq)
                .replyWriter(memorialReplyCreateRequest.getReplyWriter())
                .replyPassword(memorialReplyCreateRequest.getReplyPassword())
                .replyContents(memorialReplyCreateRequest.getReplyContents())
                .build();
    }

    public void validateReplyPassword(String requestPassword) {
        if(!this.replyPassword.equals(requestPassword)) {
            throw new ReplyPasswordMismatchException(this.replySeq);
        }
    }

    public void validateNotDeleted() {
        if(!"N".equals(this.delFlag)) {
            throw new ReplyAlreadyDeleteException(this.replySeq);
        }
    }

}

