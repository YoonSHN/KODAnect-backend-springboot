package kodanect.domain.heaven.repository;

import kodanect.domain.heaven.dto.response.HeavenCommentResponse;
import kodanect.domain.heaven.entity.Heaven;
import kodanect.domain.heaven.entity.HeavenComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HeavenCommentRepository extends JpaRepository<HeavenComment, Integer> {

    /**
     * 댓글 전체 조회 (페이징)
     *
     * @param letterSeq
     * @param cursor
     * @param pageable
     * @return
     */
    @Query(
            value = """
            SELECT new kodanect.domain.heaven.dto.response.HeavenCommentResponse
                    (hc.commentSeq, hc.commentWriter, hc.contents, hc.writeTime)
            FROM HeavenComment hc
            WHERE (:cursor IS NULL OR hc.commentSeq < :cursor)
            AND hc.heaven.letterSeq = :letterSeq
            ORDER BY hc.commentSeq DESC
        """
    )
    List<HeavenCommentResponse> findByCursor(Integer letterSeq, Integer cursor, Pageable pageable);

    /** 댓글 개수 조회 */
    int countByHeaven(Heaven heaven);
}
