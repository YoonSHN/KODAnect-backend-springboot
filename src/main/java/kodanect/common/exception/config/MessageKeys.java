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
    public static final String ARTICLE_NOT_FOUND = "article.notFound";
    public static final String INVALID_BOARD_CODE = "board.invalidCode";
    public static final String FILE_NOT_FOUND = "file.notFound";
    public static final String FILE_ACCESS_VIOLATION = "file.accessViolation";
    public static final String ARTICLE_DETAIL_SUCCESS = "article.detailSuccess";
    public static final String ARTICLE_LIST_SUCCESS = "article.listSuccess";

    // 추모관 관련
    public static final String DONATE_NOT_FOUND = "error.donate.notFound";
    public static final String DONATE_INVALID = "error.donate.invalid";
    public static final String REPLY_NOT_FOUND = "error.reply.notFound";
    public static final String REPLY_INVALID = "error.reply.invalid";
    public static final String EMOTION_INVALID = "error.emotion.invalid";
    public static final String REPLY_ALREADY_DELETED = "error.reply.alreadyDeleted";
    public static final String REPLY_PASSWORD_MISMATCH = "error.reply.password.mismatch";
    public static final String PAGINATION_INVALID = "error.pagination.range";
    public static final String SEARCH_DATE_FORMAT_INVALID = "error.search.date.format.invalid";
    public static final String SEARCH_DATE_RANGE_INVALID = "error.search.date.range.invalid";
    public static final String SEARCH_DATE_MISSING = "error.search.date.missing";
    public static final String REPLY_WRITER_EMPTY = "error.reply.writer.empty";
    public static final String REPLY_PASSWORD_EMPTY = "error.reply.password.empty";
    public static final String REPLY_PASSWORD_INVALID = "error.reply.password.invalid";
    public static final String REPLY_CONTENTS_EMPTY = "error.reply.contents.empty";

    private MessageKeys() {}

}
