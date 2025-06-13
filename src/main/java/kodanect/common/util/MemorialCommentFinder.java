package kodanect.common.util;

import kodanect.domain.remembrance.entity.MemorialComment;
import kodanect.domain.remembrance.exception.MemorialCommentNotFoundException;
import kodanect.domain.remembrance.repository.MemorialCommentRepository;
import org.springframework.stereotype.Component;

/**
 *
 * 댓글 존재 유무 확인 및 MemorialReply 반환 클래스
 *
 * <p>사용법: MemorialReplyFinder.findByIdOrThrow(Integer replySeq)</p>
 *
 * */
@Component
public class MemorialCommentFinder {

    private final MemorialCommentRepository memorialCommentRepository;

    public MemorialCommentFinder(MemorialCommentRepository memorialCommentRepository){
        this.memorialCommentRepository = memorialCommentRepository;
    }

    /**
     *
     * 댓글 존재 유무 확인 및 MemorialReply 반환 메서드
     *
     * @param commentSeq 기증자 추모관 댓글 번호
     * @return 댓글 번호에 매칭되는 댓글(MemorialReply)
     *
     * */
    public MemorialComment findByIdOrThrow(Integer commentSeq) throws MemorialCommentNotFoundException {

        return memorialCommentRepository.findById(commentSeq).orElseThrow(() -> new MemorialCommentNotFoundException(commentSeq));
    }
}
