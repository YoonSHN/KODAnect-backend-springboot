package kodanect.domain.remembrance.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.common.util.CursorFormatter;
import kodanect.domain.remembrance.dto.MemorialCommentCreateRequest;
import kodanect.domain.remembrance.dto.MemorialCommentPasswordRequest;
import kodanect.domain.remembrance.dto.MemorialCommentUpdateRequest;
import kodanect.domain.remembrance.entity.MemorialComment;
import kodanect.domain.remembrance.dto.MemorialCommentResponse;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.repository.MemorialCommentRepository;
import kodanect.domain.remembrance.service.MemorialCommentService;
import kodanect.common.util.MemorialFinder;
import kodanect.common.util.MemorialCommentFinder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * 기증자 추모관 댓글 서비스 구현체
 * <br>
 * 댓글 저장, 수정, 삭제, 더보기 기능을 제공
 *
 **/
@Service
public class MemorialCommentServiceImpl implements MemorialCommentService {

    /** 스레드 생존 기간 */
    private static final int CACHE_EXPIRE_MINUTES = 10;
    /** 멀티 스레딩 갯수 */
    private static final int CACHE_MAX_SIZE = 100_000;

    private final MemorialCommentRepository memorialCommentRepository;
    private final MemorialFinder memorialFinder;
    private final MemorialCommentFinder memorialCommentFinder;

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

    public MemorialCommentServiceImpl(MemorialCommentRepository memorialCommentRepository, MemorialFinder memorialFinder, MemorialCommentFinder memorialCommentFinder){
        this.memorialCommentRepository = memorialCommentRepository;
        this.memorialFinder = memorialFinder;
        this.memorialCommentFinder = memorialCommentFinder;
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
    private ReentrantReadWriteLock getLock(int donateSeq) {
        return lockCache.get(donateSeq, k -> new ReentrantReadWriteLock());
    }

    /**
     *
     * 기증자 추모관 댓글 수정 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param memorialCommentCreateRequest 댓글 생성 요청 dto
     *
     * */
    @Override
    public void createComment(Integer donateSeq, MemorialCommentCreateRequest memorialCommentCreateRequest)
            throws  MemorialNotFoundException
    {
        /* 게시글 댓글 작성 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.writeLock().lock();

        try{
            /* 게시글 조회 */
            memorialFinder.findByIdOrThrow(donateSeq);

            /* 객체 생성 */
            MemorialComment memorialComment = MemorialComment.of(memorialCommentCreateRequest, donateSeq);

            memorialCommentRepository.save(memorialComment);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    /**
     *
     * 기증자 추모관 댓글 수정 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param commentSeq 댓글 번호
     * @param memorialCommentUpdateRequest 댓글 수정 요청 dto
     *
     * */
    @Override
    public void updateComment(Integer donateSeq, Integer commentSeq, MemorialCommentUpdateRequest memorialCommentUpdateRequest)
            throws  MemorialCommentNotFoundException,
                    MemorialNotFoundException,
                    CommentAlreadyDeleteException
    {
        /* 게시글 댓글 수정 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.writeLock().lock();

        try{
            /* 게시글 조회 */
            memorialFinder.findByIdOrThrow(donateSeq);

            /* 댓글 조회 */
            MemorialComment memorialComment = memorialCommentFinder.findByIdOrThrow(commentSeq);

            /* 댓글 삭제 여부 검증 */
            memorialComment.validateNotDeleted();

            /* 댓글 수정 */
            memorialCommentRepository.updateCommentContents(
                    commentSeq,
                    memorialCommentUpdateRequest.getContents(),
                    memorialCommentUpdateRequest.getCommentWriter()
            );
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    /**
     *
     * 기증자 추모관 댓글 삭제 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param commentSeq 댓글 번호
     * @param memorialCommentPasswordRequest 댓글 삭제 요청 dto
     *
     * */
    @Override
    public void deleteComment(Integer donateSeq, Integer commentSeq, MemorialCommentPasswordRequest memorialCommentPasswordRequest)
            throws  CommentPasswordMismatchException,
                    MemorialCommentNotFoundException,
                    MemorialNotFoundException,
                    CommentAlreadyDeleteException
    {
        /* 게시글 댓글 삭제 del_flag = 'Y' 설정 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.writeLock().lock();

        try{
            /* 게시글 조회 */
            memorialFinder.findByIdOrThrow(donateSeq);

            /* 댓글 조회 */
            MemorialComment memorialComment = memorialCommentFinder.findByIdOrThrow(commentSeq);

            /* 비밀번호 일치 여부 검증 */
            memorialComment.validateCommentPassword(memorialCommentPasswordRequest.getCommentPasscode());

            /* 댓글 삭제 여부 검증 */
            memorialComment.validateNotDeleted();

            /* 소프트 삭제 */
            memorialComment.setDelFlag("Y");

            memorialCommentRepository.save(memorialComment);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    /**
     *
     * 기증자 추모관 댓글 비밀번호 검증 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param commentSeq 댓글 번호
     * @param memorialCommentPasswordRequest 비밀번호 검증 dto
     *
     * */
    public void varifyComment(Integer donateSeq, Integer commentSeq, MemorialCommentPasswordRequest memorialCommentPasswordRequest)
            throws  CommentPasswordMismatchException,
                    MemorialCommentNotFoundException,
                    MemorialNotFoundException,
                    CommentAlreadyDeleteException
    {
        /* 게시글 조회 */
        memorialFinder.findByIdOrThrow(donateSeq);

        /* 댓글 조회 */
        MemorialComment memorialComment = memorialCommentFinder.findByIdOrThrow(commentSeq);

        /* 비밀번호 일치 여부 검증 */
        memorialComment.validateCommentPassword(memorialCommentPasswordRequest.getCommentPasscode());

        /* 댓글 삭제 여부 검증 */
        memorialComment.validateNotDeleted();
    }

    /**
     *
     * 기증자 추모관 댓글 리스트 반환 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param cursor 조회할 댓글 페이지 번호(이 ID보다 작은 번호의 댓글을 조회)
     * @param size 조회할 댓글 페이지 사이즈
     * @return 조건에 맞는 댓글 리스트(최신순)
     *
     * */
    @Override
    public List<MemorialCommentResponse> getMemorialCommentList(Integer donateSeq, Integer cursor, int size)
            throws  MemorialNotFoundException
    {
        /* 게시글 댓글 리스트 조회 */

        /* 게시글 조회 */
        memorialFinder.findByIdOrThrow(donateSeq);

        Pageable pageable = PageRequest.of(0, size +1);

        /* 조건에 맞는 댓글 리스트 조회 */
        return memorialCommentRepository.findByCursor(donateSeq, cursor, pageable);

    }

    /**
     *
     * 기증자 추모관 댓글 더 보기(리스트) 반환 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param cursor 조회할 댓글 페이지 번호(이 ID보다 작은 번호의 댓글을 조회)
     * @param size 조회할 댓글 페이지 사이즈
     * @return 조건에 맞는 댓글 리스트(최신순)
     *
     * */
    public CursorCommentPaginationResponse<MemorialCommentResponse, Integer> getMoreCommentList(Integer donateSeq, Integer cursor, int size)
            throws  MemorialNotFoundException
    {
        /* 게시글 댓글 리스트 더보기 */

        /* 게시글 조회 */
        memorialFinder.findByIdOrThrow(donateSeq);

        /* 페이징 */
        Pageable pageable = PageRequest.of(0, size +1);

        /* 댓글 리스트 모두 조회 */
        List<MemorialCommentResponse> memorialCommentResponses = memorialCommentRepository.findByCursor(donateSeq, cursor, pageable);

        return CursorFormatter.cursorCommentFormat(memorialCommentResponses, size);
    }

    /**
     *
     * 기증자 추모관 상세 게시글 댓글 총 갯수 반환 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @return 조건에 맞는 댓글 총 갯수
     *
     * */
    public long getTotalCommentCount(Integer donateSeq) {
        return memorialCommentRepository.countByDonateSeq(donateSeq);
    }
}

