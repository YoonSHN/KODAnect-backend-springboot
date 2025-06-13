package kodanect.domain.remembrance.repository;

import kodanect.domain.remembrance.entity.MemorialComment;
import kodanect.domain.remembrance.dto.MemorialCommentResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 *
 * 기증자 추모관 댓글에 대한 데이터베이스 접근 인터페이스
 * <br>
 * 댓글의 생성, 수정, 삭제, 조회 등의 JPA 기반 기능 제공
 *
 **/
public interface MemorialCommentRepository extends JpaRepository<MemorialComment, Integer> {

    /**
     *
     * 기증자 추모관 댓글 수정 메서드
     *
     * @param commentSeq 댓글 번호
     * @param contents 댓글 내용
     *
     **/
    @Modifying(clearAutomatically = true)
    @Query(
            value = """
            UPDATE MemorialComment r
            SET r.contents = :contents,
                r.commentWriter = :writer
            WHERE r.commentSeq = :commentSeq
                    AND r.delFlag = 'N'
        """
    )
    void updateCommentContents(@Param("commentSeq") Integer commentSeq, @Param("contents") String contents, @Param("writer") String writer);

    /**
     *
     * 기증자 추모관 댓글 리스트 조회 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param cursor 조회할 댓글 페이지 번호(이 ID보다 작은 번호의 댓글을 조회)
     * @param pageable 최대 결과 개수 등 페이징 정보
     * @return 조건에 맞는 댓글 리스트(최신순)
     *
     **/
    @Query(
            value = """
            SELECT new kodanect.domain.remembrance.dto.MemorialCommentResponse
                    (r.commentSeq, r.commentWriter, r.contents, r.writeTime)
            FROM MemorialComment r
            WHERE r.donateSeq = :donateSeq
                    AND r.delFlag = 'N'
                    AND (:cursor IS NULL OR r.commentSeq < :cursor)
            ORDER BY r.commentSeq DESC
        """
    )
    List<MemorialCommentResponse> findByCursor(@Param("donateSeq") Integer donateSeq, @Param("cursor") Integer cursor, Pageable pageable);

    /** 게시물 번호 기준 총 댓글 수 */
    @Query(
            value = """
            SELECT COUNT(*)
            FROM tb25_401_memorial_reply r
            WHERE r.donate_seq = :donateSeq
                    AND r.del_flag = 'N'
        """, nativeQuery = true
    )
    long countByDonateSeq(@Param("donateSeq") Integer donateSeq);
}
