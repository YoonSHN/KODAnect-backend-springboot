package kodanect.domain.donation.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import kodanect.common.util.CursorIdentifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DonationStoryListDto implements CursorIdentifiable<Long> {

    private Long storySeq;

    private String storyTitle;
    private String storyWriter;

    private Integer readCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate writeTime;

    public DonationStoryListDto(Long storySeq, String storyTitle, String storyWriter,
                                Integer readCount, LocalDateTime writeTime) {
        this.storySeq = storySeq;
        this.storyTitle = storyTitle;
        this.storyWriter = storyWriter;
        this.readCount = readCount;
        this.writeTime = writeTime != null ? writeTime.toLocalDate() : null;
    }


    @Override
    @JsonIgnore
    public Long getCursorId(){
        return storySeq;
    }
}
