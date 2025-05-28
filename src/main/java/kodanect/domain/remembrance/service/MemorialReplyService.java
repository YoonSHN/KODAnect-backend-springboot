package kodanect.domain.remembrance.service;

import kodanect.domain.remembrance.dto.MemorialReplyDto;
import kodanect.domain.remembrance.exception.*;

import java.util.List;

public interface MemorialReplyService {
    /* 게시글 댓글 작성 */
    void createReply(Integer donateSeq, MemorialReplyDto memorialReplyDto)
            throws  MissingReplyContentException,
                    MissingReplyWriterException,
                    MissingReplyPasswordException,
                    InvalidDonateSeqException,
                    MemorialNotFoundException;
    /* 게시글 댓글 수정 */
    void updateReply(Integer donateSeq, Integer replySeq, MemorialReplyDto memorialReplyDto)
            throws  InvalidDonateSeqException,
                    MissingReplyContentException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    InvalidReplySeqException;
    /* 게시글 댓글 삭제 del_flag = 'Y' 설정 */
    void deleteReply(Integer donateSeq, Integer replySeq, MemorialReplyDto memorialReplyDto)
            throws  ReplyPostMismatchException,
                    ReplyIdMismatchException,
                    MissingReplyPasswordException,
                    ReplyPasswordMismatchException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    InvalidReplySeqException,
                    InvalidDonateSeqException;
    /* 게시글 댓글 리스트 조회 */
    List<MemorialReplyDto> findMemorialReplyList(Integer donateSeq)
            throws  MemorialNotFoundException,
                    InvalidDonateSeqException;
}
