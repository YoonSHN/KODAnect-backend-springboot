package kodanect.common.util;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.domain.remembrance.dto.MemorialListResponse;
import kodanect.domain.remembrance.dto.MemorialReplyResponse;

import java.util.List;

public class CursorFormatter {

    private CursorFormatter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static CursorPaginationResponse<MemorialListResponse> cursorFormat(List<MemorialListResponse> memorial, int size) {
        /* 기본 cursor 포맷 */
        boolean hasNext = memorial.size() > size;

        List<MemorialListResponse> content = memorial.stream().limit(size).toList();

        Integer nextCursor = hasNext ? content.get(content.size() - 1).getDonateSeq() : null;

        return CursorPaginationResponse.<MemorialListResponse>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();

    }

    public static CursorReplyPaginationResponse<MemorialReplyResponse> cursorReplyFormat(List<MemorialReplyResponse> memorial, int size) {
        /* 댓글 cursor 포맷 */
        boolean hasNext = memorial.size() > size;

        List<MemorialReplyResponse> content = memorial.stream().limit(size).toList();

        Integer nextCursor = hasNext ? content.get(content.size() - 1).getReplySeq() : null;

        return CursorReplyPaginationResponse.<MemorialReplyResponse>builder()
                .content(content)
                .replyNextCursor(nextCursor)
                .replyHasNext(hasNext)
                .build();
    }
}
