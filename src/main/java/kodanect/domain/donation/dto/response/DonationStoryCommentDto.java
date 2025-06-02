package kodanect.domain.donation.dto.response;


import kodanect.domain.donation.entity.DonationStoryComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DonationStoryCommentDto {

    private Long commentSeq;

    private String commentWriter; //추모자

    private String comments;
    private LocalDateTime commentWriteTime;

    public DonationStoryCommentDto(Long commentSeq, String commentWriter, String comments, LocalDateTime commentWriteTime) {
        this.commentSeq = commentSeq;
        this.commentWriter = commentWriter;
        this.comments = comments;
        this.commentWriteTime = commentWriteTime;
    }

    public static DonationStoryCommentDto fromEntity(DonationStoryComment domainStoryComment) {
        return DonationStoryCommentDto.builder()
                .commentSeq(domainStoryComment.getCommentSeq())
                .commentWriter(domainStoryComment.getCommentWriter())
                .comments(domainStoryComment.getContents())
                .commentWriteTime(domainStoryComment.getWriteTime()).build();
    }
}
