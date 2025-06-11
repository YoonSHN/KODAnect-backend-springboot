package kodanect.domain.recipient.dto;

import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.domain.recipient.entity.RecipientEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientDetailResponseDto {

    private Integer letterSeq;
    private String organCode;
    private String organEtc;
    private String letterTitle;
    private String recipientYear;
    private String letterWriter;
    private String anonymityFlag;
    private int readCount;
    private String letterContents;
    private String fileName;
    private String orgFileName;
    private LocalDateTime writeTime;
    private String writerId;
    private LocalDateTime modifyTime;
    private String modifierId;
    private String delFlag;
    private int commentCount;        // 댓글 수는 조회 시 필요한 정보이므로 DTO에 포함
    private boolean hasMoreComments; // 추가 댓글이 있는지 여부
    private String imageUrl;         // 게시물에 등록된 이미지의 URL
    // 게시물 조회 시 초기 댓글 데이터를 CursorReplyPaginationResponse 형태로 포함
    private CursorReplyPaginationResponse<RecipientCommentResponseDto, Integer> recipientCommentData;

    /** 2020-12-13T02:11:12 -> 2020-12-13 형식 변경 */
    public String getWriteTime() {
        return writeTime.toLocalDate().toString();
    }
    public String getModifyTime() {
        return modifyTime.toLocalDate().toString();
    }

    // Entity -> DTO 변환 메서드 (정적 팩토리 메서드)
    public static RecipientDetailResponseDto fromEntity(RecipientEntity entity) {

        return RecipientDetailResponseDto.builder() // 빌더로 객체를 생성한 결과를 바로 반환
                .letterSeq(entity.getLetterSeq())
                .organCode(entity.getOrganCode())
                .organEtc(entity.getOrganEtc())
                .letterTitle(entity.getLetterTitle())
                .recipientYear(entity.getRecipientYear())
                .letterWriter(entity.getLetterWriter())
                .anonymityFlag(entity.getAnonymityFlag())
                .readCount(entity.getReadCount())
                .letterContents(entity.getLetterContents())
                .fileName(entity.getFileName())
                .orgFileName(entity.getOrgFileName())
                .writeTime(entity.getWriteTime())
                .writerId(entity.getWriterId())
                .modifyTime(entity.getModifyTime())
                .modifierId(entity.getModifierId())
                .delFlag(entity.getDelFlag())
                .commentCount(0)
                .hasMoreComments(false)
                .imageUrl(entity.getFileName()) // RecipientEntity의 fileName을 이미지 URL로 활용
                // 초기에는 댓글 데이터를 비워두고, 서비스 계층에서 설정
                .recipientCommentData(null) // 초기화 시 null 또는 기본 빈 객체
                .build();
    }
    // 서비스 계층에서 초기 댓글 데이터를 설정하기 위한 setter
    public void setRecipientCommentData(CursorReplyPaginationResponse<RecipientCommentResponseDto, Integer> recipientCommentData) {
        this.recipientCommentData = recipientCommentData;
    }
}
