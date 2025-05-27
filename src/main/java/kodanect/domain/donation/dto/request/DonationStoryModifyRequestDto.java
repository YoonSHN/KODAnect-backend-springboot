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

    @NotNull
    private AreaCode areaCode;
    @NotBlank
    private String storyTitle;

    @NotBlank
    private String storyWriter;
    private String storyContents;

    private MultipartFile file;
    @NotNull
    private String captchaToken; // hCaptcha가 전달한 캡차 인증 값
}
