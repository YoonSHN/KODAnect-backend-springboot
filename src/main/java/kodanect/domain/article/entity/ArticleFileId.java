package kodanect.domain.article.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ArticleFileId implements Serializable {

    @Column(name = "board_code", nullable = false, length = 20)
    private String board_code;


    @Column(name = "article_seq", nullable = false)
    private Integer articleSeq;

    @Column(name = "file_seq")
    private Integer fileSeq;

}
