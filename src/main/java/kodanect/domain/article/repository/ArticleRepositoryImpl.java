package kodanect.domain.article.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kodanect.domain.article.entity.Article;
import kodanect.domain.article.entity.QArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * {@link ArticleRepositoryCustom}의 구현체로,
 * 게시글의 커스텀 조회 및 비즈니스 쿼리를 처리합니다.
 *
 * <p>QueryDSL을 사용하여 동적 검색 조건과 조회수 증가 기능을 제공합니다.</p>
 *
 * @author gkr97
 * @see ArticleRepositoryCustom
 * @see Article
 */
@Slf4j
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 게시글 목록 검색 (조건: 게시판코드, 검색 필드, 키워드, 페이징)
     *
     * @param boardCodes 게시판 코드 리스트
     * @param type "title" | "contents" | "all"
     * @param keyWord 검색어
     * @param pageable 페이지 정보
     * @return Page<Article> 결과 목록
     */
    @Override
    public Page<Article> searchArticles(List<String> boardCodes, String type, String keyWord, Pageable pageable) {

        QArticle article = QArticle.article;

        BooleanBuilder where = new BooleanBuilder();
        where.and(article.delFlag.eq("N"));

        if (boardCodes != null && !boardCodes.isEmpty()) {
            where.and(article.id.boardCode.in(boardCodes));
        }

        if (keyWord != null && !keyWord.isBlank()) {
            BooleanBuilder keywordCondition = new BooleanBuilder();

            String effectiveField = (type != null) ? type : "all";

            switch (effectiveField) {
                case "title":
                    keywordCondition.or(article.title.containsIgnoreCase(keyWord));
                    break;
                case "contents":
                    keywordCondition.or(article.contents.containsIgnoreCase(keyWord));
                    break;
                case "all":
                    keywordCondition.or(article.title.containsIgnoreCase(keyWord));
                    keywordCondition.or(article.contents.containsIgnoreCase(keyWord));
                    break;
                default:
                    log.warn("잘못된 searchField 값: {}", type);
            }

            where.and(keywordCondition);
        }

        List<Article> content = queryFactory
                .selectFrom(article)
                .where(where)
                .orderBy(article.fixFlag.desc(), article.writeTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(article.count())
                .from(article)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 조회수 증가
     * @param boardCode 게시판 코드
     * @param articleSeq 게시글 ID
     */
    @Override
    public void increaseHitCount(String boardCode, int articleSeq) {
        QArticle article = QArticle.article;

        long updated = queryFactory.update(article)
                .set(article.readCount, article.readCount.add(1))
                .where(article.id.boardCode.eq(boardCode)
                        .and(article.id.articleSeq.eq(articleSeq)))
                .execute();

        if (updated == 0) {
            log.warn("조회수 증가 실패: boardCode={}, articleSeq={}", boardCode, articleSeq);
        }
    }
}
