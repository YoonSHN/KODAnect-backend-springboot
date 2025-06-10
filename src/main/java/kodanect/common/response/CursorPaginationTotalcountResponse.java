package kodanect.common.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 커서 기반 페이지네이션 응답 포맷
 * <p>
 * <b>역할:</b> 커서 기반 페이징 처리 결과를 담는 응답 구조로,
 * 클라이언트가 다음 페이지 요청을 할 수 있도록 커서 정보를 제공함.
 * <p>
 * <b>특징:</b>
 * <ul>
 *     <li>정적 페이지 번호 방식이 아닌 유동적인 커서(cursor) 방식 지원</li>
 *     <li>데이터 정합성과 무결성 유지에 유리 (삽입/삭제 빈번한 경우)</li>
 *     <li>다음 요청 가능 여부(hasNext) 및 커서 값(nextCursor) 포함</li>
 * </ul>
 * <p>
 * <b>사용 예:</b><br>
 * - 무한 스크롤(스크롤 기반 더보기)<br>
 * - 정렬 순서를 유지하며 특정 지점 이후 데이터를 이어서 불러올 때
 */
@Getter
@Builder
@AllArgsConstructor
public class CursorPaginationTotalcountResponse<T, C> {

    /** 실제 데이터 응답 리스트 */
    private List<T> content;

    /** 다음 요청 시 사용할 커서 값*/
    private C nextCursor;

    /** 다음 페이지가 존재하는지 여부 (true면 다음 요청 가능) */
    private boolean hasNext;

    /** 총 게시물(레코드) 개수 */
    private Integer totalCount;
}
