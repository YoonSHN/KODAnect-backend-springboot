package kodanect.domain.article.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 게시글 첨부파일 정보를 담는 DTO
 */
@Getter
@Builder
public class ArticleFileDto {
    private String fileName;
    private String orgFileName;
}
