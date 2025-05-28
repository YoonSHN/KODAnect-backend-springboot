package kodanect.common.validation;

import kodanect.domain.remembrance.exception.InvalidPaginationFormatException;
import kodanect.domain.remembrance.exception.InvalidPaginationRangeException;
import kodanect.domain.remembrance.exception.MissingPaginationParameterException;

public class PaginationValidator {

    public static void validatePagination(String page, String size) throws Exception {
        /* 페이지 번호와 한 페이지에 보여질 게시물 수 검증 */

        if(page == null || size == null || page.trim().isEmpty() || size.trim().isEmpty()) {
            /* 입력 값이 누락 됐을 경우 */
            throw new MissingPaginationParameterException();
        }

        try{
            int pageNum = Integer.parseInt(page);
            int sizeNum = Integer.parseInt(size);

            if(pageNum <= 0 || sizeNum <= 0) {
                /* 페이지 범위가 잘못 됐을 경우 */
                throw new InvalidPaginationRangeException();
            }
        }
        catch(NumberFormatException e) {
            /* 숫자가 아닌 값이 들어왔을 경우 */
            throw new InvalidPaginationFormatException();
        }
    }
}
