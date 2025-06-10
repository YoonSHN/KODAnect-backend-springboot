package kodanect.domain.remembrance.service;

import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.domain.remembrance.dto.MemorialReplyDeleteRequest;
import kodanect.domain.remembrance.dto.MemorialReplyResponse;
import kodanect.domain.remembrance.dto.MemorialReplyCreateRequest;
import kodanect.domain.remembrance.dto.MemorialReplyUpdateRequest;
import kodanect.domain.remembrance.exception.MemorialNotFoundException;
import kodanect.domain.remembrance.exception.MemorialReplyNotFoundException;
import kodanect.domain.remembrance.exception.ReplyAlreadyDeleteException;
import kodanect.domain.remembrance.exception.ReplyPasswordMismatchException;

import java.util.List;

public interface MemorialReplyService {
    /**
     *
     * 기증자 추모관 댓글 수정 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param memorialReplyCreateRequest 댓글 생성 요청 dto
     *
     * */
    void createReply(Integer donateSeq, MemorialReplyCreateRequest memorialReplyCreateRequest)
            throws MemorialNotFoundException;
    /**
     *
     * 기증자 추모관 댓글 수정 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param replySeq 댓글 번호
     * @param memorialReplyUpdateRequest 댓글 수정 요청 dto
     *
     * */
    void updateReply(Integer donateSeq, Integer replySeq, MemorialReplyUpdateRequest memorialReplyUpdateRequest)
            throws ReplyPasswordMismatchException,
            MemorialReplyNotFoundException,
                    MemorialNotFoundException,
            ReplyAlreadyDeleteException;
    /**
     *
     * 기증자 추모관 댓글 삭제 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param replySeq 댓글 번호
     * @param memorialReplyDeleteRequest 댓글 삭제 요청 dto
     *
     * */
    void deleteReply(Integer donateSeq, Integer replySeq, MemorialReplyDeleteRequest memorialReplyDeleteRequest)
            throws  ReplyPasswordMismatchException,
                    MemorialReplyNotFoundException,
                    MemorialNotFoundException,
                    ReplyAlreadyDeleteException;
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
    List<MemorialReplyResponse> getMemorialReplyList(Integer donateSeq, Integer cursor, int size)
            throws  MemorialNotFoundException;
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
    CursorReplyPaginationResponse<MemorialReplyResponse, Integer> getMoreReplyList(Integer donateSeq, Integer cursor, int size)
            throws  MemorialNotFoundException;
}
