package kodanect.domain.donation.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationCommentCreateRequestDto {

    @NotBlank
    private String commentWriter;
    @NotNull
    private String commentPasscode;
    @NotBlank
    private String contents;
    @NotNull
    private String captchaToken;
}
