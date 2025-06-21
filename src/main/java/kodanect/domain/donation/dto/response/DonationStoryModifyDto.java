package kodanect.domain.donation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DonationStoryModifyDto {

    @NotNull(message="{donation.story.areaCode.null}")
    private AreaCode areaCode;
    @NotBlank(message = "{donation.error.required.title}")
    private String storyTitle;

    @NotBlank(message = "{donation.story.writer.blank}")
    private String storyWriter;

    private String storyContents;

    public static DonationStoryModifyDto fromEntity(DonationStoryDetailDto storyDetailDto){
        return DonationStoryModifyDto.builder()
                .areaCode(storyDetailDto.getAreaCode())
                .storyTitle(storyDetailDto.getStoryTitle())
                .storyWriter(storyDetailDto.getStoryWriter())
                .storyContents(storyDetailDto.getStoryContents()).build();
    }
}
