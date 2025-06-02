package kodanect.domain.article.service;

import kodanect.domain.article.dto.ArticleDTO;
import kodanect.domain.article.dto.ArticleDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArticleService {
    Page<? extends ArticleDTO>  getArticles(List<String> boardCodes, String searchField, String keyword, Pageable pageable);

    ArticleDetailDto getArticle(String boardCode, Integer articleSeq);
}
