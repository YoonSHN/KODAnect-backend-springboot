package kodanect.domain.donation.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DonationStoryCommentModifyRequestDto {
    @NotBlank
    private String commentWriter;
    @NotBlank
    private String commentContents;
    @NotBlank
    private String commentPasscode;
    @NotBlank
    private String captchaToken;

}
