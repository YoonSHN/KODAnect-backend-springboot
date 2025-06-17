package kodanect.domain.heaven.dto.response;

import kodanect.common.response.CursorCommentCountPaginationResponse;
import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.domain.heaven.entity.Heaven;
import kodanect.domain.remembrance.entity.Memorial;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@Builder
public class HeavenDetailResponse {

    /* 편지 일련번호 */
    private int letterSeq;

    /* 기증자 일련번호 */
    private Integer donateSeq;

    /* 편지 제목 */
    private String letterTitle;

    /* 기증자 명 */
    private String donorName;

    /* 편지 작성자 */
    private String letterWriter;

    /* 편지 익명여부 */
    private String anonymityFlag;

    /* 조회 건수 */
    private Integer readCount;

    /* 편지 내용 */
    private String letterContents;

    /* 이미지 파일 명 */
    private String fileName;

    /* 이미지 원본 파일 명 */
    private String orgFileName;

    /* 생성 일시 */
    private LocalDateTime writeTime;

    /* 댓글 리스트 */
    private CursorCommentPaginationResponse<HeavenCommentResponse, Integer> cursorCommentPaginationResponse;

    /* 생성 일시 형식화 */
    public String getWriteTime() {
        return writeTime.toLocalDate().toString();
    }

    public static HeavenDetailResponse of(
            Heaven heaven,
            CursorCommentCountPaginationResponse<HeavenCommentResponse, Integer> cursorCommentPaginationResponse
    ) {
        Memorial memorial = heaven.getMemorial();

        return HeavenDetailResponse.builder()
                .letterSeq(heaven.getLetterSeq())
                .donateSeq((memorial != null) ? memorial.getDonateSeq() : null)
                .letterTitle(heaven.getLetterTitle())
                .donorName(heaven.getDonorName())
                .letterWriter(heaven.getLetterWriter())
                .anonymityFlag(heaven.getAnonymityFlag())
                .readCount(heaven.getReadCount())
                .letterContents(heaven.getLetterContents())
                .fileName(heaven.getFileName())
                .orgFileName(heaven.getOrgFileName())
                .writeTime(heaven.getWriteTime())
                .cursorCommentPaginationResponse(cursorCommentPaginationResponse)
                .build();
    }
}
