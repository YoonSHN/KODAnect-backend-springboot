package kodanect.domain.donation.dto.response;


import kodanect.domain.donation.entity.DonationStory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DonationStoryListDto {

    private Long storySeq;

    private String storyTitle;
    private String storyWriter;

    private Integer readCount;
    private LocalDateTime writeTime;

    public static DonationStoryListDto fromEntity(DonationStory story){ //정적 팩토리 메서드(DTO변환용)
        return DonationStoryListDto.builder()
                .storySeq(story.getStorySeq())
                .storyTitle(story.getStoryTitle())
                .storyWriter(story.getStoryWriter())
                .readCount(story.getReadCount() != null ? story.getReadCount() : 0)
                .writeTime(story.getWriteTime() != null ? story.getWriteTime() : LocalDateTime.now())
                .build();
    }
}
