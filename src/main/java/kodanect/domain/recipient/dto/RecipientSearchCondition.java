package kodanect.domain.recipient.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientSearchCondition {

    // 검색 키워드
    private String searchKeyword;

    // 검색 타입 (예: SearchType.TITLE, SearchType.CONTENTS, SearchType.ALL)
    private SearchType searchType;

}
