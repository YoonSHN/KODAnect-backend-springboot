package kodanect.domain.recipient.dto;

import kodanect.domain.recipient.entity.RecipientEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientResponseDto {

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
    private String delFlag; // char 타입으로 유지
    private int commentCount; // 댓글 수는 조회 시 필요한 정보이므로 DTO에 포함
    private List<RecipientCommentResponseDto> comments;

    // Entity -> DTO 변환 메서드 (정적 팩토리 메서드)
    public static RecipientResponseDto fromEntity(RecipientEntity entity) {
        RecipientResponseDto dto = RecipientResponseDto.builder()
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
                .build();

        // 댓글이 로딩된 경우에만 DTO로 변환하여 포함
        if (entity.getComments() != null) {
            // 삭제되지 않은 댓글만 DTO로 변환
            dto.setComments(entity.getComments().stream()
                    .filter(comment -> "N".equalsIgnoreCase(comment.getDelFlag()))
                    .map(RecipientCommentResponseDto::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
