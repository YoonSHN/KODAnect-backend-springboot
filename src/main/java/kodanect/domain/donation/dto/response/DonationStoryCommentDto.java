package kodanect.domain.donation.dto.response;


import kodanect.domain.donation.entity.entity.DonationStoryComment;
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

    public static DonationStoryCommentDto fromEntity(DonationStoryComment domainStoryComment) {
        return DonationStoryCommentDto.builder()
                .commentSeq(domainStoryComment.getCommentSeq())
                .commentWriter(domainStoryComment.getCommentWriter())
                .comments(domainStoryComment.getContents())
                .commentWriteTime(domainStoryComment.getWriteTime()).build();
    }
}
