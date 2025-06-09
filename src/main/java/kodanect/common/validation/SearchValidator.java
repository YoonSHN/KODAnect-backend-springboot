package kodanect.common.validation;

import kodanect.domain.remembrance.exception.InvalidSearchDateFormatException;
import kodanect.domain.remembrance.exception.InvalidSearchDateRangeException;
import kodanect.domain.remembrance.exception.MissingSearchDateParameterException;

public class SearchValidator {

    private static final int DATE_LENGTH = 10;

    private SearchValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void validateSearchDates(String startDate, String endDate)
            throws  MissingSearchDateParameterException,
                    InvalidSearchDateFormatException,
                    InvalidSearchDateRangeException
    {
        /* 날짜 검증 */
        if(startDate.isBlank() || endDate.isBlank()) {
            throw new MissingSearchDateParameterException(startDate, endDate);
        }

        if(startDate.length() != DATE_LENGTH || endDate.length() != DATE_LENGTH) {
            throw new InvalidSearchDateFormatException(startDate, endDate);
        }

        if(startDate.compareTo(endDate) > 0) {
            throw new InvalidSearchDateRangeException(startDate, endDate);
        }
    }
}
