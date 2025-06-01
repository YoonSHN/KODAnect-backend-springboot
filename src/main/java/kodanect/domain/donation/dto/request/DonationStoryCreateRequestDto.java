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

    @NotNull
    private AreaCode areaCode;
    @NotBlank
    private String storyTitle;

    @NotBlank
    @Size(min = 8, message = "{Size.donationStoryCreateRequestDto.storyPasscode}")
    private String storyPasscode;
    @NotBlank
    private String storyWriter;
    private String storyContents;

    @NotNull
    private String captchaToken; // hCaptcha가 전달한 캡차 인증 값
    private MultipartFile file;
}
