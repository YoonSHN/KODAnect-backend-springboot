package kodanect.domain.remembrance.service.impl;

import kodanect.domain.remembrance.entity.BoardTest;
import kodanect.domain.remembrance.service.BoardTestService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * BoardTest 서비스 구현체
 *
 * 역할
 * - 더미 게시글 데이터 반환
 * - 서버 내부 예외 시뮬레이션
 */
@Service
public class BoardTestServiceImpl implements BoardTestService {

    BoardTest dummy1 = BoardTest.builder()
            .title("테스트 제목 1")
            .content("내용입니다 1")
            .writer("작성자 1")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    BoardTest dummy2 = BoardTest.builder()
            .title("테스트 제목 2")
            .content("내용입니다 2")
            .writer("작성자 2")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    BoardTest dummy3 = BoardTest.builder()
            .title("테스트 제목 3")
            .content("내용입니다 3")
            .writer("작성자 3")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    private final List<BoardTest> boards = Arrays.asList(dummy1, dummy2, dummy3);

    /**
     * 게시글 목록 반환
     *
     * 더미 게시글 리스트 반환
     */
    @Override
    public List<BoardTest> success() {
        return boards;
    }

    /**
     * 내부 오류 시뮬레이션
     *
     * IllegalArgumentException 강제 발생
     */
    @Override
    public void simulateInternalError() {
        throw new IllegalArgumentException("서버 내부 오류가 발생했습니다.");
    }

}
