package kodanect.common.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 게시글 조회 수 중복 증가 방지를 위한 제한 도구
 *
 * 동일한 사용자가 일정 시간 내에 같은 게시글을 여러 번 조회해도
 * 조회 수가 중복 증가하지 않도록 제어하는 컴포넌트입니다.
 *
 * IP + 게시글 기준으로 제한하며,
 * 10분(600,000ms) 이내의 중복 조회는 무시합니다.
 * 단일 인스턴스 서버에만 유지가능
 */
@Component
public class RequestBasedHitLimiter {

    private static final long VIEW_LIMIT_DURATION_MILLIS = 10L * 60 * 1000;
    private static final Map<String, Long> hitTimeMap = new ConcurrentHashMap<>();

    public boolean isFirstView(String boardCode, int articleSeq, String ip) {
        String key = boardCode + ":" + articleSeq + ":" + ip;
        long now = System.currentTimeMillis();

        Long lastViewed = hitTimeMap.get(key);
        if (lastViewed == null || now - lastViewed > VIEW_LIMIT_DURATION_MILLIS) {
            hitTimeMap.put(key, now);
            return true;
        }
        return false;
    }
}
