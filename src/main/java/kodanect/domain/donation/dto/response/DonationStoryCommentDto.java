package kodanect.domain.donation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import kodanect.common.util.CursorIdentifiable;
import kodanect.domain.donation.entity.DonationStoryComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DonationStoryCommentDto implements CursorIdentifiable<Long> {

    private Long commentSeq;

    private String commentWriter; // 추모자

    private String contents;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime writeTime; // yyyy-MM-dd 형식의 문자열

    public DonationStoryCommentDto(Long commentSeq, String commentWriter, String comments, LocalDateTime writeTime) {
        this.commentSeq = commentSeq;
        this.commentWriter = commentWriter;
        this.contents = comments;
        this.writeTime = writeTime;
    }

    @Override
    @JsonIgnore
    public Long getCursorId() {
        return commentSeq;
    }

    /**
     * Entity → DTO 변환용 정적 메서드
     */
    public static DonationStoryCommentDto fromEntity(DonationStoryComment storyComment) {
        return DonationStoryCommentDto.builder()
                .commentSeq(storyComment.getCommentSeq())
                .commentWriter(storyComment.getCommentWriter())
                .contents(storyComment.getContents())
                .writeTime(storyComment.getWriteTime())
                .build();
    }
}