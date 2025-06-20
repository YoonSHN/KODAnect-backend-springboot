package kodanect.domain.logging.controller;

import kodanect.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FaviconController
 *
 * 역할:
 * - 브라우저가 자동으로 요청하는 "/favicon.ico" 경로를 무시 처리합니다.
 * - 불필요한 서버 로그 발생 및 에러 추적 혼선을 방지하기 위한 컨트롤러입니다.
 *
 * 설명:
 * - HTML 문서에 명시적으로 favicon 링크가 없으면 브라우저는 기본적으로 "/favicon.ico"를 요청합니다.
 * - 해당 요청을 별도의 리소스 없이 수신하고, 204 No Content 상태로 응답합니다.
 * - 응답은 공통 응답 포맷인 {@link ApiResponse} 형식을 따릅니다.
 */
@RestController
@RequiredArgsConstructor
public class FaviconController {

    private final MessageSourceAccessor messageSource;

    /**
     * "/favicon.ico" 요청을 무시하고 204 응답을 반환하는 엔드포인트입니다.
     *
     * @return 공통 응답 포맷 {@link ApiResponse} - 상태 코드 204(No Content), 본문 데이터 없음
     */
    @GetMapping("/favicon.ico")
    public ResponseEntity<ApiResponse<Void>> ignoreFavicon() {
        String message = messageSource.getMessage("log.favicon.ignored", new Object[]{});
        ApiResponse<Void> response = ApiResponse.success(HttpStatus.NO_CONTENT, message);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

}
