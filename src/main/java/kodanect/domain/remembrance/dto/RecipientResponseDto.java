package kodanect.domain.remembrance.dto;

import kodanect.domain.remembrance.entity.RecipientEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private char anonymityFlag;
    private int readCount;
    private String letterContents;
    private String fileName;
    private String orgFileName;
    private LocalDateTime writeTime;
    private String writerId;
    private LocalDateTime modifyTime;
    private String modifierId;
    private char delFlag; // char 타입으로 유지
    private int commentCount; // 댓글 수는 조회 시 필요한 정보이므로 DTO에 포함

    // Entity -> DTO 변환 메서드 (정적 팩토리 메서드)
    public static RecipientResponseDto fromEntity(RecipientEntity entity) {
        return RecipientResponseDto.builder()
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
                // commentCount 필드는 Entity에 @Transient로 정의되어 있으므로,
                // 기본값으로 0을 설정하거나, 서비스 계층에서 Repository를 통해 별도로 조회하여 주입해야 합니다.
                // 여기서는 초기값 0을 설정하고, 실제 값은 서비스 계층에서 채우도록 합니다.
                .commentCount(0)
                .build();
    }
}
