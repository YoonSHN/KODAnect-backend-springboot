package kodanect.domain.recipient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String commentWriter;
    private String contents;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime writeTime;
    private String delFlag;

    // Entity -> DTO 변환 메서드 (정적 팩토리 메서드)
    public static RecipientCommentResponseDto fromEntity(RecipientCommentEntity entity) {
        return RecipientCommentResponseDto.builder()
                .commentSeq(entity.getCommentSeq())
                .commentWriter(entity.getCommentWriter())
                .contents(entity.getContents())
                .writeTime(entity.getWriteTime())
                .delFlag(entity.getDelFlag())
                .build();
    }

    // CursorIdentifiable 인터페이스 구현
    @Override
    public Integer getCursorId() {
        return this.commentSeq; // 댓글 커서 ID로 commentSeq 사용
    }
}
