package kodanect.domain.heaven.service.impl;

import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.common.util.CursorFormatter;
import kodanect.common.util.HeavenCommentFinder;
import kodanect.common.util.HeavenFinder;
import kodanect.domain.heaven.dto.request.HeavenCommentCreateRequest;
import kodanect.domain.heaven.dto.request.HeavenCommentUpdateRequest;
import kodanect.domain.heaven.dto.request.HeavenCommentVerifyRequest;
import kodanect.domain.heaven.dto.response.HeavenCommentResponse;
import kodanect.domain.heaven.entity.Heaven;
import kodanect.domain.heaven.entity.HeavenComment;
import kodanect.domain.heaven.repository.HeavenCommentRepository;
import kodanect.domain.heaven.service.HeavenCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeavenCommentServiceImpl implements HeavenCommentService {

    private final HeavenCommentRepository heavenCommentRepository;
    private final HeavenFinder heavenFinder;
    private final HeavenCommentFinder heavenCommentFinder;

    /* 게시물 전체 조회 (페이징) */
    @Override
    public List<HeavenCommentResponse> getHeavenCommentList(Integer letterSeq, Integer cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);

        return heavenCommentRepository.findByCursor(letterSeq, cursor, pageable);
    }

    /* 댓글 더보기 (페이징) */
    @Override
    public CursorCommentPaginationResponse<HeavenCommentResponse, Integer> getMoreCommentList(Integer letterSeq, Integer cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);

        List<HeavenCommentResponse> heavenCommentResponseList = heavenCommentRepository.findByCursor(letterSeq, cursor, pageable);

        return CursorFormatter.cursorCommentFormat(heavenCommentResponseList, size);
    }

    /* 댓글 등록 */
    @Override
    public void createHeavenComment(Integer letterSeq, HeavenCommentCreateRequest heavenCommentCreateRequest) {
        Heaven heaven = heavenFinder.findByIdOrThrow(letterSeq);

        HeavenComment heavenComment = HeavenComment.builder()
                .heaven(heaven)
                .commentWriter(heavenCommentCreateRequest.getCommentWriter())
                .commentPasscode(heavenCommentCreateRequest.getCommentPasscode())
                .contents(heavenCommentCreateRequest.getContents())
                .build();

        heavenCommentRepository.save(heavenComment);
    }

    /* 댓글 수정 인증 */
    @Override
    public void verifyHeavenCommentPasscode(Integer letterSeq, Integer commentSeq, HeavenCommentVerifyRequest heavenCommentVerifyRequest) {
        HeavenComment heavenComment = heavenCommentFinder.findByIdAndValidateOwnership(letterSeq, commentSeq);

        heavenComment.verifyPasscode(heavenCommentVerifyRequest.getCommentPasscode());
    }

    /* 댓글 수정 */
    @Override
    public void updateHeavenComment(Integer letterSeq, Integer commentSeq, HeavenCommentUpdateRequest heavenCommentUpdateRequest) {
        HeavenComment heavenComment = heavenCommentFinder.findByIdAndValidateOwnership(letterSeq, commentSeq);

        heavenComment.updateHeavenComment(heavenCommentUpdateRequest);
    }

    /* 댓글 삭제 */
    @Override
    public void deleteHeavenComment(Integer letterSeq, Integer commentSeq, HeavenCommentVerifyRequest heavenCommentVerifyRequest) {
        HeavenComment heavenComment = heavenCommentFinder.findByIdAndValidateOwnership(letterSeq, commentSeq);

        heavenComment.verifyPasscode(heavenCommentVerifyRequest.getCommentPasscode());

        heavenCommentRepository.delete(heavenComment);
    }
}
