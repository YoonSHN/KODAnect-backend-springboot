package kodanect.domain.donation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class CaptchaService {
    // 캡차 검증 (프론트에서 받은 토큰을 hcaptcha에 전달하여 재검증)
    public boolean verifyCaptcha(String token) {
        String secret = "team-secret-key"; // 실제 시크릿키로 바꿔야 함
        String url = "https://hcaptcha.com/siteverify";

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString("secret=" + secret + "&response=" + token))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(response.body(), Map.class);

            // 성공 여부를 true/false로 반환
            return Boolean.TRUE.equals(result.get("success"));
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}