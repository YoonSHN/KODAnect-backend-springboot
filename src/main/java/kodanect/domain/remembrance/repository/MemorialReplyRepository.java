package kodanect.domain.remembrance.repository;

import kodanect.domain.remembrance.entity.MemorialReply;
import kodanect.domain.remembrance.dto.MemorialReplyResponse;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemorialReplyRepository extends JpaRepository<MemorialReply, Integer> {

    @Query(
        value = """
            SELECT new kodanect.domain.remembrance.dto.MemorialReplyResponse
                    (r.replySeq, r.replyWriter, r.replyContents, r.replyWriteTime)
            FROM MemorialReply r
            WHERE r.donateSeq = :donateSeq AND r.delFlag = 'N'
            ORDER BY r.replyWriteTime DESC
        """
    )/* 댓글 리스트 전부 조회 */
    List<MemorialReplyResponse> findMemorialReplyList(@Param("donateSeq") int donateSeq);

    @Modifying(clearAutomatically = true)
    @Query(
        value = """
            UPDATE MemorialReply r
            SET r.replyContents = :contents
            WHERE r.replySeq = :replySeq AND r.delFlag = 'N'
        """
    )/* 댓글 수정 */
    void updateReplyContents(@Param("replySeq") Integer replySeq, @Param("contents") String contents);
}
