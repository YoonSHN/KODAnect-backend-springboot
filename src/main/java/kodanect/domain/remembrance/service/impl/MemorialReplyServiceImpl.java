package kodanect.domain.remembrance.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kodanect.domain.remembrance.entity.MemorialReply;
import kodanect.domain.remembrance.dto.MemorialReplyDto;
import kodanect.domain.remembrance.exception.*;
import kodanect.domain.remembrance.repository.MemorialReplyRepository;
import kodanect.domain.remembrance.service.MemorialReplyService;
import kodanect.common.util.MemorialFinder;
import kodanect.common.util.MemorialReplyFinder;
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
    public void createReply(Integer donateSeq, MemorialReplyDto memorialReplyDto)
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
            validateReplyWriteFields(memorialReplyDto);

            /* 게시글 ID 검증 */
            validateDonateSeq(donateSeq);

            /* 게시글 조회 */
            memorialFinder.findByIdOrThrow(donateSeq);

            MemorialReply reply = MemorialReply.builder()
                    .donateSeq(memorialReplyDto.getDonateSeq())
                    .replyWriter(memorialReplyDto.getReplyWriter())
                    .replyPassword(memorialReplyDto.getReplyPassword())
                    .replyContents(memorialReplyDto.getReplyContents())
                    .build();

            memorialReplyRepository.save(reply);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void updateReply(Integer donateSeq, Integer replySeq, MemorialReplyDto memorialReplyDto)
            throws  InvalidDonateSeqException,
                    MissingReplyContentException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    InvalidReplySeqException
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
            MemorialReply reply = memorialReplyFinder.findByIdOrThrow(replySeq);

            /* 댓글 번호, 비밀 번호, 게시판, 검증 */
            validateReplyAuthority(donateSeq, replySeq, memorialReplyDto, reply);

            /* 댓글 내용 검증 */
            validateReplyContent(memorialReplyDto);

            reply.setReplyContents(memorialReplyDto.getReplyContents());
            memorialReplyRepository.save(reply);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteReply(Integer donateSeq, Integer replySeq, MemorialReplyDto memorialReplyDto)
            throws  ReplyPostMismatchException,
                    ReplyIdMismatchException,
                    MissingReplyPasswordException,
                    ReplyPasswordMismatchException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    InvalidReplySeqException,
                    InvalidDonateSeqException
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
            MemorialReply reply = memorialReplyFinder.findByIdOrThrow(replySeq);

            /* 댓글 번호, 비밀 번호, 게시판, 검증 */
            validateReplyAuthority(donateSeq, replySeq, memorialReplyDto, reply);

            /* 소프트 삭제 */
            reply.setDelFlag("Y");
            memorialReplyRepository.save(reply);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<MemorialReplyDto> findMemorialReplyList(Integer donateSeq)
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

            /* 댓글 리스트 모두 조회 */
            return memorialReplyRepository.findMemorialReplyList(donateSeq);
        }
        finally {
            lock.readLock().unlock();
        }
    }
}

