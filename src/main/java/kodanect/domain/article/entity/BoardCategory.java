package kodanect.domain.article.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "tb25_200_board_category")
public class BoardCategory {

    @Id
    @Column(name = "board_code", length = 20)
    private String boardCode;

    @Column(name = "board_name", length = 600, nullable = false)
    private String boardName;

    @Column(name = "board_type_code", length = 10)
    private String boardTypeCode;

    @Column(name = "file_count")
    private Integer fileCount;

    @Column(name = "html_flag", length = 1)
    private String htmlFlag;

    @Column(name = "reply_flag", length = 1)
    private String replyFlag;

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

    @Builder.Default
    @OneToMany(mappedBy = "boardCategory", cascade = CascadeType.ALL)
    private List<Article> articles = new ArrayList<>();
}
