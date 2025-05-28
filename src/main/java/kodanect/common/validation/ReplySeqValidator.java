package kodanect.common.validation;

import kodanect.domain.remembrance.exception.InvalidReplySeqException;

public class ReplySeqValidator {

    public static void replySeqValidate(Integer replySeq) throws InvalidReplySeqException {
        /* 댓글 ID 검증 */
        if(replySeq == null || replySeq < 1) {
            throw new InvalidReplySeqException();
        }
    }
}
