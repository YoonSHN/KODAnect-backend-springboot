package kodanect.domain.remembrance.dto;

import kodanect.domain.remembrance.entity.Memorial;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialDetailResponse {

    /* 기증자 일련번호 */
    private Integer donateSeq;

    /* 기증자 명 */
    private String donorName;

    /* 익명 여부 Y, N */
    private String anonymityFlag;

    /* 추모합니다. */
    private String donateTitle;

    /* 기증자 내용 */
    private String contents;

    /* 이미지 파일 명 */
    private String fileName;

    /* 이미지 원본 파일 명 */
    private String orgFileName;

    /* 운영자 */
    private String writer;

    /* 기증자 기증 일시 20120101 */
    private String donateDate;

    /* 기증자 성별 */
    private String genderFlag;

    /* 기증자 나이 */
    private Integer donateAge;

    /* 추모수 헌화 */
    private int flowerCount;

    /* 추모수 사랑해요 */
    private int loveCount;

    /* 추모수 보고싶어요 */
    private int seeCount;

    /* 추모수 그리워요 */
    private int missCount;

    /* 추모수 자랑스러워요 */
    private int proudCount;

    /* 추모수 힘들어요 */
    private int hardCount;

    /* 추모수 슬퍼요 */
    private int sadCount;

    /* 생성 일시 */
    private LocalDateTime writeTime;

    /* 댓글 리스트 */
    private List<MemorialReplyResponse> memorialReplyResponseList;

    /* 기증자 상세 조회 */
    public static MemorialDetailResponse of(Memorial memorial, List<MemorialReplyResponse> replies) {
        return MemorialDetailResponse.builder()
                .donateSeq(memorial.getDonateSeq())
                .donorName(memorial.getDonorName())
                .anonymityFlag(memorial.getAnonymityFlag())
                .donateTitle(memorial.getDonateTitle())
                .contents(memorial.getContents())
                .fileName(memorial.getFileName())
                .orgFileName(memorial.getOrgFileName())
                .writer(memorial.getWriter())
                .donateDate(memorial.getDonateDate())
                .genderFlag(memorial.getGenderFlag())
                .donateAge(memorial.getDonateAge())
                .flowerCount(memorial.getFlowerCount())
                .loveCount(memorial.getLoveCount())
                .seeCount(memorial.getSeeCount())
                .missCount(memorial.getMissCount())
                .proudCount(memorial.getProudCount())
                .hardCount(memorial.getHardCount())
                .sadCount(memorial.getSadCount())
                .writeTime(memorial.getWriteTime())
                .memorialReplyResponseList(replies)
                .build();
    }
}

