package kodanect.domain.donation.dto.response;



import kodanect.domain.donation.entity.DonationStory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DonationStoryListDto {
    @NotNull
    private Long storySeq;
    @NotBlank
    private String storyTitle;
    @NotBlank
    private String storyWriter;
    @NotNull
    @Min(0)
    private Integer readCount;
    @NotNull
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
