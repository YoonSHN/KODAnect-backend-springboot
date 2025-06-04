package kodanect.domain.donation.dto;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.Objects;

/**
 * 커스텀 Pageable 구현체로, offset/limit 기반 페이지 요청을 지원합니다.
 * Spring의 PageRequest는 page/size 기반이기 때문에, offset 기반 API에 적합하지 않습니다.
 */
public class OffsetBasedPageRequest implements Pageable, Serializable {

    private static final long serialVersionUID = 1L;

    private final int offset;
    private final int limit;
    private final Sort sort;

    /**
     * 생성자 - offset, limit, sort 지정
     *
     * @param offset 시작 위치 (0 이상)
     * @param limit  가져올 데이터 수 (1 이상)
     * @param sort   정렬 정보
     */
    public OffsetBasedPageRequest(int offset, int limit, Sort sort) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be negative.");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than 0.");
        }
        this.offset = offset;
        this.limit = limit;
        this.sort = sort != null ? sort : Sort.unsorted();
    }

    /**
     * 생성자 - offset, limit만 지정 (정렬 없음)
     */
    public OffsetBasedPageRequest(int offset, int limit) {
        this(offset, limit, Sort.unsorted());
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetBasedPageRequest(offset + limit, limit, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        int newOffset = offset - limit;
        return newOffset < 0 ? first() : new OffsetBasedPageRequest(newOffset, limit, sort);
    }

    @Override
    public Pageable first() {
        return new OffsetBasedPageRequest(0, limit, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetBasedPageRequest(pageNumber * limit, limit, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }

    @Override
    public boolean isPaged() {
        return true;
    }

    @Override
    public boolean isUnpaged() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OffsetBasedPageRequest)) {
            return false;
        }
        OffsetBasedPageRequest that = (OffsetBasedPageRequest) o;
        return offset == that.offset &&
                limit == that.limit &&
                Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit, sort);
    }

    @Override
    public String toString() {
        return "OffsetBasedPageRequest{" +
                "offset=" + offset +
                ", limit=" + limit +
                ", sort=" + sort +
                '}';
    }
}