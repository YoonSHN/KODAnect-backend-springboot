package kodanect.domain.recipient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kodanect.common.util.CursorIdentifiable;
import kodanect.domain.recipient.entity.RecipientEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientListResponseDto implements CursorIdentifiable<Integer> {

    private Integer letterSeq;
    private String letterTitle;
    private String letterWriter;
    private String anonymityFlag;
    private int readCount;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime writeTime;

    // Entity -> DTO 변환 메서드 (정적 팩토리 메서드)
    public static RecipientListResponseDto fromEntity(RecipientEntity entity, String anonymousWriterValue) {
        // 익명 처리 로직을 DTO 변환 시점에 적용
        String displayWriter = processAnonymityWriterForDisplay(entity.getLetterWriter(), entity.getAnonymityFlag(), anonymousWriterValue);

        return RecipientListResponseDto.builder()
                .letterSeq(entity.getLetterSeq())
                .letterTitle(entity.getLetterTitle())
                .letterWriter(displayWriter)
                .anonymityFlag(entity.getAnonymityFlag())
                .readCount(entity.getReadCount())
                .writeTime(entity.getWriteTime())
                .build();
    }

    // CursorIdentifiable 인터페이스 구현
    @Override
    public Integer getCursorId() {
        return this.letterSeq; // 게시물 커서 ID로 letterSeq 사용
    }

    /**
     * 익명 여부(anonymityFlag)에 따라 작성자 이름(letterWriter)을 표시용으로 처리합니다.
     * 'Y'인 경우 첫 글자만 남기고 나머지는 '*'로 처리하거나, anonymousWriterValue를 반환합니다.
     * 이 메서드는 Entity를 DTO로 변환할 때만 사용되어야 합니다.
     *
     * @param letterWriter 원본 작성자 이름 (DB에 저장된 이름)
     * @param anonymityFlag 익명 여부 ('Y' 또는 'N')
     * @param anonymousWriterValue 익명 처리 시 사용할 값 (e.g., "익명")
     * @return 표시용으로 처리된 작성자 이름
     */
    private static String processAnonymityWriterForDisplay(String letterWriter, String anonymityFlag, String anonymousWriterValue) {
        if ("Y".equalsIgnoreCase(anonymityFlag)) {
            if (StringUtils.hasText(letterWriter) && letterWriter.length() > 1) {
                return letterWriter.charAt(0) + "*".repeat(letterWriter.length() - 1);
            } else {
                return anonymousWriterValue;
            }
        }
        return letterWriter;
    }
}
