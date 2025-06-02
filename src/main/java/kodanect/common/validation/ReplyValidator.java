package kodanect.common.validation;

import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.dto.common.ReplyAuthRequest;
import kodanect.domain.remembrance.dto.common.ReplyContentHolder;
import kodanect.domain.remembrance.entity.MemorialReply;
import kodanect.domain.remembrance.exception.*;

public class ReplyValidator {

    private ReplyValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void validateReplyContent(ReplyContentHolder replyContentHolder) throws MissingReplyContentException {
        /* 댓글 내용 검증 */
        if(replyContentHolder.getReplyContents() == null || replyContentHolder.getReplyContents().trim().isEmpty()) {
            throw new MissingReplyContentException();
        }
    }

    public static void validateReplyWriteFields(MemorialReplyCreateRequest memorialReplyCreateRequest)
            throws  MissingReplyContentException,
                    MissingReplyWriterException,
                    MissingReplyPasswordException
    {
        /* 댓글 내용 검증 */
        validateReplyContent(memorialReplyCreateRequest);

        /* 작성자 검증 */
        if(memorialReplyCreateRequest.getReplyWriter() == null || memorialReplyCreateRequest.getReplyWriter().trim().isEmpty()) {
            throw new MissingReplyWriterException();
        }

        /* 비밀 번호 검증 */
        else if(memorialReplyCreateRequest.getReplyPassword() == null || memorialReplyCreateRequest.getReplyPassword().trim().isEmpty()) {
            throw new MissingReplyPasswordException();
        }
    }

    public static void validateReplyAuthority(
            Integer donateSeq, Integer replySeq,
            ReplyAuthRequest replyAuthRequest, MemorialReply memorialReply)
            throws  ReplyPostMismatchException,
                    ReplyIdMismatchException,
                    MissingReplyPasswordException,
                    ReplyPasswordMismatchException,
                    ReplyAlreadyDeleteException
    {
        /* 게시글 검증 */
        if(!donateSeq.equals(memorialReply.getDonateSeq()) || !donateSeq.equals(replyAuthRequest.getDonateSeq())) {
            throw new ReplyPostMismatchException();
        }

        /* 댓글 검증 */
        else if(!replySeq.equals(replyAuthRequest.getReplySeq())){
            throw new ReplyIdMismatchException();
        }

        /* 삭제 여부 검증 */
        else if(!memorialReply.getDelFlag().equals("N")){
            throw new ReplyAlreadyDeleteException();
        }

        /* 비밀 번호 검증 */
        else if(memorialReply.getReplyPassword() == null || memorialReply.getReplyPassword().trim().isEmpty()) {
            throw new MissingReplyPasswordException();
        }

        /* 비밀 번호 비교 */
        else if(!memorialReply.getReplyPassword().equals(replyAuthRequest.getReplyPassword())) {
            throw new ReplyPasswordMismatchException();
        }
    }

}

