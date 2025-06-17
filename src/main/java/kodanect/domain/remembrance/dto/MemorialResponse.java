package kodanect.domain.remembrance.dto;

import kodanect.common.util.CursorIdentifiable;
import kodanect.common.util.FormatUtils;
import kodanect.domain.remembrance.dto.common.MemorialNextCursor;
import lombok.*;

/**
 *
 * 기증자 추모관 게시글 응답 dto
 *
 * <p>donateSeq : 게시글 번호</p>
 * <p>donorName : 기증자 명</p>
 * <p>anonymityFlag : 익명 여부 Y, N</p>
 * <p>donateDate : 기증 일시</p>
 * <p>genderFlag : 기증자 성별</p>
 * <p>donateAge : 기증자 나이</p>
 * <p>commentCount : 댓글 개수</p>
 *
 * */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialResponse implements CursorIdentifiable<MemorialNextCursor> {

    /* 기증자 일련번호 */
    private Integer donateSeq;

    /* 기증자 명 */
    private String donorName;

    /* 익명 여부 Y, N */
    private String anonymityFlag;

    /* 기증자 기증 일시 20120101 */
    private String donateDate;

    /* 기증자 성별 */
    private String genderFlag;

    /* 기증자 나이 */
    private Integer donateAge;

    /* 댓글 개수 조회 */
    private long commentCount;

    /** 20101212 -> 2010-12-12 형식 변경 메서드 */
    public String getDonateDate() {
        return FormatUtils.formatDonateDate(this.donateDate);
    }

    @Override
    public MemorialNextCursor getCursorId() {
        return new MemorialNextCursor(donateSeq, donateDate);
    }

}

