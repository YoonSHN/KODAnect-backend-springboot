package kodanect.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class hCaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(hCaptchaService.class);
    private static final String HCAPTCHA_VERIFY_URL = "https://hcaptcha.com/siteverify";

    @Value("${hcaptcha.secret-key}") // application.properties에서 시크릿 키를 주입받습니다.
    private String secretKey;

    private final RestTemplate restTemplate;

    public hCaptchaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean verifyCaptcha(String captchaToken) {

        // !!!!! 개발/테스트용 임시 코드 시작 !!!!!
        // 실제 hCaptcha 인증 없이 항상 true를 반환하도록 합니다.
        logger.warn("===== 경고: hCaptcha 인증이 현재 테스트 목적으로 우회되었습니다! =====");
        logger.warn("===== 이 코드는 프로덕션 환경에 배포되면 안 됩니다! =====");
        return true;
        // !!!!! 개발/테스트용 임시 코드 끝 !!!!!

        /*
        if (captchaToken == null || captchaToken.trim().isEmpty()) {
            logger.warn("hCaptcha 토큰이 비어있거나 null입니다.");
            return false;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(HCAPTCHA_VERIFY_URL)
                .queryParam("secret", secretKey)
                .queryParam("response", captchaToken);

        try {
            // hCaptcha API 호출
            // 응답은 Map 형태로 파싱
            Map<String, Object> response = restTemplate.postForObject(builder.toUriString(), null, Map.class);

            if (response == null) {
                logger.error("hCaptcha 응답이 null입니다.");
                return false;
            }

            Boolean success = (Boolean) response.get("success");
            if (success != null && success) {
                logger.info("hCaptcha 인증 성공.");
                return true;
            } else {
                List<String> errorCodes = (List<String>) response.get("error-codes");
                logger.warn("hCaptcha 인증 실패. 오류 코드: {}", errorCodes);
                return false;
            }
        } catch (Exception e) {
            logger.error("hCaptcha API 호출 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
         */
    }
}
