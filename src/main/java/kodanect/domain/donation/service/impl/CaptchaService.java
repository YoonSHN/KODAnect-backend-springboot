package kodanect.domain.donation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
@Slf4j
public class CaptchaService {
    // 캡차 검증 (프론트에서 받은 토큰을 hcaptcha에 전달하여 재검증)
    public boolean verifyCaptcha(String token) {
        String secret = "team-secret-key";
        String url = "https://hcaptcha.com/siteverify";

        try {
            HttpClient client = HttpClient.newHttpClient(); // 그냥 선언만 하면 됨

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString("secret=" + secret + "&response=" + token))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(response.body(), Map.class);

            return Boolean.TRUE.equals(result.get("success"));
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("인터럽트 발생", e);
            return false;
        }
        catch (Exception e) {
            log.error("캡차 검증 중 예외 발생", e);
            return false;
        }
    }
}