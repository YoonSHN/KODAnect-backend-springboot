package kodanect.domain.remembrance.repository;

import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.dto.MemorialListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemorialRepository extends JpaRepository<Memorial, Integer> {

    @Query(
        value = """
            SELECT new kodanect.domain.remembrance.dto.MemorialListDto(
                 m.donateSeq, m.donorName, m.anonymityFlag,
                 m.donateDate, m.genderFlag, m.donateAge, m.delFlag,
                 COUNT(r)
            )
            FROM tb25_400_memorial m
            LEFT JOIN tb25_401_memorial_reply r ON m.donateSeq = r.donateSeq
            WHERE m.delFlag = 'N'
            GROUP BY m.donateSeq
            ORDER BY m.writeTime DESC
        """,
        countQuery = """
            SELECT COUNT(DISTINCT m.donateSeq)
            FROM tb25_400_memorial m
            WHERE m.delFlag = 'N'
        """
    )/* 기증자 추모관 게시글 리스트 조회 */
    Page<MemorialListDto> findMemorialList(Pageable pageable);

    @Query(
        value = """
            SELECT new kodanect.domain.remembrance.dto.MemorialListDto(
             m.donateSeq, m.donorName, m.anonymityFlag,
             m.donateDate, m.genderFlag, m.donateAge, m.delFlag,
             COUNT(r)
            )
            FROM tb25_400_memorial m
            LEFT JOIN tb25_401_memorial_reply r ON m.donateSeq = r.donateSeq
            WHERE m.donorName LIKE :searchWord AND m.donateDate BETWEEN :startDate AND :endDate AND m.delFlag = 'N'
            GROUP BY m.donateSeq
            ORDER BY m.writeTime DESC
        """,
        countQuery = """
            SELECT COUNT(DISTINCT m.donateSeq)
            FROM tb25_400_memorial m
            WHERE m.donorName LIKE :searchWord AND m.donateDate BETWEEN :startDate AND :endDate AND m.delFlag = 'N'
        """
    )/* 기증자 추모관 게시글 리스트 날짜 + 문자 조건 조회  */
    Page<MemorialListDto> findSearchMemorialList(
            Pageable pageable,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("searchWord") String searchWord);
}
