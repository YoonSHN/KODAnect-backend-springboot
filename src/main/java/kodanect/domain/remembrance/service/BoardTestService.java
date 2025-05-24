package kodanect.domain.remembrance.service;

import kodanect.domain.remembrance.entity.BoardTest;

import java.util.List;

/**
 * BoardTest 서비스 인터페이스
 *
 * 역할
 * - 게시글 목록 반환
 * - 내부 오류 시뮬레이션
 */
public interface BoardTestService {

    /**
     * 게시글 목록 조회
     *
     * 더미 데이터를 리스트 형태로 반환
     */
    List<BoardTest> success();

    /**
     * 내부 오류 시뮬레이션
     *
     * 예외 강제 발생
     */
    void simulateInternalError();
}
