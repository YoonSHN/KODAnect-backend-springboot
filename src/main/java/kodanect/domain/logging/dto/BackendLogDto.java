package kodanect.domain.logging.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 백엔드 서버 내에서 발생한 요청 및 처리 결과를 기록하는 로그 DTO
 */
@Getter
@Builder
public class BackendLogDto {

    /**
     * HTTP 메서드
     */
    private String httpMethod;

    /**
     * 요청한 API 엔드포인트
     */
    private String endpoint;


    /**
     * 요청을 처리한 컨트롤러 클래스 이름
     */
    private String controller;


    /**
     * 요청을 처리한 메서드 이름
     */
    private String method;


    /**
     * 요청 시 전달된 파라미터
     */
    private String parameters;

    /**
     * HTTP 응답 상태 코드
     */
    private int httpStatus;

    /**
     * 로그 기록 시각
     */
    private String timestamp;

}
