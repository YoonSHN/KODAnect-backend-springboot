package kodanect.domain.remembrance.service;

import kodanect.domain.remembrance.dto.MemorialReplyDto;

import java.util.List;

public interface MemorialReplyService {
    /* 게시글 댓글 작성 */
    void createReply(Integer donateSeq, MemorialReplyDto memorialReplyDto) throws Exception;
    /* 게시글 댓글 수정 */
    void updateReply(Integer donateSeq, Integer replySeq, MemorialReplyDto memorialReplyDto) throws Exception;
    /* 게시글 댓글 삭제 del_flag = 'Y' 설정 */
    void deleteReply(Integer donateSeq, Integer replySeq, MemorialReplyDto memorialReplyDto) throws Exception;
    /* 게시글 댓글 리스트 조회 */
    List<MemorialReplyDto> findMemorialReplyList(Integer donateSeq) throws Exception;
}
