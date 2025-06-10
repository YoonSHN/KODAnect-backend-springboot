package kodanect.domain.article.repository;

import kodanect.domain.article.exception.InvalidBoardCodeException;
import kodanect.domain.article.entity.BoardCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 게시판 분류(BoardCategory) 정보를 캐싱하여 boardCode, URL param 등을 통한 조회를 빠르게 제공.
 * DB 접근을 최소화하고 enum 기반 매핑 제거, URL param 매핑 지원 등의 목적.
 * 현재 캐싱 방식은 인메모리 캐싱 방식을 사용하기 때문에 백오프에서 DB 추가구현시 reload 메서드 적용 필요
 * 사실 컬럼 하나 추가하면 끝나긴하지만 현재 DB를 변경없이 작동하는 방식을 고려
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoardCategoryCache {

    private final Map<String, BoardCategory> boardCodeMap = new ConcurrentHashMap<>();
    private final Map<String, BoardCategory> urlParamMap = new ConcurrentHashMap<>();

    private final BoardCategoryRepository repository;

    /**
     * 애플리케이션 시작 시 DB에서 게시판 목록을 모두 불러와 캐시에 저장.
     * 삭제되지 않은(delFlag != 'Y') 항목만 사용.
     */
    @PostConstruct
    public void init() {
        List<BoardCategory> categories = repository.findAll();

        for (BoardCategory category : categories) {
            if (!"Y".equals(category.getDelFlag())) {
                String boardCode = category.getBoardCode();
                boardCodeMap.put(boardCode, category);

                String paramValue = switch (boardCode) {
                    case "7"  -> "1";
                    case "27" -> "2";
                    case "32" -> "makepublic";
                    default   -> null;
                };

                if (paramValue != null) {
                    urlParamMap.put(paramValue.toLowerCase(), category);
                }
            }
        }
    }

    public BoardCategory getByBoardCode(String boardCode) {
        BoardCategory category = boardCodeMap.get(boardCode);
        if (category == null) {
            throw new InvalidBoardCodeException(boardCode);
        }
        return category;
    }

    public List<String> getAllBoardCodesForOptions() {
        return urlParamMap.values().stream()
                .map(BoardCategory::getBoardCode)
                .toList();
    }

    public BoardCategory getByUrlParam(String urlParam) {
        BoardCategory category = urlParamMap.get(urlParam.toLowerCase());
        if (category == null) {
            throw new InvalidBoardCodeException(urlParam);
        }
        return category;
    }

    public String getBoardCodeByUrlParam(String urlParam) {
        return getByUrlParam(urlParam).getBoardCode();
    }
    /**
     * 게시판 캐시를 강제로 재로딩
     * DB에서 다시 읽어와 boardCodeMap과 urlParamMap을 초기화
     * - 백오피스 등에서 게시판 데이터가 변경된 경우 수동 호출
     */
    public void reload() {
        boardCodeMap.clear();
        urlParamMap.clear();
        init();
    }
}
