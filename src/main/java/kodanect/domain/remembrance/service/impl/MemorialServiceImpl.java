package kodanect.domain.remembrance.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.common.util.CursorFormatter;
import kodanect.domain.heaven.dto.response.MemorialHeavenResponse;
import kodanect.domain.heaven.service.HeavenService;
import kodanect.domain.remembrance.dto.*;
import kodanect.domain.remembrance.dto.common.MemorialNextCursor;
import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.repository.MemorialRepository;
import kodanect.domain.remembrance.service.MemorialCommentService;
import kodanect.domain.remembrance.service.MemorialService;
import kodanect.common.util.EmotionType;
import kodanect.common.util.MemorialFinder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static kodanect.common.util.FormatUtils.formatDate;
import static kodanect.common.util.FormatUtils.formatSearchWord;


/**
 *
 * 기증자 추모관 게시글 서비스 구현체
 * <br>
 * 게시글 조회, 검색, 이모지 카운팅 등의 기능을 제공
 *
 **/
@Service
public class MemorialServiceImpl implements MemorialService {

    /** 스레드 생존 기간 */
    private static final int CACHE_EXPIRE_MINUTES = 10;
    /** 멀티 스레딩 갯수 */
    private static final int CACHE_MAX_SIZE = 100_000;
    /** Cursor 기반 기본 Size */
    private static final int DEFAULT_SIZE = 3;

    private final MemorialRepository memorialRepository;
    private final MemorialCommentService memorialCommentService;
    private final MemorialFinder memorialFinder;
    private final HeavenService heavenService;

    /**
     *
     * 멀티 스레딩 설정
     *
     * <p>CACHE_MAX_SIZE : 멀티 스레딩 갯수</p>
     * <p>CACHE_EXPIRE_MINUTES : 스레드 생존 기간</p>
     *
     * */
    private final Cache<Integer, ReentrantReadWriteLock> lockCache =
            Caffeine.newBuilder().expireAfterAccess(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES).maximumSize(CACHE_MAX_SIZE).build();

    public MemorialServiceImpl(MemorialRepository memorialRepository, MemorialCommentService memorialCommentService, MemorialFinder memorialFinder, HeavenService heavenService){
        this.memorialRepository = memorialRepository;
        this.memorialCommentService = memorialCommentService;
        this.memorialFinder = memorialFinder;
        this.heavenService = heavenService;
    }

    /**
     *
     * 게시글 번호 기반 락 생성 및 반환 메서드
     * 락은 Caffeine 캐시에 저장되며, 최대 10분간 유지됩니다.
     *
     * @param donateSeq 상세 게시글 번호
     * @return 게시글 별로 ReentrantReadWriteLock을 lockCache 설정에 맞게 반환
     *
     * */
    private ReentrantReadWriteLock getLock(Integer donateSeq) {
        return lockCache.get(donateSeq, k -> new ReentrantReadWriteLock());
    }

    /**
     *
     * 기증자 추모관 이모지 카운팅 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param emotion  추가 카운트 될 이모지
     *
     * */
    @Override
    @Transactional
    public void emotionCountUpdate(Integer donateSeq, String emotion)
            throws  InvalidEmotionTypeException,
            MemorialNotFoundException
    {
        /* 게시글 마다 락을 개별 쓰기 락 객체로 관리 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.writeLock().lock();

        try{
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

    /**
     *
     * 기증자 추모관 게시글 검색 조건 조회 메서드
     *
     * @param startDate 시작 일
     * @param endDate 종료 일
     * @param keyWord 검색 문자
     * @param cursor 조회할 댓글 페이지 번호(이 ID보다 작은 번호의 댓글을 조회)
     * @param size 조회할 댓글 페이지 사이즈
     * @return 조건에 맞는 게시글 리스트(최신순)
     *
     * */
    @Override
    public CursorPaginationResponse<MemorialResponse, MemorialNextCursor> getSearchMemorialList(
            String startDate, String endDate, String keyWord, MemorialNextCursor cursor, int size)
    {
        /* 검색 문자 포매팅 */
        keyWord = formatSearchWord(keyWord);

        /* 날짜 포매팅 */
        String startDateStr = formatDate(startDate);
        String endDateStr = formatDate(endDate);
        cursor.setDate(formatDate(cursor.getDate()));

        /* 페이징 포매팅 */
        Pageable pageable = PageRequest.of(0, size+1);

        List<MemorialResponse> memorialResponses =
                memorialRepository.findSearchByCursor(
                        cursor.getDate(),
                        cursor.getCursor(),
                        startDateStr,
                        endDateStr,
                        keyWord,
                        pageable
                );

        long totalCount = memorialRepository.countBySearch(startDateStr, endDateStr, keyWord);

        return CursorFormatter.cursorFormat(memorialResponses, size, totalCount);

    }

    @Override
    public Page<HeavenMemorialResponse> getSearchHeavenMemorialList(
            String startDate, String endDate, String keyWord, Integer page, int size)
    {

        /* 검색 문자 포매팅 */
        keyWord = formatSearchWord(keyWord);

        /* 날짜 포매팅 */
        String startDateStr = formatDate(startDate);
        String endDateStr = formatDate(endDate);

        /* 페이지 포매팅 */
        Pageable pageable = Pageable.ofSize(size).withPage(page - 1);

        return memorialRepository.findSearchByPage(pageable, startDateStr, endDateStr, keyWord);
    }

    /**
     *
     * 기증자 추모관 게시글 조회 메서드
     *
     * @param nextCursor 조회할 댓글 페이지 번호(이 ID보다 작은 번호의 댓글을 조회)
     * @param size 조회할 댓글 페이지 사이즈
     * @return 조건에 맞는 게시글 리스트(최신순)
     *
     * */
    @Override
    public CursorPaginationResponse<MemorialResponse, MemorialNextCursor> getMemorialList(MemorialNextCursor nextCursor, int size) {

        /* 페이징 포매팅 */
        Pageable pageable = PageRequest.of(0, size +1);
        nextCursor.setDate(formatDate(nextCursor.getDate()));

        List<MemorialResponse> memorialResponses = memorialRepository.findByCursor(nextCursor.getCursor(), nextCursor.getDate(), pageable);

        long totalCount = memorialRepository.count();

        return CursorFormatter.cursorFormat(memorialResponses, size, totalCount);
    }

    /**
     *
     * 기증자 추모관 게시글 상세 조회 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @return 조건에 맞는 게시글
     *
     * */
    @Override
    public MemorialDetailResponse getMemorialByDonateSeq(Integer donateSeq)
            throws  MemorialNotFoundException,
                    InvalidContentsException
    {
        /* 게시글 조회 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.readLock().lock();

        try{
            Memorial memorial = memorialFinder.findByIdOrThrow(donateSeq);

            /* 댓글 리스트 모두 조회 */
            List<MemorialCommentResponse> memorialCommentResponses =
                    memorialCommentService.getMemorialCommentList(donateSeq, null, DEFAULT_SIZE + 1);

            /* 댓글 총 갯수 조회 */
            long totalCount = memorialCommentService.getTotalCommentCount(donateSeq);

            /* 댓글 리스트 페이징 포매팅 */
            CursorCommentPaginationResponse<MemorialCommentResponse, Integer> cursoredReplies =
                    CursorFormatter.cursorCommentCountFormat(memorialCommentResponses, DEFAULT_SIZE, totalCount);

            /* 하늘나라 편지 리스트 조회 */
            CursorPaginationResponse<MemorialHeavenResponse, Integer> cursoredLetters =
                    heavenService.getMemorialHeavenList(donateSeq, null, DEFAULT_SIZE);

            /* 기증자 상세 조회 */
            return MemorialDetailResponse.of(
                    memorial,
                    cursoredReplies,
                    cursoredLetters
            );
        }
        finally {
            lock.readLock().unlock();
        }
    }
}

