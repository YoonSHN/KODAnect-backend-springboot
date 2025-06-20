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

    /* 댓글 리스트 */
    private CursorCommentPaginationResponse<HeavenCommentResponse, Integer> cursorCommentPaginationResponse;

    public static HeavenDetailResponse of(
            HeavenDto heavenDto,
            CursorCommentCountPaginationResponse<HeavenCommentResponse, Integer> cursorCommentPaginationResponse
    ) {

        return HeavenDetailResponse.builder()
                .heavenDto(heavenDto)
                .cursorCommentPaginationResponse(cursorCommentPaginationResponse)
                .build();
    }
}
