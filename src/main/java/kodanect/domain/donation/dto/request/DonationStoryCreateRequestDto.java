package kodanect.domain.donation.dto.request;


import kodanect.domain.donation.dto.response.AreaCode;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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

    @NotBlank(message="donation.captcha.token.blank")
    private String captchaToken; // hCaptcha가 전달한 캡차 인증 값
    private MultipartFile file;
}
