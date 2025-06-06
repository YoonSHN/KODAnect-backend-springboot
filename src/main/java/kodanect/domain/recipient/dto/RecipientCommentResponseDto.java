package kodanect.domain.recipient.dto;

import kodanect.common.util.CursorIdentifiable;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientCommentResponseDto implements CursorIdentifiable<Integer> {

    private Integer commentSeq;
    private Integer letterSeq;
    private String commentWriter;
    private String commentContents;
    private LocalDateTime writeTime;
    private LocalDateTime modifyTime;
    private String delFlag;

    // Entity -> DTO 변환 메서드 (정적 팩토리 메서드)
    public static RecipientCommentResponseDto fromEntity(RecipientCommentEntity entity) {
        if (entity.getDelFlag() != null && entity.getDelFlag().equalsIgnoreCase("Y")) {
            return null;
        }
        return RecipientCommentResponseDto.builder()
                .commentSeq(entity.getCommentSeq())
                .letterSeq(entity.getLetterSeq().getLetterSeq())
                .commentWriter(entity.getCommentWriter())
                .commentContents(entity.getCommentContents())
                .writeTime(entity.getWriteTime())
                .modifyTime(entity.getModifyTime())
                .delFlag(entity.getDelFlag())
                .build();
    }

    // CursorIdentifiable 인터페이스 구현
    @Override
    public Integer getCursorId() {
        return this.commentSeq; // 댓글 커서 ID로 commentSeq 사용
    }
}
