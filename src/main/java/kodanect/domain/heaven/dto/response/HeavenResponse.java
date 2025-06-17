package kodanect.domain.heaven.dto.response;

import kodanect.common.util.CursorIdentifiable;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@Builder
public class HeavenResponse implements CursorIdentifiable<Integer> {

    /* 편지 일련번호 */
    private int letterSeq;

    /* 편지 제목 */
    private String letterTitle;

    /* 기증자 명 */
    private String donorName;

    /* 편지 작성자 */
    private String letterWriter;

    /* 편지 익명 여부 */
    private String anonymityFlag;

    /* 조회 건수 */
    private Integer readCount;

    /* 생성 일시 */
    private LocalDateTime writeTime;

    /* 생성 일시 형식화 */
    public String getWriteTime() {
        return writeTime.toLocalDate().toString();
    }

    @Override
    public Integer getCursorId() {
        return letterSeq;
    }
}
