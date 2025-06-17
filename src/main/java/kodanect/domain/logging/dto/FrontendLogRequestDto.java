package kodanect.domain.logging.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 프론트엔드에서 수집된 사용자 액션 로그 요청 DTO
 * 여러 개의 프론트엔드 로그를 한 번에 서버로 전송할 때 사용됩니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FrontendLogRequestDto {

    /**
     * 프론트엔드에서 발생한 사용자 액션 로그 리스트
     */
    private List<FrontendLogDto> frontendLogs;

}
