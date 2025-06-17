package kodanect.domain.heaven.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @ToString
@Builder
public class HeavenCreateRequest {

    /* 편지 작성자 */
    private String letterWriter;

    /* 편지 익명 여부 */
    private String anonymityFlag;

    /* 편지 비밀번호 */
    private String letterPasscode;

    /* 기증자 명 */
    private String donorName;

    /* 기증자 일련번호 */
    private int donateSeq;

    /* 편지 제목 */
    private String letterTitle;

    /* 편지 내용 */
    private String letterContents;

    /* 파일 */
    private MultipartFile file;
}
