package kodanect.domain.heaven.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import kodanect.common.response.CursorCommentCountPaginationResponse;
import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.domain.heaven.dto.HeavenDto;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@Builder
public class HeavenDetailResponse {

    @JsonUnwrapped
    HeavenDto heavenDto;

    /* 파일 URL */
    private String imageUrl;

    /* 댓글 리스트 */
    private CursorCommentPaginationResponse<HeavenCommentResponse, Integer> cursorCommentPaginationResponse;

    /* 생성 일시 형식화 */
    public String getWriteTime() {
        return heavenDto.getWriteTime().toLocalDate().toString();
    }

    public static HeavenDetailResponse of(
            HeavenDto heavenDto,
            CursorCommentCountPaginationResponse<HeavenCommentResponse, Integer> cursorCommentPaginationResponse,
            String imageUrl
    ) {

        return HeavenDetailResponse.builder()
                .heavenDto(heavenDto)
                .imageUrl(imageUrl)
                .cursorCommentPaginationResponse(cursorCommentPaginationResponse)
                .build();
    }
}
