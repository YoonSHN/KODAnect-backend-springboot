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
    private String organCode;
    private String organEtc;
    private String letterTitle;
    private String recipientYear;
    private String letterWriter;
    private String anonymityFlag;
    private int readCount;
    private String fileName;
    private String orgFileName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime writeTime;
    private String writerId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime modifyTime;
    private String modifierId;
    private String delFlag; // char 타입으로 유지
    private int commentCount; // 댓글 수는 조회 시 필요한 정보이므로 DTO에 포함

    // Entity -> DTO 변환 메서드 (정적 팩토리 메서드)
    public static RecipientListResponseDto fromEntity(RecipientEntity entity) {
        return RecipientListResponseDto.builder()
                .letterSeq(entity.getLetterSeq())
                .organCode(entity.getOrganCode())
                .organEtc(entity.getOrganEtc())
                .letterTitle(entity.getLetterTitle())
                .recipientYear(entity.getRecipientYear())
                .letterWriter(entity.getLetterWriter())
                .anonymityFlag(entity.getAnonymityFlag())
                .readCount(entity.getReadCount())
                .fileName(entity.getFileName())
                .orgFileName(entity.getOrgFileName())
                .writeTime(entity.getWriteTime())
                .writerId(entity.getWriterId())
                .modifyTime(entity.getModifyTime())
                .modifierId(entity.getModifierId())
                .delFlag(entity.getDelFlag())
                .commentCount(0)
                .build();
    }

    // CursorIdentifiable 인터페이스 구현
    @Override
    public Integer getCursorId() {
        return this.letterSeq; // 게시물 커서 ID로 letterSeq 사용
    }
}
