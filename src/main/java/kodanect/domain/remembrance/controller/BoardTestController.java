package kodanect.domain.remembrance.controller;

import kodanect.common.response.ApiResponse;
import kodanect.domain.remembrance.entity.BoardTest;
import kodanect.domain.remembrance.service.BoardTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

/**
 * BoardTest 컨트롤러
 *
 * 역할
 * - 게시글 목록 응답 시나리오 제공
 * - 내부 예외 발생 시나리오 제공
 *
 * 특징
 * - 실제 DB 연동 없이 더미 데이터 기반으로 동작
 * - 클라이언트 응답 포맷은 ApiResponse 기준
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardTestController {
    private static final int HTTP_OK = 200;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    private final MessageSource messageSource;
    private final BoardTestService boardTestService;


    /**
     * 게시글 목록 조회 성공 시나리오
     *
     * 더미 게시글 리스트와 함께 성공 메시지 반환
     */
    @GetMapping("/success")
    public ResponseEntity<ApiResponse<List<BoardTest>>> getSuccess() {
        String successMessage = messageSource.getMessage("board.list.get.success", null, Locale.getDefault());
        List<BoardTest> boards = boardTestService.success();
        return ResponseEntity.ok(ApiResponse.success(HTTP_OK, successMessage, boards));
    }

    /**
     * 500 예외 시나리오
     *
     * 내부에서 예외 발생 유도
     */
    @GetMapping("/error/500")
    public ResponseEntity<ApiResponse<Void>> simulateInternalError() {
        boardTestService.simulateInternalError();
        return ResponseEntity.ok(ApiResponse.fail(HTTP_INTERNAL_SERVER_ERROR, "이 코드는 도달하지 않습니다."));
    }

}
