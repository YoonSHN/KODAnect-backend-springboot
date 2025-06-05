package kodanect.domain.article.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import kodanect.domain.article.entity.Article;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 조회 응답 DTO
 * - 게시글 기본 정보 + 파일 목록 포함
 */
@Getter
@Builder
public class ArticleDetailDto {

    private String boardCode;
    private Integer articleSeq;
    private String title;
    private String contents;
    private int readCount;
    private String fixFlag;

    private List<ArticleFileDto> files;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd a h:mm:ss", locale = "ko_KR")
    private LocalDateTime writeTime;

    private String writerId;

    public static ArticleDetailDto fromArticleDetailDto(Article article) {
        return ArticleDetailDto.builder()
                .boardCode(article.getId().getBoardCode())
                .articleSeq(article.getId().getArticleSeq())
                .title(article.getTitle())
                .contents(article.getContents())
                .readCount(article.getReadCount() != null ? article.getReadCount() : 0)
                .fixFlag(article.getFixFlag())
                .writeTime(article.getWriteTime())
                .writerId(article.getWriterId())
                .files(article.getFiles().stream()
                        .filter(f -> !"Y".equals(f.getDelFlag()))
                        .map(f -> ArticleFileDto.builder()
                                .fileName(f.getFileName())
                                .orgFileName(f.getOrgFileName())
                                .build())
                        .toList())
                .build();
    }
}
