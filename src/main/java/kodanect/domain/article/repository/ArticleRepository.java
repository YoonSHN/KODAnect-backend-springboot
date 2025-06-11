package kodanect.domain.article.repository;

import kodanect.domain.article.entity.Article;
import kodanect.domain.article.entity.ArticleId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, ArticleId>, ArticleRepositoryCustom {

    @EntityGraph(attributePaths = "files")
    Optional<Article> findByIdBoardCodeAndIdArticleSeq(String boardCode, int articleSeq);

    Optional<Article> findFirstByIdBoardCodeAndIdArticleSeqLessThanAndDelFlagOrderByIdArticleSeqDesc(
            String boardCode, Integer articleSeq, String delFlag);

    Optional<Article> findFirstByIdBoardCodeAndIdArticleSeqGreaterThanAndDelFlagOrderByIdArticleSeqAsc(
            String boardCode, Integer articleSeq, String delFlag);

}
