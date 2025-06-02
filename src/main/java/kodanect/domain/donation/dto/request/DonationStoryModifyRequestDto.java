package kodanect.domain.donation.dto.request;


import kodanect.domain.donation.dto.response.AreaCode;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class DonationStoryModifyRequestDto {

    @NotNull(message="donation.story.areaCode.null")
    private AreaCode areaCode;
    @NotBlank(message = "{donation.error.required.title}")
    private String storyTitle;

    @NotBlank(message = "{donation.story.writer.blank}")
    private String storyWriter;
    private String storyContents;

    private MultipartFile file;
    @NotBlank(message = "donation.captcha.token.blank")
    private String captchaToken; // hCaptcha가 전달한 캡차 인증 값
}
