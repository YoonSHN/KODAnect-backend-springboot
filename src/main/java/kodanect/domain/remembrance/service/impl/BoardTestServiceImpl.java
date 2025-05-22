package kodanect.domain.remembrance.service.impl;

import kodanect.domain.remembrance.entity.BoardTest;
import kodanect.domain.remembrance.service.BoardTestService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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

    @Override
    public List<BoardTest> success() {
        return boards;
    }

    @Override
    public void simulateNotFound() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.");
    }

    @Override
    public void simulateInternalError() {
        throw new IllegalArgumentException("서버 내부 오류가 발생했습니다.");
    }

}
