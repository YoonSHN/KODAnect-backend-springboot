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
    public static final String INVALID_FILE_NAME = "file.invalidFileName";
    public static final String ARTICLE_DETAIL_SUCCESS = "article.detailSuccess";
    public static final String ARTICLE_LIST_SUCCESS = "article.listSuccess";
    public static final String RECIPIENT_COMMENT_NOT_FOUND = "recipient.comment.notfound";
    public static final String RECIPIENT_INVALID_DATA = "recipient.invalid.data";
    public static final String RECIPIENT_INVALID_PASSCODE = "recipient.invalid.passcode";
    public static final String RECIPIENT_NOT_FOUND = "recipient.notfound";
    public static final String COMMON_INVALID_INTEGER_CONVERSION = "common.invalid.integer.conversion";

    // 추모관 관련
    public static final String DONATE_NOT_FOUND = "error.donate.notFound";
    public static final String DONATE_INVALID = "error.donate.invalid";
    public static final String EMOTION_INVALID = "error.emotion.invalid";
    public static final String CONTENTS_INVALID = "error.contents.invalid";
    public static final String PAGINATION_INVALID = "error.pagination.invalid";
    public static final String SEARCH_DATE_FORMAT_INVALID = "error.search.date.format.invalid";
    public static final String SEARCH_DATE_RANGE_INVALID = "error.search.date.range.invalid";
    public static final String SEARCH_DATE_MISSING = "error.search.date.missing";
    public static final String COMMENT_NOT_FOUND = "error.comment.notFound";
    public static final String COMMENT_INVALID = "error.comment.invalid";
    public static final String COMMENT_ALREADY_DELETED = "error.comment.alreadyDeleted";
    public static final String COMMENT_PASSWORD_MISMATCH = "error.comment.password.mismatch";
    public static final String COMMENT_WRITER_EMPTY = "error.comment.writer.empty";
    public static final String COMMENT_WRITER_INVALID = "error.comment.writer.invalid";
    public static final String COMMENT_PASSWORD_EMPTY = "error.comment.password.empty";
    public static final String COMMENT_PASSWORD_INVALID = "error.comment.password.invalid";
    public static final String COMMENT_CONTENTS_EMPTY = "error.comment.contents.empty";

    // 하늘나라 편지 관련
    public static final String HEAVEN_DONOR_INFORM_MISMATCH = "heaven.donor.mismatch.error";
    public static final String HEAVEN_PASSWORD_MISMATCH = "heaven.password.mismatch.error";
    public static final String HEAVEN_TYPE_INVALID = "heaven.type.invalid.error";
    public static final String HEAVEN_FILE_SAVE_FAIL = "heaven.file.save.error";
    public static final String HEAVEN_NOT_FOUND = "heaven.notFound.error";
    public static final String HEAVEN_COMMENT_NOT_FOUND = "heaven.comment.notFound.error";
    public static final String HEAVEN_COMMENT_INFORM_MISMATCH = "heaven.comment.mismatch.error";

    private MessageKeys() {}

}
