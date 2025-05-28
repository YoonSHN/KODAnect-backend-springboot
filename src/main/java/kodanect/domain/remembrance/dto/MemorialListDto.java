package kodanect.domain.remembrance.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialListDto {

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

    /* 삭제 여부 */
    private String delFlag;

    /* 댓글 개수 조회 */
    private long replyCount;
}

