package kodanect.common.exception.config;

/**
 * 국제화 메시지 키 상수 정의
 *
 * 역할:
 * - 메시지 키 하드코딩 방지
 * - 메시지 키 일괄 관리
 */
public final class MessageKeys {
    // 게시글 관련
    public static final String ARTICLE_LIST_SUCCESS = "article.list.success";
    public static final String ARTICLE_DETAIL_SUCCESS = "article.detail.success";
    public static final String ARTICLE_NOT_FOUND = "exception.article.not-found";

    // 파일 관련
    public static final String FILE_NOT_FOUND = "file.not.found";
    public static final String FILE_DOWNLOAD_ERROR = "file.download.error";

    // 게시판 옵션 관련
    public static final String INVALID_BOARD_OPTION = "exception.board.invalid-option";

    private MessageKeys() {}

}
