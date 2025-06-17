package kodanect.common.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * 전역 설정 프로퍼티 클래스
 *
 * application.yml 또는 application.properties 내의
 * globals.* 값을 객체로 바인딩하여 사용하는 용도
 */
@Getter
@ConfigurationProperties(prefix = "globals")
@ConstructorBinding
@RequiredArgsConstructor
public class GlobalsProperties {

    private final String fileStorePath;
    private final Long posblAtchFileSize;
    private final String fileBaseUrl;

}
