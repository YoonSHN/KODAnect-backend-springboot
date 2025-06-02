package kodanect.domain.article.repository;

import kodanect.domain.article.entity.ArticleFile;
import kodanect.domain.article.entity.ArticleFileId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleFileRepository extends JpaRepository<ArticleFile, ArticleFileId> {

}
