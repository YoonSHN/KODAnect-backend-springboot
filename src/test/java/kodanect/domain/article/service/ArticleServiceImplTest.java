package kodanect.domain.article.service;

import kodanect.common.exception.custom.ArticleNotFoundException;
import kodanect.domain.article.dto.ArticleDTO;
import kodanect.domain.article.dto.ArticleDetailDto;
import kodanect.domain.article.entity.Article;
import kodanect.domain.article.entity.ArticleFile;
import kodanect.domain.article.entity.ArticleFileId;
import kodanect.domain.article.entity.ArticleId;
import kodanect.domain.article.repository.ArticleRepository;
import kodanect.domain.article.service.impl.ArticleServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ArticleServiceImplTest {

    private ArticleRepository articleRepository;
    private ArticleServiceImpl articleService;

    @Before
    public void setUp() {
        articleRepository = mock(ArticleRepository.class);
        articleService = new ArticleServiceImpl(articleRepository);
    }

    @Test
    public void getArticles_noticeType_returnsExpectedResult() {
        // given
        Article article = Article.builder()
                .id(ArticleId.builder().boardCode("7").articleSeq(1).build())
                .title("공지")
                .contents("내용")
                .fixFlag("Y")
                .delFlag("N")
                .writeTime(LocalDateTime.now())
                .writerId("admin")
                .build();

        Page<Article> articlePage = new PageImpl<>(
                Collections.singletonList(article),
                PageRequest.of(0, 10),
                1
        );

        when(articleRepository.searchArticles(eq(List.of("7")), any() ,any(), any(Pageable.class)))
                .thenReturn(articlePage);

        // when
        Page<? extends ArticleDTO> result = articleService.getArticles(List.of("7"), "제목","공지", PageRequest.of(0, 10));

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("7", result.getContent().get(0).getBoardCode());
        assertTrue(result.getContent().get(0).isFixed());
    }

    @Test
    public void getArticles_withNullKeyword_shouldNotFail() {
        // given
        Article article = Article.builder()
                .id(ArticleId.builder().boardCode("27").articleSeq(2).build())
                .title("채용 공고")
                .contents("인턴 모집")
                .fixFlag("N")
                .delFlag("N")
                .writeTime(LocalDateTime.now().minusDays(2))
                .writerId("hr")
                .build();

        Page<Article> articlePage = new PageImpl<>(
                Collections.singletonList(article),
                PageRequest.of(0, 10),
                1
        );

        when(articleRepository.searchArticles(eq(List.of("27")), isNull(),isNull(), any(Pageable.class)))
                .thenReturn(articlePage);

        // when
        Page<? extends ArticleDTO> result = articleService.getArticles(List.of("27"), null ,null, PageRequest.of(0, 10));

        // then
        assertEquals(1, result.getTotalElements());
        assertFalse(result.getContent().get(0).isFixed());
        assertTrue(result.getContent().get(0).isNew());
    }

    @Test
    public void getArticles_allType_shouldIncludeNoticeAndRecruit() {
        // given
        Article article = Article.builder()
                .id(new ArticleId("7", 1))
                .title("통합 테스트")
                .contents("전체 게시판")
                .fixFlag("N")
                .delFlag("N")
                .writeTime(LocalDateTime.now())
                .writerId("admin")
                .build();

        List<String> expectedBoardCodes = List.of("7", "27");

        Page<Article> mockPage = new PageImpl<>(List.of(article), PageRequest.of(0, 10), 1);

        when(articleRepository.searchArticles(eq(expectedBoardCodes),  isNull(),isNull(), any(Pageable.class)))
                .thenReturn(mockPage);

        // when
        Page<? extends ArticleDTO> result = articleService.getArticles(expectedBoardCodes, null,null, PageRequest.of(0, 10));

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("7", result.getContent().get(0).getBoardCode());
    }

    @Test
    public void getArticle_existingArticle_returnsDetailDto() {
        // given
        Article article = Article.builder()
                .id(ArticleId.builder().boardCode("7").articleSeq(1).build())
                .title("공지")
                .contents("내용.")
                .fixFlag("N")
                .delFlag("N")
                .writeTime(LocalDateTime.of(2023, 11, 11, 13, 11, 10))
                .writerId("관리자")
                .build();

        when(articleRepository.findWithFilesByBoardCodeAndArticleSeq("7", 1))
                .thenReturn(Optional.of(article));

        // when
        ArticleDetailDto dto = articleService.getArticle("7", 1);

        // then
        assertNotNull(dto);
        assertEquals("공지", dto.getTitle());
        assertEquals("관리자", dto.getWriterId());
    }

    @Test(expected = ArticleNotFoundException.class)
    public void getArticle_notExistArticle_throwException() {
        // given
        when(articleRepository.findWithFilesByBoardCodeAndArticleSeq("7", 999))
                .thenReturn(Optional.empty());

        // when
        articleService.getArticle("7", 999);
    }

    @Test
    public void getArticle_shouldReturnFiles() {
        // given
        ArticleFile file = ArticleFile.builder()
                .id(new ArticleFileId("1", 7, 1))
                .fileName("abc123.pdf")
                .orgFileName("첨부파일.pdf")
                .filePathName("/upload/7/1/abc123.pdf")
                .delFlag("N")
                .build();

        Article article = Article.builder()
                .id(new ArticleId("7", 1))
                .title("공지사항")
                .contents("본문")
                .fixFlag("N")
                .delFlag("N")
                .writeTime(LocalDateTime.now())
                .writerId("admin")
                .files(List.of(file))
                .build();

        when(articleRepository.findWithFilesByBoardCodeAndArticleSeq("7", 1))
                .thenReturn(Optional.of(article));

        // when
        ArticleDetailDto dto = articleService.getArticle("7", 1);

        // then
        assertNotNull(dto.getFiles());
        assertEquals(1, dto.getFiles().size());
        assertEquals("첨부파일.pdf", dto.getFiles().get(0).getOrgFileName());
    }
}
