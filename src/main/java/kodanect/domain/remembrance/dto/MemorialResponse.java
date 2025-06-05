package kodanect.domain.remembrance.dto;

import kodanect.common.util.CursorIdentifiable;
import kodanect.common.util.FormatUtils;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialResponse implements CursorIdentifiable<Integer> {

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
    private long replyCount;

    /** 20101212 -> 2010-12-12 형식 변경 */
    public String getDonateDate() {
        return FormatUtils.formatDonateDate(this.donateDate);
    }

    @Override
    public Integer getCursorId() {
        return donateSeq;
    }

}

