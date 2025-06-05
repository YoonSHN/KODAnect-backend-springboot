package kodanect.domain.remembrance.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.common.util.CursorFormatter;
import kodanect.domain.remembrance.dto.*;
import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.repository.MemorialRepository;
import kodanect.domain.remembrance.service.MemorialReplyService;
import kodanect.domain.remembrance.service.MemorialService;
import kodanect.common.util.EmotionType;
import kodanect.common.util.MemorialFinder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static kodanect.common.util.FormatUtils.formatDate;
import static kodanect.common.util.FormatUtils.formatSearchWord;
import static kodanect.common.validation.DonateSeqValidator.validateDonateSeq;
import static kodanect.common.validation.PaginationValidator.validatePagination;
import static kodanect.common.validation.SearchValidator.validateSearchDates;


@Service
public class MemorialServiceImpl implements MemorialService {

    /** 스레드 생존 기간 */
    private static final int CACHE_EXPIRE_MINUTES = 10;
    /** 멀티 스레딩 갯수 */
    private static final int CACHE_MAX_SIZE = 100_000;
    /** Cursor 기반 기본 Size */
    private static final int DEFAULT_SIZE = 3;

    private final MemorialRepository memorialRepository;
    private final MemorialReplyService memorialReplyService;
    private final MemorialFinder memorialFinder;

    /**
     *
     * 멀티 스레딩 설정
     *
     * */
    private final Cache<Integer, ReentrantReadWriteLock> lockCache =
            Caffeine.newBuilder().expireAfterAccess(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES).maximumSize(CACHE_MAX_SIZE).build();

    public MemorialServiceImpl(MemorialRepository memorialRepository, MemorialReplyService memorialReplyService, MemorialFinder memorialFinder){
        this.memorialRepository = memorialRepository;
        this.memorialReplyService = memorialReplyService;
        this.memorialFinder = memorialFinder;
    }

    private ReentrantReadWriteLock getLock(Integer donateSeq) {
        return lockCache.get(donateSeq, k -> new ReentrantReadWriteLock());
    }

    /** 이모지 카운팅 */
    @Override
    public void emotionCountUpdate(Integer donateSeq, String emotion)
            throws  InvalidEmotionTypeException,
            MemorialNotFoundException,
            InvalidDonateSeqException
    {
        /* 게시글 마다 락을 개별 쓰기 락 객체로 관리 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.writeLock().lock();

        try{
            /* 게시글 ID 검증 */
            validateDonateSeq(donateSeq);

            /* 게시글 조회 */
            memorialFinder.findByIdOrThrow(donateSeq);

            /* 이모지 검증 */
            EmotionType emotionType = EmotionType.from(emotion);
            emotionType.apply(memorialRepository, donateSeq);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    /** 게시글 검색 조건 조회 */
    @Override
    public CursorPaginationResponse<MemorialResponse, Integer> getSearchMemorialList(
            String startDate, String endDate, String searchWord, Integer cursor, int size)
            throws  InvalidPaginationRangeException,
            MissingSearchDateParameterException,
            InvalidSearchDateFormatException,
            InvalidSearchDateRangeException
    {

        /* 날짜 조건 검증 */
        validateSearchDates(startDate, endDate);

        /* 검색 문자 포매팅 */
        searchWord = formatSearchWord(searchWord);

        /* 날짜 포매팅 */
        String startDateStr = formatDate(startDate);
        String endDateStr = formatDate(endDate);

        /* 페이징 포매팅 */
        Pageable pageable = PageRequest.of(0, size +1);

        List<MemorialResponse> memorialResponses = memorialRepository.findSearchByCursor(cursor, pageable, startDateStr, endDateStr, searchWord);

        return CursorFormatter.cursorFormat(memorialResponses, size);

    }

    /** 게시글 리스트 조회 */
    @Override
    public CursorPaginationResponse<MemorialResponse, Integer> getMemorialList(Integer cursor, int size) throws InvalidPaginationRangeException {

        /* 페이징 검증 */
        validatePagination(cursor, size);

        /* 페이징 포매팅 */
        Pageable pageable = PageRequest.of(0, size +1);

        List<MemorialResponse> memorialResponses = memorialRepository.findByCursor(cursor, pageable);

        return CursorFormatter.cursorFormat(memorialResponses, size);
    }

    /** 게시글 상세 조회 */
    @Override
    public MemorialDetailResponse getMemorialByDonateSeq(Integer donateSeq)
            throws  MemorialNotFoundException,
            InvalidDonateSeqException
    {

        /* 게시글 ID 검증 */
        validateDonateSeq(donateSeq);

        /* 게시글 조회 */
        Memorial memorial = memorialFinder.findByIdOrThrow(donateSeq);

        /* 댓글 리스트 모두 조회 */
        List<MemorialReplyResponse> memorialReplyResponses = memorialReplyService.getMemorialReplyList(donateSeq, null, DEFAULT_SIZE + 1);

        /* 댓글 리스트 페이징 포매팅 */
        CursorReplyPaginationResponse<MemorialReplyResponse, Integer> cursoredReplies = CursorFormatter.cursorReplyFormat(memorialReplyResponses, DEFAULT_SIZE);

        /* 하늘나라 편지 리스트 조회 예정 */

        /* 기증자 상세 조회 */
        return MemorialDetailResponse.of(memorial,
                cursoredReplies.getContent(), cursoredReplies.getReplyNextCursor(), cursoredReplies.isReplyHasNext());
    }
}

