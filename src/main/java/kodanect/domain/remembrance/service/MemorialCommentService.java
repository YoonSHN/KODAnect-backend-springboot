package kodanect.domain.remembrance.service;

import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.domain.remembrance.dto.MemorialCommentPasswordRequest;
import kodanect.domain.remembrance.dto.MemorialCommentResponse;
import kodanect.domain.remembrance.dto.MemorialCommentCreateRequest;
import kodanect.domain.remembrance.dto.MemorialCommentUpdateRequest;
import kodanect.domain.remembrance.exception.MemorialNotFoundException;
import kodanect.domain.remembrance.exception.MemorialCommentNotFoundException;
import kodanect.domain.remembrance.exception.CommentAlreadyDeleteException;
import kodanect.domain.remembrance.exception.CommentPasswordMismatchException;

import java.util.List;

public interface MemorialCommentService {
    /**
     *
     * 기증자 추모관 댓글 수정 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param memorialCommentCreateRequest 댓글 생성 요청 dto
     *
     * */
    void createComment(Integer donateSeq, MemorialCommentCreateRequest memorialCommentCreateRequest)
            throws MemorialNotFoundException;
    /**
     *
     * 기증자 추모관 댓글 수정 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param commentSeq 댓글 번호
     * @param memorialCommentUpdateRequest 댓글 수정 요청 dto
     *
     * */
    void updateComment(Integer donateSeq, Integer commentSeq, MemorialCommentUpdateRequest memorialCommentUpdateRequest);
    /**
     *
     * 기증자 추모관 댓글 삭제 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param commentSeq 댓글 번호
     * @param memorialCommentPasswordRequest 댓글 삭제 요청 dto
     *
     * */
    void deleteComment(Integer donateSeq, Integer commentSeq, MemorialCommentPasswordRequest memorialCommentPasswordRequest)
            throws  CommentPasswordMismatchException,
                    MemorialCommentNotFoundException,
                    MemorialNotFoundException,
                    CommentAlreadyDeleteException;
    /**
     *
     * 기증자 추모관 댓글 비밀번호 검증 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param commentSeq 댓글 번호
     * @param memorialCommentPasswordRequest 비밀번호 검증 dto
     *
     * */
    void varifyComment(Integer donateSeq, Integer commentSeq, MemorialCommentPasswordRequest memorialCommentPasswordRequest)
            throws  CommentPasswordMismatchException,
                    MemorialCommentNotFoundException,
                    MemorialNotFoundException,
                    CommentAlreadyDeleteException;
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
    List<MemorialCommentResponse> getMemorialCommentList(Integer donateSeq, Integer cursor, int size)
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
    CursorCommentPaginationResponse<MemorialCommentResponse, Integer> getMoreCommentList(Integer donateSeq, Integer cursor, int size)
            throws  MemorialNotFoundException;

    /**
     * 
     * 기증자 추모관 상세 게시글 댓글 총 갯수 반환 메서드
     * 
     * @param donateSeq 상세 게시글 번호
     * @return 조건에 맞는 댓글 총 갯수
     * 
     * */
    long getTotalCommentCount(Integer donateSeq);
}
