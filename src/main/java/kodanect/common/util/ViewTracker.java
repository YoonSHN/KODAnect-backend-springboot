package kodanect.common.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ViewTracker {

    private static final long EXPIRATION_TIME = 1000 * 60 * 30L; // 30분
    private final ConcurrentHashMap<String, Long> viewMap = new ConcurrentHashMap<>();

    /* IP를 통한 조회 수 증가 여부 확인 */
    public boolean shouldIncreaseView(Integer postId, String clientIp) {
        String key = postId + ":" + clientIp;
        long now = System.currentTimeMillis();

        /* 해당 IP가 해당 게시물 접근 여부 확인 */
        if (!viewMap.containsKey(key)) {
            viewMap.put(key, now);
            return true;
        }

        long lastViewTime = viewMap.get(key);

        /* 조회 수 증가 가능 여부 확인 */
        if (now - lastViewTime > EXPIRATION_TIME) {
            viewMap.put(key, now);
            return true;
        }

        return false;
    }
}
