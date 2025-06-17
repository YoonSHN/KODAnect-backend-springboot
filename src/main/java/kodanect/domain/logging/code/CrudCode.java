package kodanect.domain.logging.code;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자 액션 로그 및 서버 요청을 CRUD 유형으로 분류하는 Enum
 * 이벤트명 혹은 HTTP 메서드를 기반으로 어떤 CRUD 동작인지 추론합니다.
 */
public enum CrudCode {

    /**
     * 생성(Create) 작업
     */
    C("createPost", "createComment"),

    /**
     * 조회(Read) 또는 탐색 작업
     */
    R("clickButton", "clickMenu", "clickTab", "clickCard", "clickLink", "executeSearch", "downloadFile"),

    /**
     * 수정(Update) 작업
     */
    U("updatePost", "updateComment", "reactEmoji"),

    /**
     * 삭제(Delete) 작업
     */
    D("deletePost", "deleteComment"),

    /**
     * 기타(분류 불가) 작업
     */
    X();

    private static final Map<String, CrudCode> EVENT_TYPE_MAP = new HashMap<>();

    static {
        for (CrudCode code : values()) {
            for (String type : code.eventTypes) {
                EVENT_TYPE_MAP.put(type.toLowerCase(), code);
            }
        }
    }

    private final List<String> eventTypes;

    CrudCode(String... eventTypes) {
        this.eventTypes = eventTypes == null ? List.of() : List.of(eventTypes);
    }

    /**
     * 이벤트 이름을 기반으로 해당하는 CRUD 유형을 반환합니다.
     *
     * @param eventType 이벤트 이름
     * @return 일치하는 CrudCode, 없으면 X
     */
    public static CrudCode fromEventType(String eventType) {
        if (eventType == null || eventType.isEmpty()) {
            return X;
        }

        return EVENT_TYPE_MAP.getOrDefault(eventType.toLowerCase(), X);
    }

    /**
     * HTTP 메서드를 기반으로 해당하는 CRUD 유형을 반환합니다.
     *
     * @param method HTTP 메서드 (예: GET, POST)
     * @return 일치하는 CrudCode, 없으면 X
     */
    public static CrudCode fromHttpMethod(String method) {
        if (method == null) {
            return CrudCode.X;
        }

        return switch (method.toUpperCase()) {
            case "POST" -> CrudCode.C;
            case "GET" -> CrudCode.R;
            case "PUT", "PATCH" -> CrudCode.U;
            case "DELETE" -> CrudCode.D;
            default -> CrudCode.X;
        };
    }

}
