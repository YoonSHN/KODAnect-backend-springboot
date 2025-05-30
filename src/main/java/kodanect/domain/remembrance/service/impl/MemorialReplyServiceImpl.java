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

import static kodanect.common.validation.DonateSeqValidator.validateDonateSeq;
import static kodanect.common.validation.ReplySeqValidator.replySeqValidate;
import static kodanect.common.validation.ReplyValidator.validateReplyAuthority;
import static kodanect.common.validation.ReplyValidator.validateReplyWriteFields;
import static kodanect.common.validation.ReplyValidator.validateReplyContent;

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
            throws  MissingReplyContentException,
                    MissingReplyWriterException,
                    MissingReplyPasswordException,
                    InvalidDonateSeqException,
                    MemorialNotFoundException
    {
        /* 게시글 댓글 작성 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.writeLock().lock();

        try{
            /* 댓글 내용, 작성자, 비밀 번호 검증 */
            validateReplyWriteFields(memorialReplyCreateRequest);

            /* 게시글 ID 검증 */
            validateDonateSeq(donateSeq);

            /* 게시글 조회 */
            memorialFinder.findByIdOrThrow(donateSeq);

            /* 객체 생성 */
            MemorialReply memorialReply = MemorialReply.of(memorialReplyCreateRequest);

            memorialReplyRepository.save(memorialReply);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void updateReply(Integer donateSeq, Integer replySeq, MemorialReplyUpdateRequest memorialReplyUpdateRequest)
            throws  InvalidDonateSeqException,
                    MissingReplyContentException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    InvalidReplySeqException,
                    ReplyAlreadyDeleteException
    {
        /* 게시글 댓글 수정 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.writeLock().lock();

        try{
            /* 게시글 ID 검증 */
            validateDonateSeq(donateSeq);

            /* 댓글 ID 검증 */
            replySeqValidate(replySeq);

            /* 게시글 조회 */
            memorialFinder.findByIdOrThrow(donateSeq);

            /* 댓글 조회 */
            MemorialReply memorialReply = memorialReplyFinder.findByIdOrThrow(replySeq);

            /* 댓글 번호, 비밀 번호, 게시판, 삭제 여부 검증 */
            validateReplyAuthority(donateSeq, replySeq, memorialReplyUpdateRequest, memorialReply);

            /* 댓글 내용 검증 */
            validateReplyContent(memorialReplyUpdateRequest);

            /* 댓글 수정 */
            memorialReplyRepository.updateReplyContents(replySeq, memorialReplyUpdateRequest.getReplyContents());
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteReply(Integer donateSeq, Integer replySeq, MemorialReplyDeleteRequest memorialReplyDeleteRequest)
            throws  ReplyPostMismatchException,
                    ReplyIdMismatchException,
                    MissingReplyPasswordException,
                    ReplyPasswordMismatchException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    InvalidReplySeqException,
                    InvalidDonateSeqException,
                    ReplyAlreadyDeleteException
    {
        /* 게시글 댓글 삭제 del_flag = 'Y' 설정 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.writeLock().lock();

        try{
            /* 게시글 ID 검증 */
            validateDonateSeq(donateSeq);

            /* 댓글 ID 검증 */
            replySeqValidate(replySeq);

            /* 게시글 조회 */
            memorialFinder.findByIdOrThrow(donateSeq);

            /* 댓글 조회 */
            MemorialReply memorialReply = memorialReplyFinder.findByIdOrThrow(replySeq);

            /* 댓글 번호, 비밀 번호, 게시판, 삭제 여부 검증 */
            validateReplyAuthority(donateSeq, replySeq, memorialReplyDeleteRequest, memorialReply);

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
            throws  MemorialNotFoundException,
                    InvalidDonateSeqException
    {
        /* 게시글 댓글 리스트 조회 */
        ReentrantReadWriteLock lock = getLock(donateSeq);
        lock.readLock().lock();

        try{
            /* 게시글 ID 검증 */
            validateDonateSeq(donateSeq);

            /* 게시글 조회 */
            memorialFinder.findByIdOrThrow(donateSeq);

            Pageable pageable = PageRequest.of(0, size +1);

            /* 댓글 리스트 모두 조회 */
            return memorialReplyRepository.findByCursor(donateSeq, cursor, pageable);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public CursorReplyPaginationResponse<MemorialReplyResponse> getMoreReplyList(Integer donateSeq, Integer cursor, int size)
            throws  MemorialNotFoundException,
                    InvalidDonateSeqException
    {
        /* 게시글 댓글 리스트 더보기 */

        /* 게시글 ID 검증 */
        validateDonateSeq(donateSeq);

        /* 게시글 조회 */
        memorialFinder.findByIdOrThrow(donateSeq);

        /* 페이징 */
        Pageable pageable = PageRequest.of(0, size +1);

        /* 댓글 리스트 모두 조회 */
        List<MemorialReplyResponse> memorialReplyResponses = memorialReplyRepository.findByCursor(donateSeq, cursor, pageable);

        return CursorFormatter.cursorReplyFormat(memorialReplyResponses, size);
    }
}

