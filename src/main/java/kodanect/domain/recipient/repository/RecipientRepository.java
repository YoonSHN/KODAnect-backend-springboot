package kodanect.domain.recipient.repository;

import kodanect.domain.recipient.entity.RecipientEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipientRepository extends JpaRepository<RecipientEntity, Integer>, JpaSpecificationExecutor<RecipientEntity> {
    // 페이징된 게시물 ID 목록에 대해서만 댓글 수 조회
    @Query(value =  "SELECT c.letter_seq, COUNT(c.comment_seq) " +
                    "FROM tb25_431_recipient_letter_comment c " +
                    "WHERE c.letter_seq IN :letterSeqs " +
                    "GROUP BY c.letter_seq", nativeQuery = true)
    List<Object[]> countCommentsByLetterSeqs(@Param("letterSeqs") List<Integer> letterSeqs);

    // 단일 게시물 댓글 수 조회 시 (selectRecipient에서 사용)
    @Query(value =  "SELECT COUNT(c.comment_seq) " +
                    "FROM tb25_431_recipient_letter_comment c " +
                    "WHERE c.letter_seq = :letterSeq", nativeQuery = true)
    Integer countCommentsByLetterSeq(@Param("letterSeq") int letterSeq);
}
