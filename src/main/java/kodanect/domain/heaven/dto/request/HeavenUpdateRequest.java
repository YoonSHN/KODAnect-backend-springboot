package kodanect.domain.heaven.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @ToString
@Builder
public class HeavenUpdateRequest {

    /* 편지 작성자 */
    private String letterWriter;

    /* 편지 익명 여부 */
    private String anonymityFlag;

    /* 기증자 명 */
    private String donorName;

    /* 기증자 일련번호 */
    private int donateSeq;

    /* 편지 제목 */
    private String letterTitle;

    /* 편지 내용 */
    private String letterContents;
}
