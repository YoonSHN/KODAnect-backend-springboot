package kodanect.domain.article.service.impl;

import kodanect.common.exception.custom.ArticleNotFoundException;
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

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;

    /**
     * 게시글 목록 조회
     *
     * @param boardCodes 게시판 유형
     * @param keyword    검색 키워드 (제목/내용)
     * @param pageable   페이징 정보
     * @return 페이징 ArticleDTO 리스트
     */
    public Page<? extends ArticleDTO> getArticles(List<String> boardCodes, String searchField, String keyword, Pageable pageable) {
        Page<Article> articles = articleRepository.searchArticles(boardCodes, searchField, keyword, pageable);
        if (articles == null) {
            return Page.empty(pageable);
        }

        String boardCode = boardCodes.get(0);

        // 카테고리 증가시 factory + Strategy 패턴으로 변경고려
        switch (boardCode) {
            case "32":
                return articles.map(MakePublicDTO::fromArticleToMakePublicDto);
            default:
                return articles.map(ArticleDTO::fromArticle);
        }
    }

    /**
     * 게시글 상세 조회
     *
     * @param boardCode  게시판 코드
     * @param articleSeq 게시글 순번
     * @return ArticleDetailDto (존재하지 않으면 예외 발생)
     */
    @Transactional
    @Override
    public ArticleDetailDto getArticle(String boardCode, Integer articleSeq) {

        articleRepository.increaseHitCount(boardCode, articleSeq);

        Article article = articleRepository.findWithFilesByBoardCodeAndArticleSeq(boardCode, articleSeq)
                .orElseThrow(() -> new ArticleNotFoundException(articleSeq));

        return ArticleDetailDto.fromArticleDetailDto(article);
    }
}
