package kodanect.domain.remembrance.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kodanect.domain.remembrance.dto.*;
import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.repository.MemorialRepository;
import kodanect.domain.remembrance.service.MemorialReplyService;
import kodanect.domain.remembrance.service.MemorialService;
import kodanect.common.util.EmotionType;
import kodanect.common.util.MemorialFinder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static kodanect.common.util.PageFormatter.createPageable;
import static kodanect.common.util.SearchFormatter.formatDate;
import static kodanect.common.util.SearchFormatter.formatSearchWord;
import static kodanect.common.validation.DonateSeqValidator.validateDonateSeq;
import static kodanect.common.validation.PaginationValidator.validatePagination;
import static kodanect.common.validation.SearchValidator.validateSearchDates;


@Service
public class MemorialServiceImpl implements MemorialService {

    private static final int CACHE_EXPIRE_MINUTES = 10;
    private static final int CACHE_MAX_SIZE = 100_000;

    private final MemorialRepository memorialRepository;
    private final MemorialReplyService memorialReplyService;
    private final MemorialFinder memorialFinder;

    private final Cache<Integer, ReentrantReadWriteLock> lockCache =
            Caffeine.newBuilder().expireAfterAccess(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES).maximumSize(CACHE_MAX_SIZE).build();

    public MemorialServiceImpl(MemorialRepository memorialRepository, MemorialReplyService memorialReplyService, MemorialFinder memorialFinder){
        this.memorialRepository = memorialRepository;
        this.memorialReplyService = memorialReplyService;
        this.memorialFinder = memorialFinder;
    }

    private ReentrantReadWriteLock getLock(int donateSeq) {
        return lockCache.get(donateSeq, k -> new ReentrantReadWriteLock());
    }

    @Override
    public void emotionCountUpdate(Integer donateSeq, String emotion)
            throws  InvalidEmotionTypeException,
                    MemorialNotFoundException,
                    InvalidDonateSeqException
    {
        /* 이모지 카운트 수 업데이트 */
        /* 게시글 마다 락을 개별 쓰기 락 객체로 관리 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.writeLock().lock();

        try{
            /* 게시글 ID 검증 */
            validateDonateSeq(donateSeq);

            /* 게시글 조회 */
            Memorial memorial = memorialFinder.findByIdOrThrow(donateSeq);

            /* 이모지 검증 */
            EmotionType emotionType = EmotionType.from(emotion);
            emotionType.apply(memorial);

            memorialRepository.save(memorial);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Page<MemorialListDto> getSearchMemorialList(
            String page, String size, String startDate, String endDate, String searchWord)
            throws  MissingPaginationParameterException,
                    InvalidPaginationRangeException,
                    InvalidPaginationFormatException,
                    MissingSearchDateParameterException,
                    InvalidSearchDateFormatException,
                    InvalidSearchDateRangeException
    {
        /* 게시글 검색 조건 조회 */

        /* 날짜 조건 검증 */
        validateSearchDates(startDate, endDate);

        /* 검색 문자 포매팅 */
        searchWord = formatSearchWord(searchWord);

        /* 날짜 포매팅 */
        String startDateStr = formatDate(startDate);
        String endDateStr = formatDate(endDate);

        /* 페이징 검증 */
        validatePagination(page, size);

        /* 페이징 포매팅 */
        Pageable pageable = createPageable(page, size);

        /* 기증자 추모관 게시글 리스트 날짜 조건 조회  */
        return memorialRepository.findSearchMemorialList(pageable, startDateStr, endDateStr, searchWord);
    }

    @Override
    public Page<MemorialListDto> getMemorialList(String page, String size)
            throws  MissingPaginationParameterException,
                    InvalidPaginationRangeException,
                    InvalidPaginationFormatException
    {
        /* 게시글 리스트 조회 */

        /* 페이징 검증 */
        validatePagination(page, size);

        /* 페이징 포매팅 */
        Pageable pageable = createPageable(page, size);

        /* 기증자 추모관 게시글 리스트 조회 */
        return memorialRepository.findMemorialList(pageable);
    }

    @Override
    public MemorialDetailDto getMemorialByDonateSeq(Integer donateSeq)
            throws  MemorialNotFoundException,
                    InvalidDonateSeqException
    {
        /* 게시글 상세 조회 */

        /* 게시글 ID 검증 */
        validateDonateSeq(donateSeq);

        /* 게시글 조회 */
        Memorial memorial = memorialFinder.findByIdOrThrow(donateSeq);

        /* 댓글 리스트 모두 조회 */
        List<MemorialReplyDto> replyList = memorialReplyService.findMemorialReplyList(donateSeq);

        /* 하늘나라 편지 리스트 조회 예정 */

        /* 기증자 상세 조회 */
        return MemorialDetailDto.of(memorial, replyList);
    }
}

