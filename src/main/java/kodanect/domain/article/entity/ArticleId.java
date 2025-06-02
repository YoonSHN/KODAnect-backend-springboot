package kodanect.domain.article.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;


@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ArticleId implements Serializable {

    @Column(name = "board_code", length = 20)
    private String boardCode;

    @Column(name = "article_seq")
    private Integer articleSeq;

}
