package kodanect.common.util;

public class SearchFormatter {

    public static String formatSearchWord(String searchWord) {
        /* 검색 포매팅 */
        if(searchWord == null || searchWord.isEmpty()) {
            searchWord = "";
        }

        return "%"+searchWord.trim()+"%";
    }

    public static String formatDate(String startDate) {
        /* 날짜 포매팅 */
        return startDate.replaceAll("-", "");
    }
}