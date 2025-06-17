package kodanect.domain.heaven.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@Builder
public class HeavenCommentVerifyRequest {

    /* 댓글 비밀번호 */
    private String commentPasscode;
}
