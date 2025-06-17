package kodanect.domain.logging.key;

import kodanect.domain.logging.code.CrudCode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 액션을 식별하기 위한 키 클래스입니다.
 *
 * 세션 ID와 CRUD 코드의 조합을 기준으로 해시맵이나 버퍼링에서 키로 사용됩니다.
 *
 * 동등성 비교와 해시 연산을 위해 {@code equals}, {@code hashCode}가 자동 생성됩니다.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class UserActionKey {

    /**
     * 사용자 세션 ID
     */
    private final String sessionId;

    /**
     * 수행된 CRUD 코드
     */
    private final CrudCode crudCode;

}
