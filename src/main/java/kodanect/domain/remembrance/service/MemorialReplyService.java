package kodanect.domain.remembrance.service;

import kodanect.domain.remembrance.dto.MemorialReplyDeleteRequest;
import kodanect.domain.remembrance.dto.MemorialReplyResponse;
import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.dto.MemorialReplyUpdateRequest;
import kodanect.domain.remembrance.exception.*;

import java.util.List;

public interface MemorialReplyService {
    /* 게시글 댓글 작성 */
    void createReply(Integer donateSeq, MemorialReplyCreateRequest memorialReplyCreateRequest)
            throws  MissingReplyContentException,
                    MissingReplyWriterException,
                    MissingReplyPasswordException,
                    InvalidDonateSeqException,
                    MemorialNotFoundException;
    /* 게시글 댓글 수정 */
    void updateReply(Integer donateSeq, Integer replySeq, MemorialReplyUpdateRequest memorialReplyUpdateRequest)
            throws  InvalidDonateSeqException,
                    MissingReplyContentException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    InvalidReplySeqException;
    /* 게시글 댓글 삭제 del_flag = 'Y' 설정 */
    void deleteReply(Integer donateSeq, Integer replySeq, MemorialReplyDeleteRequest memorialReplyDeleteRequest)
            throws  ReplyPostMismatchException,
                    ReplyIdMismatchException,
                    MissingReplyPasswordException,
                    ReplyPasswordMismatchException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    InvalidReplySeqException,
                    InvalidDonateSeqException;
    /* 게시글 댓글 리스트 조회 */
    List<MemorialReplyResponse> findMemorialReplyList(Integer donateSeq)
            throws  MemorialNotFoundException,
                    InvalidDonateSeqException;
}
