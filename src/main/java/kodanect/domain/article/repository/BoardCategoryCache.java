package kodanect.domain.article.repository;

import kodanect.common.exception.config.SecureLogger;
import kodanect.domain.article.exception.InvalidBoardCodeException;
import kodanect.domain.article.entity.BoardCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 게시판 분류 {@link BoardCategory} 정보를 애플리케이션 내 메모리에 캐싱하여 빠르게 제공하는 컴포넌트입니다.
 *
 * <p>현재는 단순 인메모리 캐시이므로, 게시판 정보가 변경된 경우 수동 {@link #reload()} 호출이 필요합니다.</p>
 * 사실 컬럼 하나 추가하면 끝나긴하지만 현재 DB를 변경없이 작동하는 방식을 고려
 *
 * @see BoardCategoryRepository
 * @see BoardCategory
 */

@Component
@RequiredArgsConstructor
public class BoardCategoryCache {

    private static final SecureLogger log = SecureLogger.getLogger(BoardCategoryCache.class);

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
            log.warn("잘못된 optionStr 요청");
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
