package kodanect.common.util;

import kodanect.domain.heaven.entity.HeavenComment;
import kodanect.domain.heaven.exception.HeavenCommentInformationMismatchException;
import kodanect.domain.heaven.exception.HeavenCommentNotFoundException;
import kodanect.domain.heaven.repository.HeavenCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class HeavenCommentFinder {

    private final HeavenCommentRepository heavenCommentRepository;

    /* 댓글 존재 여부 및 해당 게시글 댓글 검증 */
    public HeavenComment findByIdAndValidateOwnership(Integer letterSeq, Integer commentSeq) {
        HeavenComment heavenComment = heavenCommentRepository.findByIdAndDelFlag(commentSeq)
                .orElseThrow(() -> new HeavenCommentNotFoundException(commentSeq));

        if (!Objects.equals(heavenComment.getHeaven().getLetterSeq(), letterSeq)) {
            throw new HeavenCommentInformationMismatchException(letterSeq, commentSeq);
        }

        return heavenComment;
    }
}
