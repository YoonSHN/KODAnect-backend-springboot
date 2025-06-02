package kodanect.domain.remembrance.dto.common;

public interface ReplyAuthRequest {
    /* 권한 검증 공통 인터페이스 */
    Integer getDonateSeq();
    Integer getReplySeq();
    String getReplyPassword();
}
