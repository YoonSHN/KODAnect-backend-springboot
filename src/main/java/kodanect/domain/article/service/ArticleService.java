package kodanect.domain.article.service;

import kodanect.domain.article.dto.ArticleDTO;
import kodanect.domain.article.dto.ArticleDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 게시글 조회 관련 기능을 정의한 서비스 인터페이스입니다.
 */
public interface ArticleService {

    /**
     * 게시글 목록을 페이징으로 조회합니다.
     *
     * <p>게시판 코드, 검색 필드, 키워드를 기반으로 조건에 맞는 게시글을 조회합니다.</p>
     *
     * @param boardCodes   게시판 코드 리스
     * @param type  검색 대상 필드명 ("all" "title", "content")
     * @param keyWord      검색 키워드
     * @param pageable     페이징 및 정렬 정보
     * @return {@link Page} 결과 게시글 목록 (게시판 유형에 따라 {@link ArticleDTO} 또는 하위 타입 반환)
     */
    Page<ArticleDTO> getArticles(List<String> boardCodes, String type, String keyWord, Pageable pageable);

    /**
     * 단일 게시글 상세 정보를 조회합니다.
     *
     * <p>게시글 본문과 함께, 이전/다음 글 정보도 함께 반환됩니다.</p>
     *
     * @param boardCode  게시판 코드
     * @param articleSeq 게시글 순번 (Primary Key)
     * @return {@link ArticleDetailDto} 게시글 상세 정보 + 이전/다음 게시글 참조 정보
     * @throws kodanect.domain.article.exception.ArticleNotFoundException 게시글이 존재하지 않을 경우
     */
    ArticleDetailDto getArticle(String boardCode, Integer articleSeq, String clientIpAddress);
}
