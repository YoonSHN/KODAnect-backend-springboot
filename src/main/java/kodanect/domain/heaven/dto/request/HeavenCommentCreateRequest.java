package kodanect.domain.heaven.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@Builder
public class HeavenCommentCreateRequest {

    /* 댓글 작성자 */
    private String commentWriter;

    /* 댓글 비밀번호 */
    private String commentPasscode;

    /* 댓글 내용 */
    private String contents;
}
