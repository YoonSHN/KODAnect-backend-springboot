package kodanect.domain.remembrance.repository;

import kodanect.domain.remembrance.entity.MemorialReply;
import kodanect.domain.remembrance.dto.MemorialReplyResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemorialReplyRepository extends JpaRepository<MemorialReply, Integer> {

    @Modifying(clearAutomatically = true)
    @Query(
        value = """
            UPDATE MemorialReply r
            SET r.replyContents = :contents
            WHERE r.replySeq = :replySeq AND r.delFlag = 'N'
        """
    )/* 댓글 수정 */
    void updateReplyContents(@Param("replySeq") Integer replySeq, @Param("contents") String contents);

    @Query(
        value = """
            SELECT new kodanect.domain.remembrance.dto.MemorialReplyResponse
                    (r.replySeq, r.replyWriter, r.replyContents, r.replyWriteTime)
            FROM MemorialReply r
            WHERE r.donateSeq = :donateSeq AND r.delFlag = 'N' AND (:cursor IS NULL OR r.replySeq < :cursor)
            ORDER BY r.replySeq DESC
        """
    )/* 댓글 리스트 조회 Cursor */
    List<MemorialReplyResponse> findByCursor(@Param("donateSeq") Integer donateSeq, @Param("cursor") Integer cursor, Pageable pageable);
}
