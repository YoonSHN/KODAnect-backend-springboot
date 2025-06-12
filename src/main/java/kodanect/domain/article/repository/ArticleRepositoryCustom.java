package kodanect.domain.article.repository;

import kodanect.domain.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * {@link Article} 엔티티에 대한 사용자 정의 쿼리 리포지토리 인터페이스입니다.
 *
 * <p>동적 검색 및 조회수 증가 등 기본 JPA 기능으로 처리할 수 없는 커스텀 로직을 정의합니다.</p>
 *
 * @see Article
 */
public interface ArticleRepositoryCustom {

    /**
     * 게시글 검색 조건에 따라 페이징된 게시글 목록을 조회합니다.
     *
     * @param boardCodes 게시판 코드 리스트
     * @param type       검색 대상 필드명 ("all", "title", "content")
     * @param keyWord    검색 키워드
     * @param pageable   페이징 및 정렬 정보
     * @return 검색 결과 게시글 페이지
     */
    Page<Article> searchArticles(List<String> boardCodes, String type, String keyWord, Pageable pageable);

    /**
     * 지정된 게시글의 조회수를 1 증가시킵니다.
     *
     * @param boardCode  게시판 코드
     * @param articleSeq 게시글 번호
     */
    void increaseHitCount(String boardCode, int articleSeq);
}
