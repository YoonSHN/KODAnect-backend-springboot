package kodanect.common.validation;

import kodanect.domain.remembrance.dto.common.MemorialNextCursor;
import kodanect.domain.remembrance.exception.InvalidPaginationException;

/** 커서기반 페이지 번호와 한 페이지에 보여질 게시물 수 검증 클래스 */
public class PaginationValidator {

    private static final int DATE_LENGTH = 8;
    private static final int DEFAULT_DATE_LENGTH = 10;

    private PaginationValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     *
     * @param nextCursor 페이지 번호
     * @param size 페이지 사이즈
     * <br>
     * <p>cursor는 null 가능하며 0보다 커야한다.</p>
     *             <br>
     * <p>size는 0보다 커야한다.</p>
     * */
    public static void validatePagination(MemorialNextCursor nextCursor, int size) throws InvalidPaginationException {

        Integer cursor = nextCursor == null ? null : nextCursor.getCursor();
        String  date   = nextCursor == null ? null : nextCursor.getDate();
        if(size <= 0) {
            throw new InvalidPaginationException(cursor, size, date);
        }

        if(cursor == null && date == null) {
            return;
        }

        if(cursor == null ^ date == null) {
            throw new InvalidPaginationException(cursor, size, date);
        }

        if(cursor <= 0 && date.length() != DATE_LENGTH && date.length() != DEFAULT_DATE_LENGTH) {
            /* 페이지 범위가 잘못 됐을 경우 */
            throw new InvalidPaginationException(cursor, size, date);
        }
    }

    public static void validatePagination(Integer cursor, int size) throws InvalidPaginationException {

        if((cursor != null && cursor <= 0 || size <= 0)) {
            /* 페이지 범위가 잘못 됐을 경우 */
            throw new InvalidPaginationException(cursor, size);
        }
    }
}
