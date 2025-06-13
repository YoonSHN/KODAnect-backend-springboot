package kodanect.domain.donation.dto.request;


import kodanect.domain.donation.dto.response.AreaCode;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class DonationStoryCreateRequestDto {

    @NotNull(message="donation.story.areaCode.null")
    private AreaCode areaCode;
    @NotBlank(message="donation.story.title.blank")
    private String storyTitle;

    @NotBlank(message="donation.story.passcode.blank")
    @Size(min = 8, message = "{Size.donationStoryCreateRequestDto.storyPasscode}")
    private String storyPasscode;
    @NotBlank(message="donation.story.passcode.blank")
    private String storyWriter;
    private String storyContents;

}
