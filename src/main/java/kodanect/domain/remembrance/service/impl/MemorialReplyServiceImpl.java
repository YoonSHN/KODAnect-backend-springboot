package kodanect.domain.remembrance.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.common.util.CursorFormatter;
import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.dto.MemorialReplyDeleteRequest;
import kodanect.domain.remembrance.dto.MemorialReplyUpdateRequest;
import kodanect.domain.remembrance.entity.MemorialReply;
import kodanect.domain.remembrance.dto.MemorialReplyResponse;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.repository.MemorialReplyRepository;
import kodanect.domain.remembrance.service.MemorialReplyService;
import kodanect.common.util.MemorialFinder;
import kodanect.common.util.MemorialReplyFinder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class MemorialReplyServiceImpl implements MemorialReplyService {

    private static final int CACHE_EXPIRE_MINUTES = 10;
    private static final int CACHE_MAX_SIZE = 100_000;

    private final MemorialReplyRepository memorialReplyRepository;
    private final MemorialFinder memorialFinder;
    private final MemorialReplyFinder memorialReplyFinder;

    private final Cache<Integer, ReentrantReadWriteLock> lockCache =
            Caffeine.newBuilder().expireAfterAccess(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES).maximumSize(CACHE_MAX_SIZE).build();

    public MemorialReplyServiceImpl(MemorialReplyRepository memorialReplyRepository, MemorialFinder memorialFinder, MemorialReplyFinder memorialReplyFinder){
        this.memorialReplyRepository = memorialReplyRepository;
        this.memorialFinder = memorialFinder;
        this.memorialReplyFinder = memorialReplyFinder;
    }

    private ReentrantReadWriteLock getLock(int donateSeq) {
        return lockCache.get(donateSeq, k -> new ReentrantReadWriteLock());
    }

    @Override
    public void createReply(Integer donateSeq, MemorialReplyCreateRequest memorialReplyCreateRequest)
            throws  MemorialNotFoundException
    {
        /* 게시글 댓글 작성 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.writeLock().lock();

        try{
            /* 게시글 조회 */
            memorialFinder.findByIdOrThrow(donateSeq);

            /* 객체 생성 */
            MemorialReply memorialReply = MemorialReply.of(memorialReplyCreateRequest, donateSeq);

            memorialReplyRepository.save(memorialReply);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void updateReply(Integer donateSeq, Integer replySeq, MemorialReplyUpdateRequest memorialReplyUpdateRequest)
            throws  ReplyPasswordMismatchException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    ReplyAlreadyDeleteException
    {
        /* 게시글 댓글 수정 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.writeLock().lock();

        try{
            /* 게시글 조회 */
            memorialFinder.findByIdOrThrow(donateSeq);

            /* 댓글 조회 */
            MemorialReply memorialReply = memorialReplyFinder.findByIdOrThrow(replySeq);

            /* 비밀번호 일치 여부 검증 */
            memorialReply.validateReplyPassword(memorialReplyUpdateRequest.getReplyPassword());

            /* 댓글 삭제 여부 검증 */
            memorialReply.validateNotDeleted();

            /* 댓글 수정 */
            memorialReplyRepository.updateReplyContents(replySeq, memorialReplyUpdateRequest.getReplyContents());
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteReply(Integer donateSeq, Integer replySeq, MemorialReplyDeleteRequest memorialReplyDeleteRequest)
            throws  ReplyPasswordMismatchException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    ReplyAlreadyDeleteException
    {
        /* 게시글 댓글 삭제 del_flag = 'Y' 설정 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.writeLock().lock();

        try{
            /* 게시글 조회 */
            memorialFinder.findByIdOrThrow(donateSeq);

            /* 댓글 조회 */
            MemorialReply memorialReply = memorialReplyFinder.findByIdOrThrow(replySeq);

            /* 비밀번호 일치 여부 검증 */
            memorialReply.validateReplyPassword(memorialReplyDeleteRequest.getReplyPassword());

            /* 댓글 삭제 여부 검증 */
            memorialReply.validateNotDeleted();

            /* 소프트 삭제 */
            memorialReply.setDelFlag("Y");

            memorialReplyRepository.save(memorialReply);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<MemorialReplyResponse> getMemorialReplyList(Integer donateSeq, Integer cursor, int size)
            throws  MemorialNotFoundException
    {
        /* 게시글 댓글 리스트 조회 */

        /* 게시글 조회 */
        memorialFinder.findByIdOrThrow(donateSeq);

        Pageable pageable = PageRequest.of(0, size +1);

        /* 댓글 리스트 모두 조회 */
        return memorialReplyRepository.findByCursor(donateSeq, cursor, pageable);

    }

    public CursorReplyPaginationResponse<MemorialReplyResponse, Integer> getMoreReplyList(Integer donateSeq, Integer cursor, int size)
            throws  MemorialNotFoundException
    {
        /* 게시글 댓글 리스트 더보기 */

        /* 게시글 조회 */
        memorialFinder.findByIdOrThrow(donateSeq);

        /* 페이징 */
        Pageable pageable = PageRequest.of(0, size +1);

        /* 댓글 리스트 모두 조회 */
        List<MemorialReplyResponse> memorialReplyResponses = memorialReplyRepository.findByCursor(donateSeq, cursor, pageable);

        return CursorFormatter.cursorReplyFormat(memorialReplyResponses, size);
    }
}

