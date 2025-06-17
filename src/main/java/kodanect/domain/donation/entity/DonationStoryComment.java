package kodanect.domain.donation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import kodanect.domain.donation.dto.request.DonationStoryCommentModifyRequestDto;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name="tb25_421_donation_story_comment")
@AllArgsConstructor
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DonationStoryComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentSeq;

    @Column(name="comment_writer", length = 150)
    private String commentWriter;

    @Column(name="comment_passcode", length = 60)
    private String commentPasscode;

    @Column(columnDefinition="TEXT")
    private String contents;
    @Column(name="write_time", nullable = false, updatable= false)
    private LocalDateTime writeTime;
    @Column(name="writer_id",length = 60)
    private String writerId;

    @Column(name="modify_time", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
            insertable = false, updatable = false)
    private LocalDateTime modifyTime;
    @Column(name="modifier_id", length = 60)
    private String modifierId;
    @Column(name="del_flag", length = 1, nullable = false)
    private String delFlag;

    @ManyToOne
    @JoinColumn(name = "story_seq")
    @JsonIgnore
    @ToString.Exclude
    private DonationStory story;

    public void setStory(DonationStory story){ //연관관계 편의 메서드에서 호출
        this.story = story;
    }

    public void modifyDonationStoryComment(DonationStoryCommentModifyRequestDto requestDto) {
        this.commentWriter = requestDto.getCommentWriter();
        this.contents = requestDto.getContents();
    }
    @PrePersist
    protected void onCreate() {
        if (this.writeTime == null) {
            this.writeTime = LocalDateTime.now();
        }
        if (this.delFlag == null) {
            this.delFlag = "N";
        }
    }


}
