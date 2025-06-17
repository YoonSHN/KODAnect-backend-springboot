package kodanect.domain.heaven.dto.response;

import kodanect.common.util.CursorIdentifiable;
import lombok.*;

import java.time.LocalDateTime;

/* 기증자 추모관 상세 조회 시 하늘나라 편지 리스트 조회할 때 사용 */
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@Builder
public class MemorialHeavenResponse implements CursorIdentifiable<Integer> {

    /* 편지 일련번호 */
    private int letterSeq;

    /* 편지 제목 */
    private String letterTitle;

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
