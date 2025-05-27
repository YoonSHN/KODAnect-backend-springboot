package kodanect.domain.article.dto;

import kodanect.domain.article.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDTO {

    private String boardCode;

    private Integer articleSeq;

    private String title;

    private boolean isFixed;

    private LocalDate writeDate;

    private boolean isNew;

    public static ArticleDTO fromArticle(Article art) {
        return ArticleDTO.builder()
                .boardCode(art.getId().getBoardCode())
                .articleSeq(art.getId().getArticleSeq())
                .title(art.getTitle())
                .isFixed("Y".equalsIgnoreCase(art.getFixFlag()))
                .writeDate(art.getWriteTime().toLocalDate())
                .isNew(art.getWriteTime().isAfter(LocalDateTime.now().minusDays(3)))
                .build();

    }
}
