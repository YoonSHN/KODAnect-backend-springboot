package kodanect.domain.article.dto;

import kodanect.common.exception.custom.InvalidBoardOptionException;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 게시판 옵션과 실제 DB의 boardCode 간 매핑을 담당하는 enum.
 */
public enum BoardOption {
    NOTICE("1", "7"),
    RECRUIT("2", "27"),
    MAKE_PUBLIC("makepublic", "32"),
    ALL("all", null);

    private final String paramValue;
    @Getter
    private final String boardCode;

    BoardOption(String paramValue, String boardCode) {
        this.paramValue = paramValue;
        this.boardCode = boardCode;
    }

    public List<String> resolveBoardCodes() {
        return switch (this) {
            case NOTICE -> List.of("7");
            case RECRUIT -> List.of("27");
            case MAKE_PUBLIC -> List.of("32");
            case ALL -> List.of("7", "27");
        };
    }

    public static boolean isValid(String boardCode) {
        return Arrays.stream(values())
                .anyMatch(opt ->
                        (opt.boardCode != null && opt.boardCode.equalsIgnoreCase(boardCode))
                                || (opt.paramValue != null && opt.paramValue.equalsIgnoreCase(boardCode))
                );
    }

    public static BoardOption fromParam(String paramValue) {
        return Arrays.stream(values())
                .filter(opt -> opt.paramValue.equalsIgnoreCase(paramValue))
                .findFirst()
                .orElseThrow(() -> new InvalidBoardOptionException(paramValue));
    }

}
