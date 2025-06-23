package kodanect.domain.heaven.dto.request;

import kodanect.domain.remembrance.dto.common.BlankGroup;
import kodanect.domain.remembrance.dto.common.PatternGroup;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static kodanect.common.exception.config.MessageKeys.BOARD_PASSCODE_EMPTY;
import static kodanect.common.exception.config.MessageKeys.BOARD_PASSCODE_INVALID;

@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@Builder
public class HeavenVerifyRequest {

    /* 편지 비밀번호 */
    @NotBlank(message = BOARD_PASSCODE_EMPTY, groups = BlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9]{8,16}$", message = BOARD_PASSCODE_INVALID, groups = PatternGroup.class)
    private String letterPasscode;
}
