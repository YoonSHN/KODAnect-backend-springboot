package kodanect.domain.remembrance.controller;

import kodanect.common.response.ApiResponse;
import kodanect.domain.remembrance.entity.BoardTest;
import kodanect.domain.remembrance.service.BoardTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 게시글 관련 테스트 API를 제공하는 컨트롤러 클래스입니다.
 *
 * 실제 DB와 연동되지 않고, 내부에 정의된 더미 데이터를 기반으로
 * 성공/실패 응답 시나리오를 테스트할 수 있도록 구성되어 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardTestController {

    private final MessageSource messageSource;
    private final BoardTestService boardTestService;

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

    List<BoardTest> boards = Arrays.asList(dummy1, dummy2, dummy3);

    @GetMapping("/success")
    public ResponseEntity<ApiResponse<List<BoardTest>>> getSuccess() {
        String successMessage = messageSource.getMessage("board.list.get.success", null, Locale.getDefault());
        return ResponseEntity.ok(ApiResponse.success(200, successMessage, boards));
    }

    @GetMapping("/error/404")
    public ResponseEntity<ApiResponse<Void>> simulateNotFound() {
        boardTestService.simulateNotFound();
        return ResponseEntity.ok(ApiResponse.fail(404, "이 코드는 도달하지 않습니다."));
    }

    //test
    @GetMapping("/error/500")
    public ResponseEntity<ApiResponse<Void>> simulateInternalError() {
        boardTestService.simulateInternalError();
        return ResponseEntity.ok(ApiResponse.fail(500, "이 코드는 도달하지 않습니다."));
    }

}