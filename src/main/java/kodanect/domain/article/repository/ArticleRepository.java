package kodanect.domain.article.repository;

import kodanect.domain.article.entity.Article;
import kodanect.domain.article.entity.ArticleId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 게시글 엔티티 {@link Article}에 대한 JPA 리포지토리입니다.
 *
 * <p>복합 키 {@link ArticleId}를 식별자로 사용하며,
 * 파일 정보(fetch join) 포함 조회 및 이전/다음 게시글 탐색 기능을 제공합니다.</p>
 *
 * @see Article
 * @see ArticleId
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, ArticleId>, ArticleRepositoryCustom {

    /**
     * 게시글과 연관된 파일 목록을 함께 조회합니다.
     *
     * @param boardCode 게시판 코드
     * @param articleSeq 게시글 번호
     * @return 파일 포함 게시글 정보
     */
    @EntityGraph(attributePaths = "files")
    Optional<Article> findByIdBoardCodeAndIdArticleSeq(String boardCode, int articleSeq);

    /**
     * 삭제되지 않은 이전 게시글 중 가장 최신 게시글을 조회합니다.
     *
     * @param boardCode 게시판 코드
     * @param articleSeq 현재 게시글 번호
     * @param delFlag 삭제 여부 (보통 "N")
     * @return 조건에 맞는 이전 게시글
     */
    Optional<Article> findFirstByIdBoardCodeAndIdArticleSeqLessThanAndDelFlagOrderByIdArticleSeqDesc(
            String boardCode, Integer articleSeq, String delFlag);

    /**
     * 삭제되지 않은 다음 게시글 중 가장 오래된 게시글을 조회합니다.
     *
     * @param boardCode 게시판 코드
     * @param articleSeq 현재 게시글 번호
     * @param delFlag 삭제 여부 (보통 "N")
     * @return 조건에 맞는 다음 게시글
     */
    Optional<Article> findFirstByIdBoardCodeAndIdArticleSeqGreaterThanAndDelFlagOrderByIdArticleSeqAsc(
            String boardCode, Integer articleSeq, String delFlag);

}
