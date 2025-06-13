package kodanect.domain.donation.repository;

import kodanect.domain.donation.dto.response.DonationStoryListDto;
import kodanect.domain.donation.entity.DonationStory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<DonationStory, Long> {

    /**
     * 게시글 목록을 wㅗ회 (더보기 방식에 사용)
     */

    @Query("""
        SELECT new kodanect.domain.donation.dto.response.DonationStoryListDto(
            d.storySeq, d.storyTitle, d.storyWriter, d.readCount, d.writeTime)
        FROM DonationStory d
        WHERE (:cursor IS NULL OR d.storySeq < :cursor)
        ORDER BY d.storySeq DESC
        """)
        List<DonationStoryListDto> findByCursor(@Param("cursor") Long cursor, Pageable pageable);


    /**
     * 게시글 상세 조회 시 댓글도 함께 가져오기 (댓글 정렬: 오름차순)
     */
    @Query("""
        SELECT s FROM DonationStory s
        WHERE s.storySeq = :storySeq
        """)
    Optional<DonationStory> findStoryOnlyById(@Param("storySeq") Long storySeq);


    @Query("""
        SELECT new kodanect.domain.donation.dto.response.DonationStoryListDto(
            d.storySeq, d.storyTitle, d.storyWriter, d.readCount, d.writeTime)
        FROM DonationStory d
        WHERE (d.storyTitle LIKE %:keyword%) AND (:cursor IS NULL OR d.storySeq < :cursor)
        ORDER BY d.storySeq DESC
        """)
        List<DonationStoryListDto> findByTitleCursor(@Param("keyword") String keyword,
                                                 @Param("cursor") Long cursor,
                                                 Pageable pageable);

    @Query("""
        SELECT new kodanect.domain.donation.dto.response.DonationStoryListDto(
            d.storySeq, d.storyTitle, d.storyWriter, d.readCount, d.writeTime)
        FROM DonationStory d
        WHERE (d.storyContents LIKE %:keyword%) AND (:cursor IS NULL OR d.storySeq < :cursor)
        ORDER BY d.storySeq DESC
        """)
        List<DonationStoryListDto> findByContentsCursor(@Param("keyword") String keyword,
                                                    @Param("cursor") Long cursor,
                                                    Pageable pageable);

    @Query("""
        SELECT new kodanect.domain.donation.dto.response.DonationStoryListDto(
            d.storySeq, d.storyTitle, d.storyWriter, d.readCount, d.writeTime)
        FROM DonationStory d
        WHERE (d.storyTitle LIKE %:keyword% OR d.storyContents LIKE %:keyword%) 
              AND (:cursor IS NULL OR d.storySeq < :cursor)
        ORDER BY d.storySeq DESC
        """)
        List<DonationStoryListDto> findByTitleOrContentsCursor(@Param("keyword") String keyword,
                                                           @Param("cursor") Long cursor,
                                                           Pageable pageable);

    @Query(value =  "SELECT COUNT(*) FROM tb25_420_donation_story", nativeQuery=true)
    long countAll();

    @Query(value = "SELECT COUNT(*) FROM tb25_420_donation_story WHERE story_title LIKE CONCAT('%', :keyword, '%')", nativeQuery = true)
    long countByTitle(@Param("keyword") String keyword);

    @Query(value = "SELECT COUNT(*) FROM tb25_420_donation_story WHERE story_contents LIKE CONCAT('%', :keyword, '%')", nativeQuery = true)
    long countByContents(@Param("keyword") String keyword);

    @Query(value = "SELECT COUNT(*) FROM tb25_420_donation_story WHERE story_title LIKE CONCAT('%',:keyword, '%') OR story_contents LIKE CONCAT('%', :keyword, '%')", nativeQuery = true)
    long countByTitleAndContents(@Param("keyword") String keyword);

}