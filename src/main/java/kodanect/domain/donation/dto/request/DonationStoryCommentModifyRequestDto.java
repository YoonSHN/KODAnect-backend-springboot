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
    @NotBlank(message="donation.error.required.writer")
    private String commentWriter;
    @NotBlank(message="donation.content.blank")
    private String commentContents;

}
