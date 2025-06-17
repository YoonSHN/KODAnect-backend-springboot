package kodanect.domain.recipient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kodanect.common.util.CursorIdentifiable;
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
public class RecipientListResponseDto implements CursorIdentifiable<Integer> {

    private Integer letterSeq;
    private String letterTitle;
    private String letterWriter;
    private int readCount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime writeTime;

    // Entity -> DTO 변환 메서드 (정적 팩토리 메서드)
    public static RecipientListResponseDto fromEntity(RecipientEntity entity) {
        return RecipientListResponseDto.builder()
                .letterSeq(entity.getLetterSeq())
                .letterTitle(entity.getLetterTitle())
                .letterWriter(entity.getLetterWriter())
                .readCount(entity.getReadCount())
                .writeTime(entity.getWriteTime())
//                .displayLetterNum(null)
                .build();
    }

    // CursorIdentifiable 인터페이스 구현
    @Override
    public Integer getCursorId() {
        return this.letterSeq; // 게시물 커서 ID로 letterSeq 사용
    }
}
