package kodanect.domain.recipient.repository;

import kodanect.domain.recipient.entity.RecipientEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipientRepository extends JpaRepository<RecipientEntity, Integer>, JpaSpecificationExecutor<RecipientEntity> {
    // 페이징된 게시물 ID 목록에 대해서만 댓글 수 조회
    @Query(value =  "SELECT c.letter_seq, COUNT(c.comment_seq) " +
                    "FROM tb25_431_recipient_letter_comment c " +
                    "WHERE c.letter_seq IN (:letterSeqs) AND c.del_flag = 'N' " + // <-- del_flag 조건 추가
                    "GROUP BY c.letter_seq", nativeQuery = true)
    List<Object[]> countCommentsByLetterSeqs(@Param("letterSeqs") List<Integer> letterSeqs);

    // 단일 게시물 댓글 수 조회 시 (selectRecipient에서 사용)
    @Query(value =  "SELECT COUNT(c.comment_seq) " +
                    "FROM tb25_431_recipient_letter_comment c " +
                    "WHERE c.letter_seq = :letterSeq AND c.del_flag = 'N'", nativeQuery = true) // <-- del_flag 조건 추가
    Integer countCommentsByLetterSeq(@Param("letterSeq") int letterSeq);

    // 게시물 조회 시 연관된 댓글을 LEFT JOIN FETCH로 한 번에 가져오기
    @Query("SELECT r FROM RecipientEntity r LEFT JOIN FETCH r.comments c WHERE r.letterSeq = :letterSeq AND r.delFlag = 'N' AND (c IS NULL OR c.delFlag = 'N')")
    Optional<RecipientEntity> findByIdWithComments(@Param("letterSeq") Integer letterSeq);
}
