package kodanect.domain.article.dto;

import kodanect.domain.article.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * 게시글 DTO
 *
 * ArticleDTO를 확장하여 공개 시작일과 공개 방식(disclosureMethod)을 추가로 포함
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MakePublicDTO extends ArticleDTO {

    private LocalDate startDate;
    private String disclosureMethod;

    public static MakePublicDTO fromArticleToMakePublicDto(Article article) {

        return MakePublicDTO.builder()
                .boardCode(article.getId().getBoardCode())
                .articleSeq(article.getId().getArticleSeq())
                .title(article.getTitle())
                .isFixed("Y".equalsIgnoreCase(article.getFixFlag()))
                .writeDate(article.getWriteTime().toLocalDate())
                .isNew(article.getWriteTime().isAfter(java.time.LocalDateTime.now().minusDays(3)))
                .startDate(article.getStartDate())
                .disclosureMethod("홈페이지")
                .build();
    }
}
