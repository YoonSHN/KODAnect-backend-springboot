package kodanect.domain.remembrance.repository;

import kodanect.domain.remembrance.dto.HeavenMemorialResponse;
import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.dto.MemorialResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 *
 * 기증자 추모관 게시글에 대한 데이터 베이스 접근 인터페이스
 * <br>
 * 게시글의 조회, 검색, 이모지 카운팅 등에 대한 JPA 기능 제공
 *
 * */
public interface MemorialRepository extends JpaRepository<Memorial, Integer> {


    /**
     *
     * 기증자 추모관 게시글 리스트 날짜 + 문자 조건 페이징 조회
     *
     * @param startDate 시작 일
     * @param endDate 종료 일
     * @param keyWord 검색 문자 (%검색어%)
     * @return 조건에 맞는 게시글 순서 리스트(최신순)
     * */
    @Query(
            value = """
                SELECT m.donate_seq AS donateSeq,
                       CASE
                           WHEN m.anonymity_flag = 'Y'
                           THEN CONCAT(LEFT(m.donor_name, 1), REPEAT('*', CHAR_LENGTH(m.donor_name) - 1))
                           ELSE m.donor_name
                       END AS donorName,
                       DATE_FORMAT(m.donate_date, '%Y-%m-%d') AS donateDate,
                       m.gender_flag AS genderFlag,
                       m.donate_age AS donateAge
                FROM tb25_400_memorial m
                WHERE m.del_flag = 'N'
                        AND m.donate_date BETWEEN :startDate AND :endDate
                        AND m.donor_name LIKE :keyWord
                ORDER BY m.donate_date DESC
        """, countQuery = """
                SELECT COUNT(*)
                FROM tb25_400_memorial m
                WHERE m.del_flag = 'N'
                        AND m.donate_date BETWEEN :startDate AND :endDate
                        AND m.donor_name LIKE :keyWord
        """, nativeQuery = true
    )
    Page<HeavenMemorialResponse> findSearchByPage(
            Pageable pageable,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("keyWord") String keyWord);

    /**
     *
     * 기증자 추모관 게시글 리스트 조회
     *
     * @param cursor 조회할 댓글 페이지 번호(이 ID보다 작은 번호의 댓글을 조회)
     * @param pageable 최대 결과 개수 등 페이징 정보
     * @return 조건에 맞는 게시글 리스트(최신순)
     *
     * */
    @Query(
            value = """
            SELECT  m.donate_seq AS donateSeq,
                    CASE
                        WHEN m.anonymity_flag = 'Y'
                        THEN CONCAT(LEFT(m.donor_name, 1), REPEAT('*', CHAR_LENGTH(m.donor_name) - 1))
                        ELSE m.donor_name
                    END AS donorName,
                    DATE_FORMAT(m.donate_date, '%Y-%m-%d') AS donateDate,
                    m.gender_flag AS genderFlag,
                    m.donate_age AS donateAge,
                    (SELECT COUNT(*) FROM tb25_401_memorial_reply AS r WHERE m.donate_seq = r.donate_seq AND r.del_flag='N') AS commentCount,
                    (SELECT COUNT(*) FROM tb25_410_heaven_letter AS h WHERE m.donate_seq = h.donate_seq AND h.del_flag='N') AS letterCount
            FROM tb25_400_memorial m
            WHERE m.del_flag = 'N'
                    AND (:date IS NULL
                    OR m.donate_date < :date
                    OR (:cursor IS NOT NULL AND m.donate_date = :date AND m.donate_seq < :cursor))
            ORDER BY m.donate_date DESC, m.donate_seq DESC
        """, nativeQuery = true
    )
    List<MemorialResponse> findByCursor(@Param("cursor") Integer cursor, @Param("date") String date, Pageable pageable);

    /**
     *
     * 기증자 추모관 게시글 리스트 날짜 + 문자 조건 순서 조회
     *
     * @param startDate 시작 일
     * @param endDate 종료 일
     * @param keyWord 검색 문자 (%검색어%)
     * @return 조건에 맞는 게시글 순서 리스트(최신순)
     * */
    @Query(
            value = """
             SELECT  m.donate_seq AS donateSeq,
                    CASE
                        WHEN m.anonymity_flag = 'Y'
                        THEN CONCAT(LEFT(m.donor_name, 1), REPEAT('*', CHAR_LENGTH(m.donor_name) - 1))
                        ELSE m.donor_name
                    END AS donorName,
                    DATE_FORMAT(m.donate_date, '%Y-%m-%d') AS donateDate,
                    m.gender_flag AS genderFlag,
                    m.donate_age AS donateAge,
                    (SELECT COUNT(*) FROM tb25_401_memorial_reply AS r WHERE m.donate_seq = r.donate_seq AND r.del_flag='N') AS commentCount,
                    (SELECT COUNT(*) FROM tb25_410_heaven_letter AS h WHERE m.donate_seq = h.donate_seq AND h.del_flag='N') AS letterCount
            FROM tb25_400_memorial m
            WHERE m.del_flag = 'N'
                    AND m.donate_date BETWEEN :startDate AND :endDate
                    AND m.donor_name LIKE CONCAT('%', :keyWord, '%')
                    AND(:date IS NULL
                            OR(m.donate_date < :date
                            OR(m.donate_date = :date AND m.donate_seq < :cursor)))
            ORDER BY m.donate_date DESC, m.donate_seq DESC
        """, nativeQuery = true
    )
    List<MemorialResponse> findSearchByCursor(
            @Param("date") String date,
            @Param("cursor") Integer cursor,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("keyWord") String keyWord,
            Pageable pageable
    );

    /**
     *
     * 기증자 추모관 게시글 리스트 날짜 + 문자 조건 카운팅
     *
     * @param startDate 시작 일
     * @param endDate 종료 일
     * @param keyWord 검색 문자 (%검색어%)
     * @return 조건에 맞는 게시글 리스트(최신순)
     * */
    @Query(
            value = """
            SELECT COUNT(*)
            FROM tb25_400_memorial m
            WHERE m.del_flag = 'N'
                    AND m.donate_date BETWEEN :startDate AND :endDate
                    AND m.donor_name LIKE :keyWord
        """, nativeQuery = true
    )
    long countBySearch(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("keyWord") String keyWord);



    /** 기증자 추모관 이모지 카운팅(Flower) */
    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.flower_count = m.flower_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementFlower(@Param("donateSeq") Integer donateSeq);
    /** 기증자 추모관 이모지 카운팅(Love) */
    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.love_count = m.love_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementLove(@Param("donateSeq") Integer donateSeq);
    /** 기증자 추모관 이모지 카운팅(See) */
    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.see_count = m.see_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementSee(@Param("donateSeq") Integer donateSeq);
    /** 기증자 추모관 이모지 카운팅(Miss) */
    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.miss_count = m.miss_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementMiss(@Param("donateSeq") Integer donateSeq);
    /** 기증자 추모관 이모지 카운팅(Proud) */
    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.proud_count = m.proud_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementProud(@Param("donateSeq") Integer donateSeq);
    /** 기증자 추모관 이모지 카운팅(Hard) */
    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.hard_count = m.hard_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementHard(@Param("donateSeq") Integer donateSeq);
    /** 기증자 추모관 이모지 카운팅(Sad) */
    @Modifying
    @Query(value = "UPDATE tb25_400_memorial m SET m.sad_count = m.sad_count + 1 WHERE m.donate_seq = :donateSeq", nativeQuery = true)
    void incrementSad(@Param("donateSeq") Integer donateSeq);
}
