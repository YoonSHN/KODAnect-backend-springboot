package kodanect.domain.donation.repository;

import kodanect.domain.donation.dto.response.DonationStoryListDto;
import kodanect.domain.donation.entity.DonationStory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<DonationStory, Long> {

    /**
     * 게시글 목록을 Offset 기반으로 슬라이스 조회 (더보기 방식에 사용)
     */
    @Query("""
            SELECT new kodanect.domain.donation.dto.response.DonationStoryListDto(
                d.storySeq, d.storyTitle, d.storyWriter, d.readCount, d.writeTime)
            FROM DonationStory d ORDER BY d.storySeq DESC
            """)
    List<DonationStoryListDto> findSliceDonationStoriesWithOffset(Pageable pageable);

    /**
     * 키워드 검색을 통한 더보기 방식 슬라이스 조회 (제목+내용 기준)
     */
    @Query("""
            SELECT new kodanect.domain.donation.dto.response.DonationStoryListDto(
                d.storySeq, d.storyTitle, d.storyWriter, d.readCount, d.writeTime)
            FROM DonationStory d
            WHERE d.storyTitle LIKE %:keyword% OR d.storyContents LIKE %:keyword%
            ORDER BY d.storySeq DESC
            """)
    List<DonationStoryListDto> findSliceDonationStoriesWithOffsetByKeyword(Pageable pageable, @Param("keyword") String keyword);

    /**
     * 게시글 상세 조회 시 댓글도 함께 가져오기 (댓글 정렬: 오름차순)
     */
    @Query("""
            SELECT s
            FROM DonationStory s
            LEFT JOIN FETCH s.comments c
            WHERE s.storySeq = :storySeq
            ORDER BY c.commentSeq ASC
            """)
    Optional<DonationStory> findWithCommentsById(@Param("storySeq") Long storySeq);

    /**
     * 제목에 특정 키워드가 포함된 게시글들을 Page로 조회
     */
    @Query("""
            SELECT new kodanect.domain.donation.dto.response.DonationStoryListDto(
                u.storySeq, u.storyTitle, u.storyWriter, u.readCount, u.writeTime)
            FROM DonationStory u WHERE u.storyTitle LIKE %:keyword%
            ORDER BY u.storySeq DESC
            """)
    Slice<DonationStoryListDto> findByTitleContaining(Pageable pageable, @Param("keyword") String keyword);

    /**
     * 내용에 특정 키워드가 포함된 게시글들을 Page로 조회
     */
    @Query("""
            SELECT new kodanect.domain.donation.dto.response.DonationStoryListDto(
                u.storySeq, u.storyTitle, u.storyWriter, u.readCount, u.writeTime)
            FROM DonationStory u WHERE u.storyContents LIKE %:keyword%
            ORDER BY u.storySeq DESC
            """)
    Slice<DonationStoryListDto> findByContentsContaining(Pageable pageable, @Param("keyword") String keyword);

    /**
     * 제목 또는 내용에 특정 키워드가 포함된 게시글들을 Page로 조회
     */
    @Query("""
            SELECT new kodanect.domain.donation.dto.response.DonationStoryListDto(
                u.storySeq, u.storyTitle, u.storyWriter, u.readCount, u.writeTime)
            FROM DonationStory u
            WHERE u.storyContents LIKE %:keyword% OR u.storyTitle LIKE %:keyword%
            ORDER BY u.storySeq DESC
            """)
    Slice<DonationStoryListDto> findByTitleOrContentsContaining(Pageable pageable, @Param("keyword") String keyword);

    /**
     * 제목 검색 시 일치하는 게시글 개수 반환 (hasNext 판단용)
     */
    @Query("SELECT COUNT(d) FROM DonationStory d WHERE d.storyTitle LIKE %:keyword%")
    long countByTitleContaining(@Param("keyword") String keyword);

    /**
     * 내용 검색 시 일치하는 게시글 개수 반환 (hasNext 판단용)
     */
    @Query("SELECT COUNT(d) FROM DonationStory d WHERE d.storyContents LIKE %:keyword%")
    long countByContentsContaining(@Param("keyword") String keyword);

    /**
     * 제목 또는 내용 검색 시 일치하는 게시글 개수 반환 (hasNext 판단용)
     */
    @Query("SELECT COUNT(d) FROM DonationStory d WHERE d.storyTitle LIKE %:keyword% OR d.storyContents LIKE %:keyword%")
    long countByTitleOrContentsContaining(@Param("keyword") String keyword);
}