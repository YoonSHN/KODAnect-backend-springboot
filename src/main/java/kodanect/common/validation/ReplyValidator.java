package kodanect.common.validation;

import kodanect.domain.remembrance.entity.MemorialReply;
import kodanect.domain.remembrance.dto.MemorialReplyDto;
import kodanect.domain.remembrance.exception.*;

public class ReplyValidator {

    private ReplyValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void validateReplyContent(MemorialReplyDto memorialReplyDto) throws MissingReplyContentException {
        /* 댓글 내용 검증 */
        if(memorialReplyDto.getReplyContents() == null || memorialReplyDto.getReplyContents().trim().isEmpty()) {
            throw new MissingReplyContentException();
        }
    }

    public static void validateReplyWriteFields(MemorialReplyDto memorialReplyDto)
            throws  MissingReplyContentException,
                    MissingReplyWriterException,
                    MissingReplyPasswordException
    {
        /* 댓글 내용 검증 */
        validateReplyContent(memorialReplyDto);

        /* 작성자 검증 */
        if(memorialReplyDto.getReplyWriter() == null || memorialReplyDto.getReplyWriter().trim().isEmpty()) {
            throw new MissingReplyWriterException();
        }

        /* 비밀 번호 검증 */
        else if(memorialReplyDto.getReplyPassword() == null || memorialReplyDto.getReplyPassword().trim().isEmpty()) {
            throw new MissingReplyPasswordException();
        }
    }

    public static void validateReplyAuthority(
            Integer donateSeq, Integer replySeq,
            MemorialReplyDto memorialReplyDto, MemorialReply reply)
            throws  ReplyPostMismatchException,
                    ReplyIdMismatchException,
                    MissingReplyPasswordException,
                    ReplyPasswordMismatchException,
                    ReplyAlreadyDeleteException
    {
        /* 게시글 검증 */
        if(!donateSeq.equals(reply.getDonateSeq()) || !donateSeq.equals(memorialReplyDto.getDonateSeq())) {
            throw new ReplyPostMismatchException();
        }

        /* 댓글 검증 */
        else if(!replySeq.equals(memorialReplyDto.getReplySeq())){
            throw new ReplyIdMismatchException();
        }

        /* 삭제 여부 검증 */
        else if(!reply.getDelFlag().equals("N")){
            throw new ReplyAlreadyDeleteException();
        }

        /* 비밀 번호 검증 */
        else if(reply.getReplyPassword() == null || reply.getReplyPassword().trim().isEmpty()) {
            throw new MissingReplyPasswordException();
        }

        /* 비밀 번호 비교 */
        else if(!reply.getReplyPassword().equals(memorialReplyDto.getReplyPassword())) {
            throw new ReplyPasswordMismatchException();
        }
    }

}

