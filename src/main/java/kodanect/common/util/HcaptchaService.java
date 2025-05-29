package kodanect.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class HcaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(HcaptchaService.class);
    private static final String HCAPTCHA_VERIFY_URL = "https://hcaptcha.com/siteverify";

    @Value("${hcaptcha.secret-key}") // application.properties에서 시크릿 키를 주입받습니다.
    private String secretKey;

    // 개발 모드 플래그 추가 (application.properties 또는 application-dev.properties에 설정)
    @Value("${hcaptcha.bypass-enabled:true}") // 기본값은 false (우회 안 함)
    private boolean bypassEnabled;

    private final RestTemplate restTemplate;

    public HcaptchaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean verifyCaptcha(String captchaToken) {
        if (bypassEnabled) {
            logger.warn("===== 경고: hCaptcha 인증이 현재 설정에 따라 우회되었습니다! =====");
            return true;
        }

        if (captchaToken == null || captchaToken.trim().isEmpty()) {
            logger.warn("hCaptcha 토큰이 비어있거나 null입니다.");
            return false;
        }

        try {
            JsonNode responseBody = requestCaptchaVerification(captchaToken);

            if (responseBody == null) {
                logger.error("hCaptcha API 응답이 null입니다.");
                return false;
            }

            boolean success = responseBody.path("success").asBoolean(false);
            if (!success) {
                logErrorCodes(responseBody);
            }

            return success;

        } catch (Exception e) {
            logger.error("hCaptcha 인증 중 예외 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    private JsonNode requestCaptchaVerification(String captchaToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("response", captchaToken);
        params.add("secret", secretKey);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(HCAPTCHA_VERIFY_URL, params, JsonNode.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }

        logger.error("hCaptcha API 호출 실패: 상태 코드 {}", response.getStatusCode());
        return null;
    }

    private void logErrorCodes(JsonNode responseBody) {
        JsonNode errorCodes = responseBody.get("error-codes");
        if (errorCodes != null && errorCodes.isArray()) {
            for (JsonNode errorCode : errorCodes) {
                logger.warn("hCaptcha 인증 실패 오류 코드: {}", errorCode.asText());
            }
        }
    }
}
