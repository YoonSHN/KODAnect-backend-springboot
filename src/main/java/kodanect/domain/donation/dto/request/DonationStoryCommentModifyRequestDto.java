package kodanect.domain.donation.dto.request;


import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class DonationStoryCommentModifyRequestDto {
    @NotBlank(message="{donation.error.required.writer}")
    private String commentWriter;
    @NotBlank(message="{donation.content.blank}")
    private String contents;

}
