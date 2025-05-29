package kodanect.domain.recipient.repository;

import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipientCommentRepository extends JpaRepository<RecipientCommentEntity, Integer>, JpaSpecificationExecutor<RecipientCommentEntity> {
    // 특정 letterSeq에 해당하는 댓글 목록을 조회 (삭제되지 않은 댓글만, 작성 시간 오름차순)
    List<RecipientCommentEntity> findByLetterSeqAndDelFlagOrderByWriteTimeAsc(RecipientEntity letterSeq, String delFlag);

    // 특정 commentSeq와 delFlag="N"인 댓글 조회
    Optional<RecipientCommentEntity> findByCommentSeqAndDelFlag(Integer commentSeq, String delFlag);

    // 특정 게시물 ID 목록에 대한 댓글 개수를 조회
    @Query("SELECT rc.letterSeq.letterSeq, COUNT(rc) FROM RecipientCommentEntity rc " +
            "WHERE rc.letterSeq.letterSeq IN :letterSeqs AND rc.delFlag = 'N' " +
            "GROUP BY rc.letterSeq.letterSeq")
    List<Object[]> countCommentsByLetterSeqs(@Param("letterSeqs") List<Integer> letterSeqs);

    // 페이징된 댓글 목록 조회를 위한 메서드 _ letterSeq와 delFlag로 댓글을 조회하고, Pageable을 적용하여 페이징 처리
    Page<RecipientCommentEntity> findByLetterSeqAndDelFlag(RecipientEntity letterSeq, String delFlag, Pageable pageable);

    // lastCommentId 이후의 댓글을 조회하는 JPQL (더 효율적일 수 있음)
    @Query("SELECT rc FROM RecipientCommentEntity rc " +
            "WHERE rc.letterSeq = :letterSeq AND rc.delFlag = 'N' " +
            "AND (:lastCommentId IS NULL OR :lastCommentId = 0 OR rc.commentSeq > :lastCommentId) " +
            "ORDER BY rc.writeTime ASC, rc.commentSeq ASC")
    List<RecipientCommentEntity> findPaginatedComments(
            @Param("letterSeq") RecipientEntity letterSeq,
            @Param("lastCommentId") Integer lastCommentId,
            Pageable pageable); // Pageable을 받아서 LIMIT/OFFSET 처리
}