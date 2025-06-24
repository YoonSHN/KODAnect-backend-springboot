package kodanect.common.constant;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * {@link CrudCode} enum의 분류 로직을 테스트하는 단위 테스트 클래스입니다.
 * - 이벤트 타입 기반 분류 {@link CrudCode#fromEventType(String)}
 * - HTTP 메서드 기반 분류 {@link CrudCode#fromHttpMethod(String)}
 */
public class CrudCodeTest {

    /**
     * 유효한 이벤트 타입 문자열이 올바르게 CRUD 코드에 매핑되는지 테스트합니다.
     */
    @Test
    public void testFromEventType_validValues() {
        assertEquals(CrudCode.C, CrudCode.fromEventType("createPost"));
        assertEquals(CrudCode.R, CrudCode.fromEventType("clickMenu"));
        assertEquals(CrudCode.U, CrudCode.fromEventType("reactEmoji"));
        assertEquals(CrudCode.D, CrudCode.fromEventType("deleteComment"));
    }

    /**
     * 대소문자를 구분하지 않고 이벤트 타입을 매핑하는지 테스트합니다.
     */
    @Test
    public void testFromEventType_caseInsensitive() {
        assertEquals(CrudCode.R, CrudCode.fromEventType("CLICKLINK"));
        assertEquals(CrudCode.U, CrudCode.fromEventType("UpdateComment"));
    }

    /**
     * 미등록된(정의되지 않은) 이벤트 타입이 들어왔을 때 기본값 X로 분류되는지 테스트합니다.
     */
    @Test
    public void testFromEventType_unknownValue() {
        assertEquals(CrudCode.X, CrudCode.fromEventType("somethingRandom"));
    }

    /**
     * null 또는 빈 문자열이 들어왔을 때 기본값 X로 분류되는지 테스트합니다.
     */
    @Test
    public void testFromEventType_nullOrEmpty() {
        assertEquals(CrudCode.X, CrudCode.fromEventType(null));
        assertEquals(CrudCode.X, CrudCode.fromEventType(""));
    }

    /**
     * 유효한 HTTP 메서드 문자열이 올바르게 CRUD 코드에 매핑되는지 테스트합니다.
     */
    @Test
    public void testFromHttpMethod_validMethods() {
        assertEquals(CrudCode.C, CrudCode.fromHttpMethod("POST"));
        assertEquals(CrudCode.R, CrudCode.fromHttpMethod("GET"));
        assertEquals(CrudCode.U, CrudCode.fromHttpMethod("PUT"));
        assertEquals(CrudCode.U, CrudCode.fromHttpMethod("patch"));
        assertEquals(CrudCode.D, CrudCode.fromHttpMethod("delete"));
    }

    /**
     * 등록되지 않은 HTTP 메서드 또는 null이 들어왔을 때 기본값 X로 분류되는지 테스트합니다.
     */
    @Test
    public void testFromHttpMethod_unknownMethod() {
        assertEquals(CrudCode.X, CrudCode.fromHttpMethod("OPTIONS"));
        assertEquals(CrudCode.X, CrudCode.fromHttpMethod(null));
    }

}
