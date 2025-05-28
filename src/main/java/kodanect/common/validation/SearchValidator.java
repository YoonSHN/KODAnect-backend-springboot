package kodanect.common.validation;

import kodanect.domain.remembrance.exception.InvalidSearchDateFormatException;
import kodanect.domain.remembrance.exception.InvalidSearchDateRangeException;
import kodanect.domain.remembrance.exception.MissingSearchDateParameterException;

public class SearchValidator {

    private static final int DATE_LENGTH = 10;

    public static void validateSearchDates(String startDate, String endDate) throws Exception{
        /* 날짜 검증 */
        if(startDate == null || endDate == null) {
            throw new MissingSearchDateParameterException();
        }

        if(startDate.isEmpty() || endDate.isEmpty()) {
            throw new MissingSearchDateParameterException();
        }

        if(startDate.length() != DATE_LENGTH || endDate.length() != DATE_LENGTH) {
            throw new InvalidSearchDateFormatException();
        }

        if(startDate.compareTo(endDate) > 0) {
            throw new InvalidSearchDateRangeException();
        }
    }
}
