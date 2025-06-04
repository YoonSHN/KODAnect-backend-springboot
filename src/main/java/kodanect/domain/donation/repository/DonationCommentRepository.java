package kodanect.domain.donation.repository;

import kodanect.domain.donation.dto.response.DonationStoryCommentDto;
import kodanect.domain.donation.entity.DonationStoryComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DonationCommentRepository extends JpaRepository<DonationStoryComment, Long> {

    /**
     * 특정 게시글(storySeq)에 작성된 댓글 목록을 조회 (페이지네이션 적용 가능)
     * - Pageable을 이용한 더보기 기능 구현 가능
     * - commentSeq 오름차순 정렬
     *
     * @param storySeq 게시글 ID
     * @param pageable 페이징 정보 (offset, limit)
     * @return DonationStoryCommentDto 리스트
     */
    @Query("""
            SELECT new kodanect.domain.donation.dto.response.DonationStoryCommentDto(
                c.commentSeq, c.commentWriter, c.contents, c.writeTime
            )
            FROM DonationStoryComment c
            WHERE c.story.storySeq = :storySeq
            ORDER BY c.commentSeq ASC
            """)
    List<DonationStoryCommentDto> findCommentsByStoryId(@Param("storySeq") Long storySeq, Pageable pageable);

    /**
     * 특정 게시글(storySeq)에 작성된 전체 댓글 개수 반환
     * - 더보기 기능의 hasNext 여부 판단에 활용
     *
     * @param storySeq 게시글 ID
     * @return 댓글 수
     */
    @Query("SELECT COUNT(c) FROM DonationStoryComment c WHERE c.story.storySeq = :storySeq")
    long countCommentsByStoryId(@Param("storySeq") Long storySeq);

}