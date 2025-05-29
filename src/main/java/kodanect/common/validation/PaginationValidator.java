package kodanect.common.validation;

import kodanect.domain.remembrance.exception.InvalidPaginationRangeException;

public class PaginationValidator {

    private PaginationValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void validatePagination(Integer cursor, int size) throws InvalidPaginationRangeException {
        /* 페이지 번호와 한 페이지에 보여질 게시물 수 검증 */

        if(cursor != null && cursor <= 0 || size <= 0) {
            /* 페이지 범위가 잘못 됐을 경우 */
            throw new InvalidPaginationRangeException();
        }
    }
}
