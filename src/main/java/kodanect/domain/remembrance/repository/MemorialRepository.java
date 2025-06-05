package kodanect.domain.remembrance.repository;

import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.dto.MemorialResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemorialRepository extends JpaRepository<Memorial, Integer> {

    @Query(
        value = """
            SELECT new kodanect.domain.remembrance.dto.MemorialResponse
                    (m.donateSeq, m.donorName, m.anonymityFlag, m.donateDate,m.genderFlag, m.donateAge, COUNT(r))
            FROM Memorial m
            LEFT JOIN MemorialReply r ON m.donateSeq = r.donateSeq
            WHERE m.delFlag = 'N' AND (:cursor IS NULL OR m.donateSeq < :cursor)
            GROUP BY m.donateSeq
            ORDER BY m.writeTime DESC
        """
    )/* 기증자 추모관 게시글 리스트 조회 */
    List<MemorialResponse> findByCursor(@Param("cursor") Integer cursor, Pageable pageable);

    @Query(
        value = """
            SELECT new kodanect.domain.remembrance.dto.MemorialResponse
                    (m.donateSeq, m.donorName, m.anonymityFlag, m.donateDate, m.genderFlag, m.donateAge, COUNT(r))
            FROM Memorial m
            LEFT JOIN MemorialReply r ON m.donateSeq = r.donateSeq
            WHERE m.delFlag = 'N' AND (:cursor IS NULL OR m.donateSeq < :cursor)
                    AND m.donateDate BETWEEN :startDate AND :endDate AND m.donorName LIKE :searchWord
            GROUP BY m.donateSeq
            ORDER BY m.writeTime DESC
        """
    )/* 기증자 추모관 게시글 리스트 날짜 + 문자 조건 조회  */
    List<MemorialResponse> findSearchByCursor(
            @Param("cursor") Integer cursor,
            Pageable pageable,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("searchWord") String searchWord);

    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.flower_count = m.flower_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementFlower(@Param("donateSeq") Integer donateSeq);

    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.love_count = m.love_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementLove(@Param("donateSeq") Integer donateSeq);

    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.see_count = m.see_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementSee(@Param("donateSeq") Integer donateSeq);

    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.miss_count = m.miss_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementMiss(@Param("donateSeq") Integer donateSeq);

    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.proud_count = m.proud_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementProud(@Param("donateSeq") Integer donateSeq);

    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.hard_count = m.hard_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementHard(@Param("donateSeq") Integer donateSeq);

    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.sad_count = m.sad_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementSad(@Param("donateSeq") Integer donateSeq);
}
