package kodanect.domain.remembrance.repository;

import kodanect.domain.remembrance.entity.RecipientEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipientRepository extends JpaRepository<RecipientEntity, Integer>, JpaSpecificationExecutor<RecipientEntity> {
    // 페이징된 게시물 ID 목록에 대해서만 댓글 수 조회
    @Query("SELECT r.letterSeq, COUNT(c.commentSeq) " + // r.letterSeq와 COUNT(c.commentSeq)만 선택
            "FROM RecipientEntity r LEFT JOIN RecipientCommentEntity c ON r.letterSeq = c.letter.letterSeq " +
            "WHERE r.letterSeq IN :letterSeqs " +
            "GROUP BY r.letterSeq")
    List<Object[]> countCommentsByLetterSeqs(@Param("letterSeqs") List<Integer> letterSeqs);

    // 단일 게시물 댓글 수 조회 시 (selectRecipient에서 사용)
    @Query("SELECT COUNT(c.commentSeq) FROM RecipientCommentEntity c WHERE c.letter.letterSeq = :letterSeq AND c.delFlag = FALSE")
    Integer countCommentsByLetterSeq(@Param("letterSeq") int letterSeq);
}
