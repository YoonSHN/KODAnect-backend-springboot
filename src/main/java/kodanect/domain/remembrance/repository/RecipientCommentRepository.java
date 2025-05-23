package kodanect.domain.remembrance.repository;

import kodanect.domain.remembrance.entity.RecipientCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipientCommentRepository extends JpaRepository<RecipientCommentEntity, Integer>, JpaSpecificationExecutor<RecipientCommentEntity> {
    // 특정 letterSeq에 해당하는 댓글 목록을 조회 (삭제되지 않은 댓글만)
    List<RecipientCommentEntity> findByLetter_LetterSeqAndDelFlagFalseOrderByWriteTimeAsc(int letterSeq);

    // 특정 commentSeq와 delFlag=false인 댓글 조회
    Optional<RecipientCommentEntity> findByCommentSeqAndDelFlagFalse(int commentSeq);
}