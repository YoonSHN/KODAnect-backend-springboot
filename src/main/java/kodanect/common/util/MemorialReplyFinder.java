package kodanect.common.util;

import kodanect.domain.remembrance.entity.MemorialReply;
import kodanect.domain.remembrance.exception.MemorialReplyNotFoundException;
import kodanect.domain.remembrance.repository.MemorialReplyRepository;
import org.springframework.stereotype.Component;

/**
 *
 * 댓글 존재 유무 확인 및 MemorialReply 반환 클래스
 *
 * <p>사용법: MemorialReplyFinder.findByIdOrThrow(Integer replySeq)</p>
 *
 * */
@Component
public class MemorialReplyFinder {

    private final MemorialReplyRepository memorialReplyRepository;

    public MemorialReplyFinder(MemorialReplyRepository memorialReplyRepository){
        this.memorialReplyRepository = memorialReplyRepository;
    }

    /**
     *
     * 댓글 존재 유무 확인 및 MemorialReply 반환 메서드
     *
     * @param replySeq 기증자 추모관 댓글 번호
     * @return 댓글 번호에 매칭되는 댓글(MemorialReply)
     *
     * */
    public MemorialReply findByIdOrThrow(Integer replySeq) throws MemorialReplyNotFoundException {

        return memorialReplyRepository.findById(replySeq).orElseThrow(() -> new MemorialReplyNotFoundException(replySeq));
    }
}
