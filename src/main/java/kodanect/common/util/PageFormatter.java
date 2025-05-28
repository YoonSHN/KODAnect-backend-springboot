package kodanect.common.util;

import org.springframework.data.domain.Pageable;

public class PageFormatter {

    private PageFormatter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Pageable createPageable(String page, String size) {
        /* 페이지 포매팅 */
        int pageNum = Integer.parseInt(page);
        int sizeNum = Integer.parseInt(size);

        return Pageable.ofSize(sizeNum).withPage(pageNum - 1);
    }
}
