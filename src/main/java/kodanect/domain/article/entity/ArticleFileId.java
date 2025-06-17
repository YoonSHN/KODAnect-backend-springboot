package kodanect.domain.article.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ArticleFileId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "board_code", nullable = false, length = 20)
    private String boardCode;

    @Column(name = "article_seq", nullable = false)
    private Integer articleSeq;

    @Column(name = "file_seq")
    private Integer fileSeq;

}
