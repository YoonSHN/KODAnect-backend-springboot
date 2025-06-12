package kodanect.domain.article.repository;

import kodanect.domain.article.entity.ArticleFile;
import kodanect.domain.article.entity.ArticleFileId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 게시글 첨부파일 엔티티 {@link ArticleFile}에 대한 JPA 리포지토리입니다.
 *
 * <p>복합 키 {@link ArticleFileId}를 식별자로 사용하며,
 * 기본적인 CRUD 기능을 제공합니다.</p>
 *
 * @see ArticleFile
 * @see ArticleFileId
 */
@Repository
public interface ArticleFileRepository extends JpaRepository<ArticleFile, ArticleFileId> {

}
