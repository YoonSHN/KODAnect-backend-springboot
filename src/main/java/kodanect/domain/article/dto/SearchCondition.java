package kodanect.domain.article.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
/**
 * 게시글 검색 조건을 담는 DTO
 *
 * 사용자가 입력한 검색어와 검색 필드를 기반으로 게시글 목록을 조회할 때 사용
 */
@Getter
@Setter
public class SearchCondition {

    @Pattern(regexp = "^(title|content|all)$", message = "검색 필드는 title, content, all 중 하나여야 합니다.")
    private String type = "all";

    @Size(max = 100, message = "검색어는 최대 100자까지 입력할 수 있습니다.")
    private String keyWord;
}