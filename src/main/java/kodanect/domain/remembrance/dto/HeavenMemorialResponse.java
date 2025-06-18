package kodanect.domain.remembrance.dto;

/**
 *
 * 기증자 추모관 게시글 응답 dto
 *
 * <p>donateSeq : 게시글 번호</p>
 * <p>donorName : 기증자 명</p>
 * <p>donateDate : 기증 일시</p>
 * <p>genderFlag : 기증자 성별</p>
 * <p>donateAge : 기증자 나이</p>
 *
 * */
public interface HeavenMemorialResponse {

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
}
