package kodanect.domain.donation.dto.response;


import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.domain.donation.entity.DonationStory;
import lombok.Builder;
import lombok.Getter;



@Builder
@Getter
public class DonationStoryDetailDto {

    private Long storySeq;

    private String storyTitle;

    private String storyWriter;
    private String writeTime;

    private AreaCode areaCode;

    private Integer readCount;
    private String storyContents;
    private String fileName;        // 저장된 파일 이름 (서버 파일명)
    private String orgFileName;

    private String imageUrl; //파일 주소

    private CursorCommentPaginationResponse<DonationStoryCommentDto, Long> comments;

    public static DonationStoryDetailDto fromEntity(DonationStory story){
        return DonationStoryDetailDto.builder()
                .storySeq(story.getStorySeq())
                .storyTitle(story.getStoryTitle())
                .storyWriter(story.getStoryWriter())
                .writeTime(story.getWriteTime().toLocalDate().toString())
                .areaCode(story.getAreaCode())
                .readCount(story.getReadCount())
                .storyContents(story.getStoryContents())
                .fileName(story.getFileName())
                .orgFileName(story.getOrgFileName())
                .build();
    }

    public void setComments(CursorCommentPaginationResponse<DonationStoryCommentDto, Long> comments) {
        this.comments = comments;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
