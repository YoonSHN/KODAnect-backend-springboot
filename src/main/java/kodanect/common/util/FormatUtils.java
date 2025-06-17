package kodanect.common.util;

public class FormatUtils {

    private static final int DATE_LENGTH = 8;
    private static final int YEAR_START = 0;
    private static final int YEAR_END = 4;
    private static final int MONTH_END = 6;

    private FormatUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String formatSearchWord(String keyWord) {
        /* 검색 포매팅 */
        if(keyWord == null || keyWord.isEmpty()) {
            keyWord = "";
        }

        return "%"+keyWord.trim()+"%";
    }

    public static String formatDate(String startDate) {
        /* 날짜 포매팅 */
        if(startDate == null || startDate.isEmpty()) {
            return startDate;
        }
        return startDate.replace("-", "");
    }

    public static String formatDonateDate(String donateDate) {
        /* donateDate 포매팅 */
        if (donateDate == null || donateDate.length() != DATE_LENGTH) {
            return donateDate;
        }

        return donateDate.substring(YEAR_START, YEAR_END) + "-"
                + donateDate.substring(YEAR_END, MONTH_END) + "-"
                + donateDate.substring(MONTH_END, DATE_LENGTH);
    }
}