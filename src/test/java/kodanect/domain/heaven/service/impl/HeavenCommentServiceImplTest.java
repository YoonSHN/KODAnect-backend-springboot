package kodanect.domain.heaven.service.impl;

import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.common.util.HeavenCommentFinder;
import kodanect.common.util.HeavenFinder;
import kodanect.domain.heaven.dto.request.HeavenCommentCreateRequest;
import kodanect.domain.heaven.dto.request.HeavenCommentUpdateRequest;
import kodanect.domain.heaven.dto.request.HeavenCommentVerifyRequest;
import kodanect.domain.heaven.dto.response.HeavenCommentResponse;
import kodanect.domain.heaven.entity.Heaven;
import kodanect.domain.heaven.entity.HeavenComment;
import kodanect.domain.heaven.repository.HeavenCommentRepository;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HeavenCommentServiceImplTest {

    @InjectMocks
    private HeavenCommentServiceImpl heavenCommentServiceImpl;
    @Mock
    private HeavenCommentRepository heavenCommentRepository;
    @Mock
    private HeavenFinder heavenFinder;
    @Mock
    private HeavenCommentFinder heavenCommentFinder;

    @Test
    @DisplayName("댓글 전체 조회 테스트")
    public void getHeavenCommentListTest() {
        /* given */
        // 매개변수 값 설정
        int letterSeq = 14;
        int size = 3;
        int commentSize = 5;

        // HeavenCommentResponse 값 설정
        String commentWriter = "댓글 작성자";
        String contents = "댓글 내용";
        LocalDateTime writeTime = LocalDateTime.now();

        // heavenCommentRepository.findByCursor 결과 미리 설정
        List<HeavenCommentResponse> heavenCommentResponseList = new ArrayList<>();
        for (int i = commentSize; i >= 1; i--) {
            heavenCommentResponseList.add(new HeavenCommentResponse(i, commentWriter, contents, writeTime));
        }

        when(heavenCommentRepository.findByCursor(eq(letterSeq), eq(null), any(Pageable.class))).thenReturn(heavenCommentResponseList);

        /* when */
        List<HeavenCommentResponse> heavenCommentList = heavenCommentServiceImpl.getHeavenCommentList(letterSeq, null, size);
        HeavenCommentResponse firstHeavenCommentResponse = heavenCommentList.get(0);

        /* then */
        assertNotNull(heavenCommentList);
        assertEquals(commentSize, heavenCommentList.size());

        assertEquals(commentSize, firstHeavenCommentResponse.getCommentSeq());
        assertEquals(commentWriter, firstHeavenCommentResponse.getCommentWriter());
        assertEquals(contents, firstHeavenCommentResponse.getContents());
        assertEquals(writeTime.toLocalDate().toString(), firstHeavenCommentResponse.getWriteTime());
    }

    @Test
    @DisplayName("댓글 더보기 테스트")
    public void getMoreCommentListTest() {
        /* given */
        // 매개변수 값 설정
        Integer letterSeq = 29;
        int size = 3;
        int commentSize = 7;

        // HeavenCommentResponse 값 설정
        String commentWriter = "댓글 작성자";
        String contents = "댓글 내용";
        LocalDateTime writeTime = LocalDateTime.now();

        // 결과 값 설정
        Integer commentNextCursor = commentSize - size + 1;
        boolean commentHasNext = true;

        // heavenCommentRepository.findByCursor() 결과 미리 설정
        List<HeavenCommentResponse> heavenCommentResponseList = new ArrayList<>();
        for (int i = commentSize; i >= 1; i--) {
            heavenCommentResponseList.add(new HeavenCommentResponse(i, commentWriter, contents, writeTime));
        }

        when(heavenCommentRepository.findByCursor(eq(letterSeq), eq(null), any(Pageable.class))).thenReturn(heavenCommentResponseList);

        /* when */
        CursorCommentPaginationResponse<HeavenCommentResponse, Integer> cursorCommentPaginationResponse = heavenCommentServiceImpl.getMoreCommentList(letterSeq, null, size);
        List<HeavenCommentResponse> resultCommentResponseList = cursorCommentPaginationResponse.getContent();
        HeavenCommentResponse firstHeavenCommentResponse = resultCommentResponseList.get(0);

        /* then */
        assertNotNull(firstHeavenCommentResponse);
        assertEquals(commentNextCursor, cursorCommentPaginationResponse.getCommentNextCursor());
        assertEquals(commentHasNext, cursorCommentPaginationResponse.isCommentHasNext());
        assertEquals(size, resultCommentResponseList.size());

        assertEquals(commentSize, firstHeavenCommentResponse.getCommentSeq());
        assertEquals(commentWriter, firstHeavenCommentResponse.getCommentWriter());
        assertEquals(contents, firstHeavenCommentResponse.getContents());
        assertEquals(writeTime.toLocalDate().toString(), firstHeavenCommentResponse.getWriteTime());
    }

    @Test
    @DisplayName("댓글 등록 테스트")
    public void createHeavenCommentTest() {
        /* given */
        // 매개변수 값 설정
        int letterSeq = 14;
        String commentWriter = "댓글 작성자";
        String commentPasscode = "qwer1234";
        String contents = "댓글 내용";
        HeavenCommentCreateRequest heavenCommentCreateRequest = HeavenCommentCreateRequest.builder()
                .commentWriter(commentWriter)
                .commentPasscode(commentPasscode)
                .contents(contents)
                .build();

        // heavenFinder.findByIdOrThrow() 결과 미리 설정
        String letterTitle = "게시물 제목";
        String letterPasscode = "asdf1234";
        String letterWriter = "게시물 작성자";
        String letterContents = "게시물 내용";
        Heaven heaven = Heaven.builder()
                .letterSeq(letterSeq)
                .letterTitle(letterTitle)
                .letterPasscode(letterPasscode)
                .letterWriter(letterWriter)
                .letterContents(letterContents)
                .build();

        when(heavenFinder.findByIdOrThrow(letterSeq)).thenReturn(heaven);
        // HeavenComment 값 저장할 수 있는 객체
        ArgumentCaptor<HeavenComment> captor = ArgumentCaptor.forClass(HeavenComment.class);

        /* when */
        heavenCommentServiceImpl.createHeavenComment(letterSeq, heavenCommentCreateRequest);

        /* then */
        verify(heavenFinder, times(1)).findByIdOrThrow(letterSeq);
        // save() 실행 시 저장값 captor에 캡쳐
        verify(heavenCommentRepository, times(1)).save(captor.capture());

        HeavenComment saveHeavenComment = captor.getValue();
        Heaven resultHeaven = saveHeavenComment.getHeaven();

        assertNotNull(saveHeavenComment);
        assertNotNull(resultHeaven);

        assertEquals(letterSeq, resultHeaven.getLetterSeq());
        assertEquals(letterTitle, resultHeaven.getLetterTitle());
        assertEquals(letterPasscode, resultHeaven.getLetterPasscode());
        assertEquals(letterWriter, resultHeaven.getLetterWriter());
        assertEquals(letterContents, resultHeaven.getLetterContents());

        assertEquals(commentWriter, saveHeavenComment.getCommentWriter());
        assertEquals(commentPasscode, saveHeavenComment.getCommentPasscode());
        assertEquals(contents, saveHeavenComment.getContents());
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    public void updateHeavenCommentTest() {
        /* given */
        // 매개변수 값 설정
        int letterSeq = 29;
        int commentSeq = 14;
        String commentWriter = "수정한 댓글 작성자";
        String contents = "수정한 댓글 내용";
        HeavenCommentUpdateRequest heavenCommentUpdateRequest = HeavenCommentUpdateRequest.builder()
                .commentWriter(commentWriter)
                .contents(contents)
                .build();

        // heavenCommentFinder.findByIdAndValidateOwnership() 결과 미리 설정
        String letterTitle = "게시물 제목";
        String letterPasscode = "asdf1234";
        String letterWriter = "게시물 작성자";
        String letterContents = "게시물 내용";
        Heaven heaven = Heaven.builder()
                .letterSeq(letterSeq)
                .letterTitle(letterTitle)
                .letterPasscode(letterPasscode)
                .letterWriter(letterWriter)
                .letterContents(letterContents)
                .build();

        String beforeCommentWriter = "기존 댓글 작성자";
        String beforeContents = "기존 댓글 내용";
        HeavenComment heavenComment = HeavenComment.builder()
                .commentSeq(commentSeq)
                .heaven(heaven)
                .commentWriter(beforeCommentWriter)
                .contents(beforeContents)
                .build();

        when(heavenCommentFinder.findByIdAndValidateOwnership(letterSeq, commentSeq)).thenReturn(heavenComment);

        /* when */
        HeavenComment updateHeavenComment = heavenCommentServiceImpl.updateHeavenComment(letterSeq, commentSeq, heavenCommentUpdateRequest);
        Heaven resultHeaven = updateHeavenComment.getHeaven();

        /* then */
        verify(heavenCommentFinder, times(1)).findByIdAndValidateOwnership(letterSeq, commentSeq);

        assertNotNull(updateHeavenComment);

        assertEquals(letterSeq, resultHeaven.getLetterSeq());
        assertEquals(letterTitle, resultHeaven.getLetterTitle());
        assertEquals(letterPasscode, resultHeaven.getLetterPasscode());
        assertEquals(letterWriter, resultHeaven.getLetterWriter());
        assertEquals(letterContents, resultHeaven.getLetterContents());

        assertEquals(commentSeq, updateHeavenComment.getCommentSeq());
        assertEquals(commentWriter, updateHeavenComment.getCommentWriter());
        assertEquals(contents, updateHeavenComment.getContents());
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    public void deleteHeavenCommentTest() {
        /* given */
        // 매개변수 값 설정
        int letterSeq = 29;
        int commentSeq = 14;
        String commentPasscode = "qwer1234";
        HeavenCommentVerifyRequest heavenCommentVerifyRequest = HeavenCommentVerifyRequest.builder()
                .commentPasscode(commentPasscode)
                .build();

        // heavenCommentFinder.findByIdAndValidateOwnership() 결과 설정
        HeavenComment mockHeavenComment = mock(HeavenComment.class);

        when(heavenCommentFinder.findByIdAndValidateOwnership(letterSeq, commentSeq)).thenReturn(mockHeavenComment);

        /* when */
        heavenCommentServiceImpl.deleteHeavenComment(letterSeq, commentSeq, heavenCommentVerifyRequest);

        /* then */
        verify(heavenCommentFinder, times(1)).findByIdAndValidateOwnership(letterSeq, commentSeq);
        verify(mockHeavenComment, times(1)).verifyPasscode(commentPasscode);
        verify(mockHeavenComment, times(1)).softDelete();
    }
}