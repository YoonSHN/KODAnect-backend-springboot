package kodanect.domain.remembrance.dto;

import kodanect.common.util.CursorIdentifiable;
import kodanect.domain.remembrance.dto.common.MemorialNextCursor;

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
public interface MemorialResponse extends CursorIdentifiable<MemorialNextCursor> {

    /* 기증자 일련번호 */
    Integer getDonateSeq();

    /* 기증자 명 */
    String getDonorName();

    /* 기증자 기증 일시 20120101 */
    String getDonateDate();

    /* 기증자 성별 */
    String getGenderFlag();

    /* 기증자 나이 */
    Integer getDonateAge();

    /* 댓글 개수 조회 */
    long getCommentCount();

    /* 하늘나라 편지 개수 조회 */
    long getLetterCount();

    @Override
    default MemorialNextCursor getCursorId() {
        return new MemorialNextCursor(getDonateSeq(), getDonateDate());
    }

}

