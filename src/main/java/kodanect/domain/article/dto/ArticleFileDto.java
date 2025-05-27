package kodanect.domain.article.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleFileDto {
    private String fileName;
    private String orgFileName;
}
