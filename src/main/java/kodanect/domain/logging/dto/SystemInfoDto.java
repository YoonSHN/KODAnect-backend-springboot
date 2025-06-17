package kodanect.domain.logging.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 환경 정보를 담는 DTO
 * 브라우저, 운영체제, 디바이스, 로케일 등의 정보를 포함합니다.
 */
@Getter
@Builder
public class SystemInfoDto {

    /**
     * 브라우저 이름
     */
    private String browserName;

    /**
     * 브라우저 버전
     */
    private String browserVersion;

    /**
     * 운영 체제
     */
    private String operatingSystem;

    /**
     * 디바이스 종류
     */
    private String device;

    /**
     * 사용자의 로케일
     */
    private String locale;

}
