package kodanect.domain.article.repository;

import kodanect.domain.article.entity.Article;
import kodanect.domain.article.entity.ArticleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, ArticleId>, ArticleRepositoryCustom {

    Optional<Article> findByIdBoardCodeAndIdArticleSeq(String boardCode, int articleSeq);
}
