package kodanect.common.util;

/**
 *
 * 커서 기반 페이징을 위한 공용 식별자 인터페이스.
 * <br>
 * 페이징 기준이 되는 커서 ID를 반환해야 한다.
 *
 */
public interface CursorIdentifiable<T> {
    /**
     *
     * 커서 ID 반환 메서드
     *
     * @return 커서 ID 반환
     * */
    T getCursorId();
}
