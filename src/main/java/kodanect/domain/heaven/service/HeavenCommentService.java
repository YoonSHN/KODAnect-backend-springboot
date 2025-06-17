package kodanect.domain.heaven.service;

import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.domain.heaven.dto.request.HeavenCommentCreateRequest;
import kodanect.domain.heaven.dto.request.HeavenCommentUpdateRequest;
import kodanect.domain.heaven.dto.request.HeavenCommentVerifyRequest;
import kodanect.domain.heaven.dto.response.HeavenCommentResponse;

import java.util.List;

public interface HeavenCommentService {

    /* 댓글 전체 조회 (페이징) */
    List<HeavenCommentResponse> getHeavenCommentList(Integer letterSeq, Integer cursor, int size);

    /* 댓글 더보기 (페이징) */
    CursorCommentPaginationResponse<HeavenCommentResponse, Integer> getMoreCommentList(Integer letterSeq, Integer cursor, int size);

    /* 댓글 등록 */
    void createHeavenComment(Integer letterSeq, HeavenCommentCreateRequest heavenCommentCreateRequest);

    /* 댓글 수정 인증 */
    void verifyHeavenCommentPasscode(Integer letterSeq, Integer commentSeq, HeavenCommentVerifyRequest heavenCommentVerifyRequest);

    /* 게시물 수정 */
    void updateHeavenComment(Integer letterSeq, Integer commentSeq, HeavenCommentUpdateRequest heavenCommentUpdateRequest);

    /* 댓글 삭제 */
    void deleteHeavenComment(Integer letterSeq, Integer commentSeq, HeavenCommentVerifyRequest heavenCommentVerifyRequest);
}
