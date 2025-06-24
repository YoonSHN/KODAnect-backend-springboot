package kodanect.domain.heaven.repository;

import kodanect.domain.heaven.dto.HeavenDto;
import kodanect.domain.heaven.dto.response.HeavenResponse;
import kodanect.domain.heaven.dto.response.MemorialHeavenResponse;
import kodanect.domain.heaven.entity.Heaven;
import kodanect.domain.remembrance.entity.Memorial;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface HeavenRepository extends JpaRepository<Heaven, Integer> {

    /**
     * 게시물 전체 조회 (페이징)
     *
     * @param cursor
     * @param pageable
     * @return
     */
    @Query(
            value = """
            SELECT new kodanect.domain.heaven.dto.response.HeavenResponse
                    (h.letterSeq, h.letterTitle,
                     CASE
                             WHEN(h.memorial.anonymityFlag = 'Y') THEN
                                     CONCAT(SUBSTRING(h.donorName, 1, 1), REPEAT('*', CHAR_LENGTH(h.donorName) - 1))
                             ELSE h.donorName
                     END AS donorName,
                     h.memorial.anonymityFlag AS memorialAnonymityFlag,
                     CASE
                             WHEN(h.anonymityFlag = 'Y') THEN
                                     CONCAT(SUBSTRING(h.letterWriter, 1, 1), REPEAT('*', CHAR_LENGTH(h.letterWriter) - 1))
                             ELSE h.letterWriter
                     END AS letterWriter,
                     h.anonymityFlag AS heavenAnonymityFlag, h.readCount, h.writeTime)
            FROM Heaven h
            WHERE h.delFlag = 'N'
            AND (:cursor IS NULL OR h.letterSeq < :cursor)
            ORDER BY h.writeTime DESC
        """
    )
    List<HeavenResponse> findByCursor(@Param("cursor") Integer cursor, Pageable pageable);

    /**
     * 전체(제목 + 내용)을 통한 게시물 전체 조회 (페이징)
     *
     * @param cursor
     * @param keyWord
     * @param pageable
     * @return
     */
    @Query(
            value = """
            SELECT new kodanect.domain.heaven.dto.response.HeavenResponse
                    (h.letterSeq, h.letterTitle,
                     CASE
                             WHEN(h.memorial.anonymityFlag = 'Y') THEN
                                     CONCAT(SUBSTRING(h.donorName, 1, 1), REPEAT('*', CHAR_LENGTH(h.donorName) - 1))
                             ELSE h.donorName
                     END AS donorName,
                     h.memorial.anonymityFlag AS memorialAnonymityFlag,
                     CASE
                             WHEN(h.anonymityFlag = 'Y') THEN
                                     CONCAT(SUBSTRING(h.letterWriter, 1, 1), REPEAT('*', CHAR_LENGTH(h.letterWriter) - 1))
                             ELSE h.letterWriter
                     END AS letterWriter,
                     h.anonymityFlag AS heavenAnonymityFlag, h.readCount, h.writeTime)
            FROM Heaven h
            WHERE h.delFlag = 'N'
            AND (:cursor IS NULL OR h.letterSeq < :cursor)
            AND (h.letterTitle LIKE %:keyWord% OR h.letterContents LIKE %:keyWord%)
            ORDER BY h.writeTime DESC
        """
    )
    List<HeavenResponse> findByTitleOrContentsContaining(@Param("keyWord") String keyWord, @Param("cursor") Integer cursor, Pageable pageable);

    /**
     * 제목을 통한 게시물 전체 조회 (페이징)
     *
     * @param keyWord
     * @param cursor
     * @param pageable
     * @return
     */
    @Query(
            value = """
            SELECT new kodanect.domain.heaven.dto.response.HeavenResponse
                    (h.letterSeq, h.letterTitle,
                     CASE
                             WHEN(h.memorial.anonymityFlag = 'Y') THEN
                                     CONCAT(SUBSTRING(h.donorName, 1, 1), REPEAT('*', CHAR_LENGTH(h.donorName) - 1))
                             ELSE h.donorName
                     END AS donorName,
                     h.memorial.anonymityFlag AS memorialAnonymityFlag,
                     CASE
                             WHEN(h.anonymityFlag = 'Y') THEN
                                     CONCAT(SUBSTRING(h.letterWriter, 1, 1), REPEAT('*', CHAR_LENGTH(h.letterWriter) - 1))
                             ELSE h.letterWriter
                     END AS letterWriter,
                     h.anonymityFlag AS heavenAnonymityFlag, h.readCount, h.writeTime)
            FROM Heaven h
            WHERE h.delFlag = 'N'
            AND (:cursor IS NULL OR h.letterSeq < :cursor)
            AND h.letterTitle LIKE %:keyWord%
            ORDER BY h.writeTime DESC
        """
    )
    List<HeavenResponse> findByTitleContaining(@Param("keyWord") String keyWord, @Param("cursor") Integer cursor, Pageable pageable);

    /**
     * 내용을 통한 게시물 전체 조회 (페이징)
     *
     * @param keyWord
     * @param cursor
     * @param pageable
     * @return
     */
    @Query(
            value = """
            SELECT new kodanect.domain.heaven.dto.response.HeavenResponse
                    (h.letterSeq, h.letterTitle,
                     CASE
                             WHEN(h.memorial.anonymityFlag = 'Y') THEN
                                     CONCAT(SUBSTRING(h.donorName, 1, 1), REPEAT('*', CHAR_LENGTH(h.donorName) - 1))
                             ELSE h.donorName
                     END AS donorName,
                     h.memorial.anonymityFlag AS memorialAnonymityFlag,
                     CASE
                             WHEN(h.anonymityFlag = 'Y') THEN
                                     CONCAT(SUBSTRING(h.letterWriter, 1, 1), REPEAT('*', CHAR_LENGTH(h.letterWriter) - 1))
                             ELSE h.letterWriter
                     END AS letterWriter,
                     h.anonymityFlag AS heavenAnonymityFlag, h.readCount, h.writeTime)
            FROM Heaven h
            WHERE h.delFlag = 'N'
            AND (:cursor IS NULL OR h.letterSeq < :cursor)
            AND h.letterContents LIKE %:keyWord%
            ORDER BY h.writeTime DESC
        """
    )
    List<HeavenResponse> findByContentsContaining(@Param("keyWord") String keyWord, @Param("cursor") Integer cursor, Pageable pageable);

    /**
     * 기증자 추모관 상세 조회 시 하늘나라 편지 리스트 조회
     *
     * @param memorial
     * @param cursor
     * @param pageable
     * @return
     */
    @Query(
            value = """
            SELECT new kodanect.domain.heaven.dto.response.MemorialHeavenResponse
            (h.letterSeq, h.letterTitle, h.readCount, h.writeTime)
            FROM Heaven h
            WHERE h.delFlag = 'N'
            AND (:cursor IS NULL OR h.letterSeq < :cursor)
            AND h.memorial = :memorial
            ORDER BY h.writeTime DESC
        """
    )
    List<MemorialHeavenResponse> findMemorialHeavenResponseById(@Param("memorial") Memorial memorial, @Param("cursor") Integer cursor, Pageable pageable);

    /**
     * 상세 조회 시 게시물 조회
     *
     * @param letterSeq
     * @return
     */
    @Query(
            value = """
            SELECT new kodanect.domain.heaven.dto.HeavenDto
                    (h.letterSeq, m.donateSeq, h.letterTitle,
                    CASE
                            WHEN(m.anonymityFlag = 'Y') THEN
                                    CONCAT(SUBSTRING(h.donorName, 1, 1), REPEAT('*', CHAR_LENGTH(h.donorName) - 1))
                            ELSE h.donorName
                    END AS donorName,
                    m.anonymityFlag AS memorialAnonymityFlag,
                    CASE
                            WHEN(h.anonymityFlag = 'Y') THEN
                                    CONCAT(SUBSTRING(h.letterWriter, 1, 1), REPEAT('*', CHAR_LENGTH(h.letterWriter) - 1))
                            ELSE h.letterWriter
                    END AS letterWriter,
                    h.anonymityFlag AS heavenAnonymityFlag,
                    h.readCount, h.letterContents, h.fileName, h.orgFileName, h.writeTime)
            FROM Heaven h
            LEFT JOIN h.memorial m
            WHERE h.delFlag = 'N'
            AND h.letterSeq = :letterSeq
        """
    )
    HeavenDto findAnonymizedById(@Param("letterSeq") Integer letterSeq);

    /**
     * letterSeq를 통한 게시물 조회
     *
     * @param letterSeq
     * @return
     */
    @Query(
            value = """
            SELECT h
            FROM Heaven h
            WHERE h.letterSeq = :letterSeq
            AND h.delFlag = 'N'
        """
    )
    Optional<Heaven> findByIdAndDelFlag(@Param("letterSeq") Integer letterSeq);

    /**
     * 전체(제목 + 내용)을 통한 게시물 개수 조회
     *
     * @param keyWord
     * @return
     */
    @Query(
            value = """
            SELECT COUNT(h)
            FROM Heaven h
            WHERE h.delFlag = 'N'
            AND h.letterTitle LIKE %:keyWord% OR h.letterContents LIKE %:keyWord%
        """
    )
    long countByTitleOrContentsContaining(@Param("keyWord") String keyWord);

    /**
     * 제목을 통한 게시물 개수 조회
     *
     * @param keyWord
     * @return
     */
    @Query(
            value = """
            SELECT COUNT(h)
            FROM Heaven h
            WHERE h.delFlag = 'N'
            AND h.letterTitle LIKE %:keyWord%
        """
    )
    long countByTitleContaining(@Param("keyWord") String keyWord);

    /**
     * 내용을 통한 게시물 개수 조회
     *
     * @param keyWord
     * @return
     */
    @Query(
            value = """
            SELECT COUNT(h)
            FROM Heaven h
            WHERE h.delFlag = 'N'
            AND h.letterContents LIKE %:keyWord%
        """
    )
    long countByContentsContaining(@Param("keyWord") String keyWord);

    /**
     * 게시물 개수 전체 조회
     *
     * @return
     */
    @Query(
            value = """
            SELECT COUNT(*)
            FROM Heaven h
            WHERE h.delFlag = 'N'
        """
    )
    long countByDelFlag();

    /**
     * donateSeq를 통한 게시물 개수 조회
     *
     * @param memorial
     * @return
     */
    @Query(
            value = """
            SELECT COUNT(*)
            FROM Heaven h
            WHERE h.memorial = :memorial
            AND h.delFlag = 'N'
        """
    )
    long countByMemorial(@Param("memorial") Memorial memorial);

    /**
     * 조회수 증가
     *
     * @param letterSeq
     */
    @Transactional
    @Modifying
    @Query(
            value = """
            UPDATE Heaven h
            SET h.readCount = h.readCount + 1
            WHERE h.letterSeq = :letterSeq
            AND h.delFlag = 'N'
        """
    )
    void updateReadCount(@Param("letterSeq") Integer letterSeq);
}
