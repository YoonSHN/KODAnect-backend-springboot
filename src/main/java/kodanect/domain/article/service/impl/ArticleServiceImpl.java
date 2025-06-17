package kodanect.domain.article.service.impl;

import kodanect.common.util.RequestBasedHitLimiter;
import kodanect.domain.article.exception.ArticleNotFoundException;
import kodanect.domain.article.dto.ArticleDTO;
import kodanect.domain.article.dto.ArticleDetailDto;
import kodanect.domain.article.dto.MakePublicDTO;
import kodanect.domain.article.entity.Article;
import kodanect.domain.article.repository.ArticleRepository;
import kodanect.domain.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * {@inheritDoc}
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final RequestBasedHitLimiter hitLimiter;

    /**
     * 게시글 목록을 조건에 따라 조회합니다.
     *
     * <p>특정 게시판 코드 목록, 검색 필드(type), 키워드를 기준으로 페이징된 게시글 목록을 반환합니다.
     * 게시판 코드가 특정 값("32")인 경우는 별도의 DTO로 변환됩니다.</p>
     *
     * @param boardCodes 게시판 코드 리스트 (예: "notice", "event")
     * @param type       검색 필드명 (예: "title", "content")
     * @param keyWord    검색 키워드
     * @param pageable   페이징 정보
     * @return 조건에 맞는 게시글 목록 페이지
     */
    @Override
    public Page<ArticleDTO> getArticles(List<String> boardCodes, String type, String keyWord, Pageable pageable) {
        if (boardCodes == null || boardCodes.isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Article> articles = articleRepository.searchArticles(boardCodes, type, keyWord, pageable);
        if (articles == null) {
            return Page.empty(pageable);
        }
        String boardCode = boardCodes.get(0);

        if ("32".equals(boardCode)) {
            return articles.map(article -> (ArticleDTO) MakePublicDTO.fromArticleToMakePublicDto(article));
        }
        return articles.map(ArticleDTO::fromArticle);
    }

    /**
     * 게시글 상세 정보를 조회합니다.
     *
     * <p>요청된 게시글이 존재하지 않으면 예외를 발생시키며,
     * 이전/다음 게시글 정보를 포함한 DTO를 반환합니다.
     * 조회 시 조회수(hit count)가 1 증가합니다.</p>
     *
     * @param boardCode  게시판 코드
     * @param articleSeq 게시글 순번 (PK)
     * @return 게시글 상세 정보 DTO
     * @throws ArticleNotFoundException 해당 게시글이 존재하지 않을 경우
     */
    @Transactional
    @Override
    public ArticleDetailDto getArticle(String boardCode, Integer articleSeq, String clientIpAddress) {

        if (hitLimiter.isFirstView(boardCode, articleSeq, clientIpAddress)) {
            articleRepository.increaseHitCount(boardCode, articleSeq);
        }

        Article article = articleRepository.findByIdBoardCodeAndIdArticleSeq(boardCode, articleSeq)
                .orElseThrow(() -> new ArticleNotFoundException(articleSeq));

        Article prev = articleRepository.findFirstByIdBoardCodeAndIdArticleSeqLessThanAndDelFlagOrderByIdArticleSeqDesc(boardCode, articleSeq, "N").orElse(null);
        Article next = articleRepository.findFirstByIdBoardCodeAndIdArticleSeqGreaterThanAndDelFlagOrderByIdArticleSeqAsc(boardCode, articleSeq, "N").orElse(null);

        ArticleDetailDto.AdjacentArticleDto prevDto = prev != null
                ? ArticleDetailDto.AdjacentArticleDto.from(prev)
                : ArticleDetailDto.AdjacentArticleDto.noPrev();

        ArticleDetailDto.AdjacentArticleDto nextDto = next != null
                ? ArticleDetailDto.AdjacentArticleDto.from(next)
                : ArticleDetailDto.AdjacentArticleDto.noNext();

        return ArticleDetailDto.fromArticleDetailDto(article, prevDto, nextDto);
    }
}
