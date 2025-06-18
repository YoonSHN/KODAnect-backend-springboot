package kodanect.domain.recipient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kodanect.common.response.CursorCommentPaginationResponse;
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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime writeTime;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime modifyTime;
    private String delFlag;
    private int commentCount;        // 댓글 수는 조회 시 필요한 정보이므로 DTO에 포함
    private String imageUrl;         // 게시물에 등록된 이미지의 URL
    // 게시물 조회 시 초기 댓글 데이터를 CursorReplyPaginationResponse 형태로 포함
    private CursorCommentPaginationResponse<RecipientCommentResponseDto, Integer> initialCommentData;

    // Entity -> DTO 변환 메서드 (정적 팩토리 메서드)
    public static RecipientDetailResponseDto fromEntity(RecipientEntity entity, String anonymousWriterValue) {
        // 익명 처리 로직을 DTO 변환 시점에 적용
        String displayWriter = processAnonymityWriterForDisplay(entity.getLetterWriter(), entity.getAnonymityFlag(), anonymousWriterValue);

        return RecipientDetailResponseDto.builder() // 빌더로 객체를 생성한 결과를 바로 반환
                .letterSeq(entity.getLetterSeq())
                .organCode(entity.getOrganCode())
                .organEtc(entity.getOrganEtc())
                .letterTitle(entity.getLetterTitle())
                .recipientYear(entity.getRecipientYear())
                .letterWriter(displayWriter)
                .anonymityFlag(entity.getAnonymityFlag())
                .readCount(entity.getReadCount())
                .letterContents(entity.getLetterContents())
                .fileName(entity.getFileName())
                .orgFileName(entity.getOrgFileName())
                .writeTime(entity.getWriteTime())
                .modifyTime(entity.getModifyTime())
                .delFlag(entity.getDelFlag())
                .commentCount(0)
                .imageUrl(entity.getImageUrl())
                // 초기에는 댓글 데이터를 비워두고, 서비스 계층에서 설정
                .initialCommentData(null) // 초기화 시 null 또는 기본 빈 객체
                .build();
    }

    // 서비스 계층에서 초기 댓글 데이터를 설정하기 위한 setter
    public void setInitialCommentData(CursorCommentPaginationResponse<RecipientCommentResponseDto, Integer> initialCommentData) {
        this.initialCommentData = initialCommentData;
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
            // 원본 작성자 이름이 유효하고 1자보다 길면 첫 글자만 표시하고 나머지는 * 처리
            if (StringUtils.hasText(letterWriter) && letterWriter.length() > 1) {
                return letterWriter.charAt(0) + "*".repeat(letterWriter.length() - 1);
            } else {
                // 원본 작성자 이름이 없거나 1자 이하면 anonymousWriterValue 사용
                return anonymousWriterValue;
            }
        }
        return letterWriter; // 익명이 아니면 원본 그대로 반환
    }
}
