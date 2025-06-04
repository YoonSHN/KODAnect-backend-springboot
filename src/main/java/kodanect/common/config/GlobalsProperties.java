package kodanect.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 전역 설정 프로퍼티 클래스
 *
 * application.yml 또는 application.properties 내의
 * globals.* 값을 객체로 바인딩하여 사용하는 용도
 *
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "globals")
public class GlobalsProperties {

    /** 파일 저장 경로 */
    private String fileStorePath;

    /** 업로드 허용 파일 크기 */
    private Long posblAtchFileSize;

    /** 업로드 파일 접근 URL */
    private String fileBaseUrl;

}

