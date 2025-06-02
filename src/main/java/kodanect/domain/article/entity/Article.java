package kodanect.domain.article.entity;

import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "tb25_210_article_dtl")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Article {

    @EmbeddedId
    private ArticleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_code", insertable = false, updatable = false)
    private BoardCategory boardCategory;

    @Column(name = "title", length = 600, nullable = false)
    private String title;

    @Column(name = "contents", columnDefinition = "TEXT", nullable = false)
    private String contents;

    @Builder.Default
    @Column(name = "read_count")
    private Integer readCount = 0;

    @Column(name = "reply_title", length = 600)
    private String replyTitle;

    @Column(name = "reply_contents", length = 3000)
    private String replyContents;

    @Column(name = "reply_writer_id", length = 60)
    private String replyWriterId;

    @Column(name = "fix_flag", length = 1)
    private String fixFlag;

    @Column(name = "article_passcode", length = 60)
    private String articlePasscode;

    @Column(name = "article_url", length = 600)
    private String articleUrl;

    @Builder.Default
    @Column(name = "del_flag", length = 1, nullable = false)
    private String delFlag = "N";

    @Builder.Default
    @Column(name = "write_time", nullable = false, updatable = false)
    private LocalDateTime writeTime = LocalDateTime.now();

    @Column(name = "writer_id", length = 60, nullable = false)
    private String writerId;

    @Builder.Default
    @Column(name = "modify_time", nullable = false)
    private LocalDateTime modifyTime = LocalDateTime.now();

    @Column(name = "modifier_id", length = 60, nullable = false)
    private String modifierId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
    @Where(clause = "del_flag = 'N'")
    private List<ArticleFile> files = new ArrayList<>();
}
