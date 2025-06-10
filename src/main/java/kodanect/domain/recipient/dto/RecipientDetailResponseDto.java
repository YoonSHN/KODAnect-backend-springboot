package kodanect.domain.recipient.dto;

import kodanect.domain.recipient.entity.RecipientEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<RecipientCommentResponseDto> topComments; // 초기에 보여줄 댓글 목록 필드

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
                .topComments(List.of())
                .build();
    }
    // 서비스 계층에서 댓글 관련 데이터 (총 댓글 수, 더보기 여부, 상위 N개 댓글)를 설정하기 위한 setter
    public void setCommentData(int commentCount, boolean hasMoreComments, List<RecipientCommentResponseDto> topComments) {
        this.commentCount = commentCount;
        this.hasMoreComments = hasMoreComments;
        this.topComments = topComments;
    }
}
