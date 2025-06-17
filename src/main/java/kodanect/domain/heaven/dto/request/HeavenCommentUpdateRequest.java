package kodanect.domain.heaven.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@Builder
public class HeavenCommentUpdateRequest {

    /* 댓글 작성자 */
    private String commentWriter;

    /* 댓글 내용 */
    private String contents;
}
