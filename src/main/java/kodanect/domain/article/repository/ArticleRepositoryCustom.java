package kodanect.domain.article.repository;

import kodanect.domain.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArticleRepositoryCustom {

    Page<Article> searchArticles(List<String> boardCodes, String searchField, String keyword, Pageable pageable);

    void increaseHitCount(String boardCode, int articleSeq);
}
