package kodanect.domain.remembrance.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import kodanect.domain.remembrance.entity.RecipientCommentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientCommentResponseDto {

    private Integer commentSeq;
    private Integer letterSeq; // 부모 RecipientEntity의 ID만 포함
    private String commentWriter;
    // private String commentPasscode; // 응답 시에는 비밀번호 제외 (매우 중요)
    private String contents;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime writeTime;
    private String writerId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifyTime;
    private String modifierId;

    private char delFlag;

    // Entity -> DTO 변환 메서드 (정적 팩토리 메서드)
    public static RecipientCommentResponseDto fromEntity(RecipientCommentEntity entity) {
        return RecipientCommentResponseDto.builder()
                .commentSeq(entity.getCommentSeq())
                .letterSeq(entity.getLetter() != null ? entity.getLetter().getLetterSeq() : null) // RecipientEntity가 null이 아닐 때만 ID 가져옴
                .commentWriter(entity.getCommentWriter())
                .contents(entity.getContents())
                .writeTime(entity.getWriteTime())
                .writerId(entity.getWriterId())
                .modifyTime(entity.getModifyTime())
                .modifierId(entity.getModifierId())
                .delFlag(entity.getDelFlag())
                .build();
    }
}
