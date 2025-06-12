package kodanect.domain.article.repository;

import kodanect.domain.article.entity.BoardCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * 게시판 카테고리 엔티티 {@link BoardCategory}에 대한 JPA 리포지토리입니다.
 *
 * @see BoardCategory
 */
@Repository
public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {
}
