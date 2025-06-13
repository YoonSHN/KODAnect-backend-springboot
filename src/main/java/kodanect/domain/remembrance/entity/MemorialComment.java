package kodanect.domain.remembrance.entity;

import kodanect.domain.remembrance.dto.MemorialCommentCreateRequest;
import kodanect.domain.remembrance.exception.CommentAlreadyDeleteException;
import kodanect.domain.remembrance.exception.CommentPasswordMismatchException;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

/** 기증자 추모관 댓글 엔티티 클래스 */
@Entity(name = "MemorialComment")
@Table(name = "tb25_401_memorial_reply")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialComment {

    /* 디폴트 값 */
    /* 삭제 여부 기본값 (N) */
    private static final String DEFAULT_DEL_FLAG = "N";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_seq")
    /* 댓글 일련번호 */
    private Integer commentSeq;

    /* 기증자 일련번호 */
    @Column(nullable = false, name = "donate_seq")
    private Integer donateSeq;

    /* 댓글 작성 닉네임 */
    @Column(nullable = true, length = 150, name = "reply_writer")
    private String commentWriter;

    /* 댓글 작성 비밀번호*/
    @Column(nullable = true, length = 60, name = "reply_password")
    private String commentPasscode;

    /* 댓글 내용 */
    @Column(nullable = true, length = 3000, name = "reply_contents")
    private String contents;

    /* 댓글 등록일시 */
    @Column(updatable = false, name = "reply_write_time")
    private LocalDateTime writeTime;

    /* 댓글 작성자 아이디 */
    @Column(nullable = true, length = 60, name = "reply_writer_id")
    private String writerId;

    /* 댓글 수정시간 */
    @Column(name = "reply_modify_time")
    private LocalDateTime modifyTime;

    /* 댓글 수정자 아이디 */
    @Column(nullable = true, length = 60, name = "reply_modifier_id")
    private String modifierId;

    /* 삭제 여부 */
    @Column(nullable = false, length = 1, name = "del_flag")
    @Builder.Default private String delFlag = DEFAULT_DEL_FLAG;

    @PrePersist
    public void prePersist() {
        this.writeTime = LocalDateTime.now();
    }

    public void setDonateSeq(int donateSeq) {
        this.donateSeq = donateSeq;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public static MemorialComment of(MemorialCommentCreateRequest memorialCommentCreateRequest, Integer donateSeq) {
        return MemorialComment.builder()
                .donateSeq(donateSeq)
                .commentWriter(memorialCommentCreateRequest.getCommentWriter())
                .commentPasscode(memorialCommentCreateRequest.getCommentPasscode())
                .contents(memorialCommentCreateRequest.getContents())
                .build();
    }

    public void validateCommentPassword(String requestPassword) {
        if(!this.commentPasscode.equals(requestPassword)) {
            throw new CommentPasswordMismatchException(this.commentSeq);
        }
    }

    public void validateNotDeleted() {
        if(!"N".equals(this.delFlag)) {
            throw new CommentAlreadyDeleteException(this.commentSeq);
        }
    }

}

