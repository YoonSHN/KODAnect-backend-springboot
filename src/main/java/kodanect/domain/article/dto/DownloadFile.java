package kodanect.domain.article.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;


/**
 * 파일 다운로드 응답 객체
 * - 컨트롤러에서 실제 파일 전송을 위한 정보를 담는 DTO
 */
@Getter
@AllArgsConstructor
public class DownloadFile {

    private final Resource resource;
    private final String contentType;
    private final String encodedFileName;

}
