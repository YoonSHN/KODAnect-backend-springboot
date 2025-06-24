package kodanect.domain.heaven.repository;

import kodanect.domain.heaven.dto.response.HeavenCommentResponse;
import kodanect.domain.heaven.entity.HeavenComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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
            WHERE hc.delFlag = 'N'
            AND (:cursor IS NULL OR hc.commentSeq < :cursor)
            AND hc.heaven.letterSeq = :letterSeq
            ORDER BY hc.writeTime DESC
        """
    )
    List<HeavenCommentResponse> findByCursor(Integer letterSeq, Integer cursor, Pageable pageable);

    /**
     * commentSeq를 통한 댓글 조회
     *
     * @param commentSeq
     * @return
     */
    @Query(
            value = """
            SELECT hc
            FROM HeavenComment hc
            WHERE hc.commentSeq = :commentSeq
            AND hc.delFlag = 'N'
        """
    )
    Optional<HeavenComment> findByIdAndDelFlag(@Param("commentSeq") Integer commentSeq);

    /**
     * 댓글 개수 전체 조회
     *
     * @param letterSeq
     * @return
     */
    @Query(
            value = """
            SELECT COUNT(*)
            FROM HeavenComment hc
            WHERE hc.heaven.letterSeq = :letterSeq
            AND hc.delFlag = 'N'
        """
    )
    long countByLetterSeq(@Param("letterSeq") Integer letterSeq);
}
