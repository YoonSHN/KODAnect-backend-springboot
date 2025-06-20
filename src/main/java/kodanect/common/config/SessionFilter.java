package kodanect.common.config;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 세션 쿠키(sessionId)를 자동으로 발급하고 관리하는 서블릿 필터입니다.
 *
 * 주요 기능:
 * - 모든 요청에 대해 sessionId 쿠키가 존재하는지 확인
 * - 존재하지 않으면 새 UUID 기반 세션 ID 생성 후 쿠키로 응답에 추가
 * - sessionId 값을 HttpServletRequest 속성에 저장하여 이후 로직에서 사용 가능하도록 설정
 */
@Component
public class SessionFilter implements Filter {

    /**
     * 세션 ID로 사용될 쿠키의 이름
     */
    private static final String SESSION_ID_COOKIE_NAME = "sessionId";

    /**
     * 세션 쿠키의 유효 시간 (초 단위). 현재 값은 1일 (86400초)
     */
    private static final int ONE_DAY_SECONDS = 60 * 60 * 24;

    /**
     * 필터의 핵심 로직을 수행합니다.
     *
     * 1. 요청 쿠키에서 sessionId 값을 탐색
     * 2. 존재하지 않으면 새로 생성하여 응답에 Set-Cookie로 추가
     * 3. 요청 속성(request attribute)에 sessionId 값을 심어 이후 컨트롤러/서비스에서 사용 가능하도록 함
     *
     * @param req  서블릿 요청 객체
     * @param res  서블릿 응답 객체
     * @param chain 필터 체인
     * @throws IOException 요청/응답 처리 중 발생한 I/O 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String sessionId = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (SESSION_ID_COOKIE_NAME.equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }

        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();

            Cookie newCookie = new Cookie(SESSION_ID_COOKIE_NAME, sessionId);
            newCookie.setHttpOnly(true);
            newCookie.setSecure(true);
            newCookie.setPath("/");
            newCookie.setMaxAge(ONE_DAY_SECONDS);

            response.addCookie(newCookie);
        }

        request.setAttribute(SESSION_ID_COOKIE_NAME, sessionId);
        chain.doFilter(request, response);
    }

}
