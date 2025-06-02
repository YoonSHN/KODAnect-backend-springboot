package kodanect.domain.article.repository;

import config.TestConfig;
import kodanect.domain.article.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private BoardCategoryRepository boardCategoryRepository;

    @BeforeEach
    void setup() {
        BoardCategory category = BoardCategory.builder()
                .boardCode("7")
                .boardName("공지사항")
                .boardTypeCode("NTC")
                .fileCount(0)
                .htmlFlag("N")
                .replyFlag("N")
                .delFlag("N")
                .writeTime(LocalDateTime.now())
                .writerId("admin")
                .modifyTime(LocalDateTime.now())
                .modifierId("admin")
                .build();

        boardCategoryRepository.save(category);
    }


    @Test
    @DisplayName("연관관계 저장 및 fetch 테스트")
    void testFindWithFilesByBoardCodeAndArticleSeq() {
        // given
        ArticleId articleId = ArticleId.builder()
                .boardCode("7")
                .articleSeq(1)
                .build();

        Article article = Article.builder()
                .id(articleId)
                .title("테스트 제목")
                .contents("본문입니다")
                .writerId("admin")
                .modifierId("admin")
                .build();

        ArticleFileId fileId = ArticleFileId.builder()
                .boardCode("7")
                .articleSeq(1)
                .fileSeq(1)
                .build();

        ArticleFile file = ArticleFile.builder()
                .id(fileId)
                .fileName("/files/sample.jpg")
                .filePathName("/files/sample.jpg")
                .article(article)
                .modifierId("admin")
                .writerId("admin")
                .build();

        article.getFiles().add(file);

        articleRepository.save(article);

        // when
        Optional<Article> result = articleRepository.findWithFilesByBoardCodeAndArticleSeq("7", 1);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getFiles()).hasSize(1);
        assertThat(result.get().getFiles().get(0).getId().getFileSeq()).isEqualTo(1);
        assertThat(result.get().getFiles().get(0).getFileName()).isEqualTo("/files/sample.jpg");
    }
}
