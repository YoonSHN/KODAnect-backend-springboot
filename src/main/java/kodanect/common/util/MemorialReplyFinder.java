package kodanect.common.util;

import kodanect.domain.remembrance.entity.MemorialReply;
import kodanect.domain.remembrance.exception.MemorialReplyNotFoundException;
import kodanect.domain.remembrance.repository.MemorialReplyRepository;
import org.springframework.stereotype.Component;

@Component
public class MemorialReplyFinder {

    private final MemorialReplyRepository memorialReplyRepository;

    public MemorialReplyFinder(MemorialReplyRepository memorialReplyRepository){
        this.memorialReplyRepository = memorialReplyRepository;
    }

    public MemorialReply findByIdOrThrow(Integer replySeq) throws MemorialReplyNotFoundException {
        /* 댓글 조회 */
        return memorialReplyRepository.findById(replySeq).orElseThrow(MemorialReplyNotFoundException::new);
    }
}
