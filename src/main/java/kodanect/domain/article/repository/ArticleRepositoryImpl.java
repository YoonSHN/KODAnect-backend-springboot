package kodanect.domain.article.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kodanect.domain.article.entity.Article;
import kodanect.domain.article.entity.QArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
/**
 * 게시글 커스텀 조회 구현체
 */
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 게시글 목록 검색 (조건: 게시판코드, 키워드, 페이징)
     *
     * @param boardCodes 게시판 코드 리스트
     * @param keyword    제목 또는 본문 검색어
     * @param pageable   페이지 정보
     * @return Page<Article> 결과 목록
     */
    @Override
    public Page<Article> searchArticles(List<String> boardCodes, String keyword, Pageable pageable) {

        QArticle article = QArticle.article;

        BooleanBuilder where = new BooleanBuilder();
        where.and(article.delFlag.eq("N"));

        if (boardCodes != null && !boardCodes.isEmpty()) {
            where.and(article.id.boardCode.in(boardCodes));
        }

        if (keyword != null && !keyword.isBlank()) {
            where.and(article.title.contains(keyword))
                    .or(article.contents.containsIgnoreCase(keyword));

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

    @Override
    public void increaseHitCount(String boardCode, int articleSeq) {
        QArticle article = QArticle.article;

        queryFactory.update(article)
                .set(article.readCount, article.readCount.add(1))
                .where(article.id.boardCode.eq(boardCode)
                        .and(article.id.articleSeq.eq(articleSeq)))
                .execute();
    }
}
