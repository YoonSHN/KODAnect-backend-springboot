package kodanect.common.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CursorReplyPaginationResponse<T> {

    /* 실제 데이터 */
    private List<T> content;

    /* 다음 요청 시 사용할 커서 값*/
    private Integer replyNextCursor;

    /* 다음 페이지 존재 여부 */
    private boolean replyHasNext;
}
