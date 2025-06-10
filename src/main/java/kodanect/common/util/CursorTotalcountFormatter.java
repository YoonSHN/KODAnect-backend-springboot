package kodanect.common.util;

import kodanect.common.response.CursorPaginationTotalcountResponse;

import java.util.List;

/**
 * 커서 기반 페이지네이션 응답 생성을 위한 유틸리티 클래스
 *
 * <p><b>역할:</b><br>
 * 서비스 계층에서 커서 기반 데이터 목록 처리 후, 클라이언트에 전달할 페이지네이션 응답 객체를 생성한다.
 *
 * <p><b>특징:</b>
 * <ul>
 *     <li>일반 목록과 댓글 목록에 대해 각기 다른 응답 포맷(CursorPaginationResponse, CursorReplyPaginationResponse) 지원</li>
 *     <li>지정된 size만큼 데이터를 제한하여 클라이언트에 제공</li>
 *     <li>다음 커서가 존재하는지 여부(hasNext, replyHasNext)를 함께 포함</li>
 * </ul>
 *
 * <p><b>사용 예:</b><br>
 * - 추모관 게시글 목록: cursorFormat()<br>
 * - 추모관 댓글 목록: cursorReplyFormat()
 */

public class CursorTotalcountFormatter {

    private CursorTotalcountFormatter() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 일반 게시글 목록 응답 포맷 생성
     *
     * @param responses 전체 응답 대상 목록
     * @param size 클라이언트 요청 size
     * @param totalCount 검색 조건에 맞는 전체 게시물(레코드) 개수
     * @return 다음 커서 정보를 포함한 CursorPaginationTotalcountResponse
     */

    public static <T extends CursorIdentifiable<C>, C> CursorPaginationTotalcountResponse<T, C> cursorFormat(List<T> responses, int size, int totalCount) {
        /* 기본 cursor 포맷 */
        boolean hasNext = responses.size() > size;

        List<T> content = responses.stream().limit(size).toList();

        C nextCursor = hasNext ? content.get(content.size() - 1).getCursorId() : null;

        return CursorPaginationTotalcountResponse.<T, C>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .totalCount(totalCount)
                .build();
    }

}
