package kodanect.common.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageApiResponse<T> {
    private final boolean success;
    private final int code;
    private final String message;
    private final List<T> data; // 페이징된 목록 데이터를 담을 필드 (List<T>)
    private final PageInfo pageInfo; // 페이징 정보를 담을 필드

    // 모든 필드를 받는 private 생성자
    private PageApiResponse(boolean success, int code, String message, List<T> data, PageInfo pageInfo) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.pageInfo = pageInfo;
    }

    /**
     * 성공적인 페이징 응답 생성 (데이터 포함)
     * Spring Page 객체와 메시지를 받아 PageApiResponse 인스턴스를 생성합니다.
     */
    public static <T> PageApiResponse<T> success(Page<T> page, String message) {
        return new PageApiResponse<>(
                true,
                200, // 성공 코드
                message,
                page.getContent(), // Page 객체에서 실제 컨텐츠 리스트 추출
                PageInfo.fromPage(page) // PageInfo 객체 생성
        );
    }

    /**
     * 실패 페이징 응답 생성
     * 실패 시에는 데이터와 pageInfo를 null로 설정합니다.
     */
    public static <T> PageApiResponse<T> fail(int code, String message) {
        return new PageApiResponse<>(false, code, message, null, null);
    }
}