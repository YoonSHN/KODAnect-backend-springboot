package kodanect.common.response;

import lombok.Getter;
import lombok.experimental.SuperBuilder;


/**
 * 댓글 전용 커서 기반 페이지네이션 응답 포맷
 *
 * <p><b>역할:</b><br>
 * 기증자 추모관의 댓글 목록을 커서 기반으로 조회할 때 사용하는 응답 구조.<br>
 * 댓글 리스트 외에도 다음 요청을 위한 커서 값 및 다음 페이지 존재 여부, 총 댓글 수를 포함한다.
 *
 * <p><b>특징:</b>
 * <ul>
 *     <li>댓글 전용 페이징 응답 구조로, 필드명에 <code>comment</code> 접두어 사용</li>
 *     <li>무한 스크롤 등에서 댓글을 일정 단위로 끊어 불러오기 용이</li>
 *     <li>커서 방식으로 정렬 순서를 안정적으로 유지하면서 이어받기 가능</li>
 * </ul>
 *
 * <p><b>사용 예:</b><br>
 * - 댓글 조회 API: <code>/remembrance/{donateSeq}/comment?cursor=xxx&amp;size=10</code><br>
 * - 클라이언트에서 추가 댓글 요청 시 <code>commentNextCursor</code> 기준으로 이어서 조회
 */
@Getter
@SuperBuilder
public class CursorCommentCountPaginationResponse<T, C> extends CursorCommentPaginationResponse<T, C> {

    /** 총 댓글 개수 */
    private Long totalCommentCount;
}
