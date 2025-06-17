package kodanect.domain.logging.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 프론트엔드에서 발생한 단일 사용자 액션 로그 DTO
 * 유형, 페이지 이동 등 이벤트 정보를 포함합니다.
 */
@Getter
@Builder
public class FrontendLogDto {

    /**
     * 이벤트 유형
     */
    private String eventType;

    /**
     * 이벤트가 발생한 요소의 ID
     */
    private String elementId;

    /**
     * 이벤트가 발생한 페이지의 전체 URL
     */
    private String pageUrl;

    /**
     * 이전 페이지의 URL
     */
    private String referrerUrl;

    /**
     * 이벤트 발생 시각
     */
    private String timestamp;

}
