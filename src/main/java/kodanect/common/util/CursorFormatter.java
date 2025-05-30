package kodanect.common.util;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.domain.remembrance.dto.MemorialResponse;
import kodanect.domain.remembrance.dto.MemorialReplyResponse;

import java.util.List;

public class CursorFormatter {

    private CursorFormatter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static CursorPaginationResponse<MemorialResponse> cursorFormat(List<MemorialResponse> memorialResponses, int size) {
        /* 기본 cursor 포맷 */
        boolean hasNext = memorialResponses.size() > size;

        List<MemorialResponse> content = memorialResponses.stream().limit(size).toList();

        Integer nextCursor = hasNext ? content.get(content.size() - 1).getDonateSeq() : null;

        return CursorPaginationResponse.<MemorialResponse>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    public static CursorReplyPaginationResponse<MemorialReplyResponse> cursorReplyFormat(List<MemorialReplyResponse> memorialReplyResponses, int size) {
        /* 댓글 cursor 포맷 */
        boolean hasNext = memorialReplyResponses.size() > size;

        List<MemorialReplyResponse> content = memorialReplyResponses.stream().limit(size).toList();

        Integer nextCursor = hasNext ? content.get(content.size() - 1).getReplySeq() : null;

        return CursorReplyPaginationResponse.<MemorialReplyResponse>builder()
                .content(content)
                .replyNextCursor(nextCursor)
                .replyHasNext(hasNext)
                .build();
    }

}
