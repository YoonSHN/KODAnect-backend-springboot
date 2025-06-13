package kodanect.domain.remembrance.dto;

import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.common.util.FormatUtils;
import kodanect.domain.remembrance.entity.Memorial;
import lombok.*;

import java.time.LocalDateTime;

/**
 *
 * 기증자 추모관 게시글 응답 dto
 *
 * <p>donateSeq : 게시글 번호</p>
 * <p>donorName : 기증자 명</p>
 * <p>anonymityFlag : 익명 여부 Y, N</p>
 * <p>donateTitle : 제목</p>
 * <p>contents : 기증자 내용</p>
 * <p>fileName : 이미지 파일 명</p>
 * <p>orgFileName : 이미지 원본 파일 명</p>
 * <p>writer : 작성자</p>
 * <p>donateDate : 기증자 기증 일시</p>
 * <p>genderFlag : 기증자 성별</p>
 * <p>donateAge : 기증자 나이</p>
 * <p>flowerCount : 이모지 헌화</p>
 * <p>loveCount : 이모지 사랑해요</p>
 * <p>seeCount : 이모지 보고싶어요</p>
 * <p>missCount : 이모지 그리워요</p>
 * <p>proudCount : 이모지 자랑스러워요</p>
 * <p>hardCount : 이모지 힘들어요</p>
 * <p>sadCount : 이모지 슬퍼요</p>
 * <p>writeTime : 생성 일시</p>
 * <p>memorialCommentResponses : 댓글 리스트</p>
 * <p>commentNextCursor : 다음 페이지 번호</p>
 * <p>commentHasNext : 다음 페이지 존재 유무</p>
 * <p>totalCommentCount : 총 댓글 갯수</p>
 * <p>heavenLetterResponses : </p>
 *
 * */
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
    private CursorCommentPaginationResponse<MemorialCommentResponse, Integer> memorialCommentResponses;

    /* 편지 리스트 */

    /** 20101212 -> 2010-12-12 형식 변경 */
    public String getDonateDate() {
        return FormatUtils.formatDonateDate(this.donateDate);
    }

    /** 2020-12-13T02:11:12 -> 2020-12-13 형식 변경 */
    public String getWriteTime() {
        return writeTime.toLocalDate().toString();
    }

    /** 기증자 상세 조회 객체 생성 메서드 */
    public static MemorialDetailResponse of(
            Memorial memorial, CursorCommentPaginationResponse<MemorialCommentResponse, Integer> replies)
    {
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
                .memorialCommentResponses(replies)
                .build();
    }
}

