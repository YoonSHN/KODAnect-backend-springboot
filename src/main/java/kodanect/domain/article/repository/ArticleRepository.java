package kodanect.domain.article.repository;

import kodanect.domain.article.entity.Article;
import kodanect.domain.article.entity.ArticleId;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, ArticleId>, ArticleRepositoryCustom {

    @Query("SELECT a " +
            "FROM Article a " +
            "LEFT JOIN FETCH a.files " +
            "where a.id.boardCode = :boardCode and a.id.articleSeq = :articleSeq")
    Optional<Article> findWithFilesByBoardCodeAndArticleSeq(@Param("boardCode") String boardCode,
                                                            @Param("articleSeq") int articleSeq);
}
