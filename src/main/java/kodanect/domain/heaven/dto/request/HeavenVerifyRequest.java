package kodanect.domain.heaven.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@Builder
public class HeavenVerifyRequest {

    /* 편지 비밀번호 */
    private String letterPasscode;
}
