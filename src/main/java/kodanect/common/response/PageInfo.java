package kodanect.common.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.stream.Collectors;
@Data
@Builder
public class PageInfo {
    private boolean hasPrevious;
    private String sort;
    private boolean isLast;
    private boolean hasNext;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int numberOfElements;
    private int pageSize;
    private boolean isFirst;

    public static <T> PageInfo fromPage(Page<T> page) {
        String sortString = page.getSort().stream()
                .map(order -> order.getProperty() + ": " + order.getDirection().name())
                .collect(Collectors.joining(", "));

        return PageInfo.builder()
                .hasPrevious(page.hasPrevious())
                .sort(sortString.isEmpty() ? null : sortString)
                .isLast(page.isLast())
                .hasNext(page.hasNext())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .numberOfElements(page.getNumberOfElements())
                .pageSize(page.getSize())
                .isFirst(page.isFirst())
                .build();
    }
}