package kodanect.domain.heaven.service.impl;

import kodanect.common.imageupload.service.FileService;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.util.HeavenFinder;
import kodanect.common.util.MemorialFinder;
import kodanect.common.util.ViewTracker;
import kodanect.domain.heaven.dto.HeavenDto;
import kodanect.domain.heaven.dto.request.HeavenCreateRequest;
import kodanect.domain.heaven.dto.request.HeavenUpdateRequest;
import kodanect.domain.heaven.dto.response.HeavenDetailResponse;
import kodanect.domain.heaven.dto.response.HeavenResponse;
import kodanect.domain.heaven.dto.response.MemorialHeavenResponse;
import kodanect.domain.heaven.entity.Heaven;
import kodanect.domain.heaven.repository.HeavenCommentRepository;
import kodanect.domain.heaven.repository.HeavenRepository;
import kodanect.domain.heaven.service.HeavenCommentService;
import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.repository.MemorialRepository;
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
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HeavenServiceImplTest {

    @InjectMocks
    private HeavenServiceImpl heavenServiceImpl;
    @Mock
    private HeavenRepository heavenRepository;
    @Mock
    private HeavenCommentRepository heavenCommentRepository;
    @Mock
    private HeavenCommentService heavenCommentService;
    @Mock
    private HeavenFinder heavenFinder;
    @Mock
    private FileService fileService;
    @Mock
    private ViewTracker viewTracker;
    @Mock
    private MemorialFinder memorialFinder;
    @Mock
    private MemorialRepository memorialRepository;

    @Test
    @DisplayName("게시물 전체 조회 테스트")
    public void getHeavenListTest() {
        /* given */
        Integer cursor = 1000;
        int size = 20;
        String memorialAnonymityFlag = "N";
        String heavenAnonymityFlag = "N";
        int readCount = 5;
        LocalDateTime now = LocalDateTime.now();

        // heavenRepository.countByDelFlag() 값 미리 설정
        Long heavenCount = 50L;

        // heavenRepository.findByCursor() 값 미리 설정
        List<HeavenResponse> heavenResponseList = new ArrayList<>();
        for (int i = 1; i <= heavenCount; i++) {
            heavenResponseList.add(new HeavenResponse(i, "제목"+i, "기증자"+i, memorialAnonymityFlag, "작성자"+i, heavenAnonymityFlag, readCount, now));
        }

        // 메서드 결과값 설정
        when(heavenRepository.findByCursor(eq(cursor), any(Pageable.class))).thenReturn(heavenResponseList);
        when(heavenRepository.countByDelFlag()).thenReturn(heavenCount);

        /* when */
        CursorPaginationResponse<HeavenResponse, Integer> cursorPaginationResponse = heavenServiceImpl.getHeavenList(cursor, size);
        HeavenResponse firstHeavenResponse = cursorPaginationResponse.getContent().get(0);

        /* then */
        assertNotNull(cursorPaginationResponse);
        assertEquals(size, cursorPaginationResponse.getContent().size());
        assertTrue(cursorPaginationResponse.isHasNext());
        assertEquals(heavenCount, cursorPaginationResponse.getTotalCount());

        assertEquals(1, firstHeavenResponse.getLetterSeq());
        assertEquals("제목1", firstHeavenResponse.getLetterTitle());
        assertEquals("기증자1", firstHeavenResponse.getDonorName());
        assertEquals(memorialAnonymityFlag, firstHeavenResponse.getMemorialAnonymityFlag());
        assertEquals("작성자1", firstHeavenResponse.getLetterWriter());
        assertEquals(heavenAnonymityFlag, firstHeavenResponse.getHeavenAnonymityFlag());
        assertEquals(Integer.valueOf(readCount), firstHeavenResponse.getReadCount());
        assertEquals(now.toLocalDate().toString(), firstHeavenResponse.getWriteTime());
    }

    @Test
    @DisplayName("검색을 통한 게시물 전체 조회 테스트")
    public void getHeavenListSearchResultTest() {
        /* given */
        String type = "ALL";
        String keyWord = "제목";
        Integer cursor = 1000;
        int size = 20;
        String memorialAnonymityFlag = "N";
        String heavenAnonymityFlag = "N";
        int readCount = 13;
        LocalDateTime now = LocalDateTime.now();

        // heavenRepository.countByTitleOrContentsContaining() 값 미리 설정
        Long heavenCount = 30L;

        // heavenRepository.findByTitleOrContentsContaining() 값 미리 설정
        List<HeavenResponse> heavenResponseList = new ArrayList<>();
        for (int i = 1; i <= heavenCount; i++) {
            heavenResponseList.add(new HeavenResponse(i, "제목"+i, "기증자"+i, memorialAnonymityFlag, "작성자"+i, heavenAnonymityFlag, readCount, now));
        }

        when(heavenRepository.findByTitleOrContentsContaining(eq(keyWord), eq(cursor), any(Pageable.class))).thenReturn(heavenResponseList);
        when(heavenRepository.countByTitleOrContentsContaining(keyWord)).thenReturn(30L);

        /* when */
        CursorPaginationResponse<HeavenResponse, Integer> cursorPaginationResponse = heavenServiceImpl.getHeavenListSearchResult(type, keyWord, cursor, size);
        HeavenResponse firstHeavenResponse = cursorPaginationResponse.getContent().get(0);

        /* then */
        assertNotNull(cursorPaginationResponse);
        assertEquals(size, cursorPaginationResponse.getContent().size());
        assertTrue(cursorPaginationResponse.isHasNext());
        assertEquals(heavenCount, cursorPaginationResponse.getTotalCount());

        assertEquals(1, firstHeavenResponse.getLetterSeq());
        assertEquals("제목1", firstHeavenResponse.getLetterTitle());
        assertEquals("기증자1", firstHeavenResponse.getDonorName());
        assertEquals(memorialAnonymityFlag, firstHeavenResponse.getMemorialAnonymityFlag());
        assertEquals("작성자1", firstHeavenResponse.getLetterWriter());
        assertEquals(heavenAnonymityFlag, firstHeavenResponse.getHeavenAnonymityFlag());
        assertEquals(Integer.valueOf(readCount), firstHeavenResponse.getReadCount());
        assertEquals(now.toLocalDate().toString(), firstHeavenResponse.getWriteTime());
    }

    @Test
    @DisplayName("게시물 상세 조회 테스트")
    public void getHeavenDetailTest() {
        /* given */
        String clientIp = "11.22.33.44";

        // heavenDto 값 설정
        int letterSeq = 14;
        String letterTitle = "게시물 작성자";
        String letterWriter = "홍길동";
        String heavenAnonymityFlag = "Y";
        Integer readCount = 0;
        String letterContents = "게시물 내용";
        LocalDateTime writeTime = LocalDateTime.now();

        // heavenCommentResponse 값 설정
        int totalCommentCount = 30;

        // heavenFinder.findAnonymizedByIdOrThrow() 값 미리 설정
        HeavenDto heavenDto = HeavenDto.builder()
                .letterSeq(letterSeq)
                .letterTitle(letterTitle)
                .letterWriter(letterWriter)
                .heavenAnonymityFlag(heavenAnonymityFlag)
                .readCount(readCount)
                .letterContents(letterContents)
                .writeTime(writeTime)
                .build();

        when(heavenFinder.findAnonymizedByIdOrThrow(letterSeq)).thenReturn(heavenDto);
        when(viewTracker.shouldIncreaseView(letterSeq, clientIp)).thenReturn(true);
        when(heavenCommentRepository.countByLetterSeq(letterSeq)).thenReturn((long) totalCommentCount);

        /* when */
        HeavenDetailResponse heavenDetail = heavenServiceImpl.getHeavenDetail(letterSeq, clientIp);
        HeavenDto resultHeavenDto = heavenDetail.getHeavenDto();

        /* then */
        assertNotNull(heavenDetail);

        assertEquals(letterSeq, resultHeavenDto.getLetterSeq());
        assertEquals(letterTitle, resultHeavenDto.getLetterTitle());
        assertEquals(letterWriter, resultHeavenDto.getLetterWriter());
        verify(heavenRepository, times(1)).updateReadCount(letterSeq);
        assertEquals(letterContents, resultHeavenDto.getLetterContents());
        assertEquals(writeTime.toLocalDate().toString(), resultHeavenDto.getWriteTime());
    }

    @Test
    @DisplayName("기증자 추모관 상세 조회 시 하늘나라 편지 전체 조회 테스트")
    public void getMemorialHeavenListTest() {
        /* given */
        // 매개변수 값 설정
        Integer donateSeq = 14;
        int size = 10;

        // memorialFinder.findByIdOrThrow() 결과 미리 설정
        String donorName = "기증자 명";
        String donateTitle = "추모합니다.";
        String contents = "기증자 내용";
        Memorial memorial = Memorial.builder()
                .donateSeq(donateSeq)
                .donorName(donorName)
                .donateTitle(donateTitle)
                .contents(contents)
                .build();

        // heavenRepository.findMemorialHeavenResponseById() 결과 미리 설정
        int memorialHeavenSize = 5;
        String letterTitle = "편지 제목";
        Integer readCount = 2;
        LocalDateTime writeTime = LocalDateTime.now();
        List<MemorialHeavenResponse> memorialHeavenResponseList = new ArrayList<>();
        for (int i = memorialHeavenSize; i >= 1; i--) {
            memorialHeavenResponseList.add(new MemorialHeavenResponse(i, letterTitle, readCount ,writeTime));
        }

        when(memorialFinder.findByIdOrThrow(donateSeq)).thenReturn(memorial);
        when(heavenRepository.findMemorialHeavenResponseById(eq(memorial), eq(null), any(Pageable.class))).thenReturn(memorialHeavenResponseList);
        when(heavenRepository.countByMemorial(memorial)).thenReturn((long) memorialHeavenSize);

        /* when */
        CursorPaginationResponse<MemorialHeavenResponse, Integer> cursorPaginationResponse = heavenServiceImpl.getMemorialHeavenList(donateSeq, null, size);
        List<MemorialHeavenResponse> resultMemorialHeavenResponseList = cursorPaginationResponse.getContent();
        MemorialHeavenResponse firstMemorialHeavenResponse = resultMemorialHeavenResponseList.get(0);

        /* then */
        assertNotNull(firstMemorialHeavenResponse);
        assertNull(cursorPaginationResponse.getNextCursor());
        assertFalse(cursorPaginationResponse.isHasNext());
        assertEquals(memorialHeavenSize, (long) cursorPaginationResponse.getTotalCount());

        assertEquals(memorialHeavenSize, resultMemorialHeavenResponseList.size());

        assertEquals(memorialHeavenSize, firstMemorialHeavenResponse.getLetterSeq());
        assertEquals(letterTitle, firstMemorialHeavenResponse.getLetterTitle());
        assertEquals(readCount, firstMemorialHeavenResponse.getReadCount());
        assertEquals(writeTime.toLocalDate().toString(), firstMemorialHeavenResponse.getWriteTime());
    }

    @Test
    @DisplayName("게시물 생성 테스트")
    public void createHeavenTest() {
        /* given */
        // 매개변수 값 설정
        String letterWriter = "편지 작성자";
        String letterPasscode = "편지 비밀번호";
        String donorName = "기증자 명";
        int donateSeq = 14;
        String letterTitle = "편지 제목";
        String letterContents = "편지 내용";
        HeavenCreateRequest heavenCreateRequest = HeavenCreateRequest.builder()
                .letterWriter(letterWriter)
                .letterPasscode(letterPasscode)
                .donorName(donorName)
                .donateSeq(donateSeq)
                .letterTitle(letterTitle)
                .letterContents(letterContents)
                .build();

        // memorialRepository.findById() 결과 미리 설정
        String donateTitle = "추모합니다.";
        String contents = "기증자 내용";
        Memorial memorial = Memorial.builder()
                .donateSeq(donateSeq)
                .donorName(donorName)
                .donateTitle(donateTitle)
                .contents(contents)
                .build();


        when(memorialRepository.findById(heavenCreateRequest.getDonateSeq())).thenReturn(Optional.of(memorial));
        ArgumentCaptor<Heaven> heavenCaptor = ArgumentCaptor.forClass(Heaven.class);

        /* when */
        heavenServiceImpl.createHeaven(heavenCreateRequest);

        /* then */
        verify(memorialRepository, times(1)).findById(heavenCreateRequest.getDonateSeq());
        verify(fileService, times(1)).saveFile(heavenCreateRequest.getLetterContents());
        verify(heavenRepository, times(1)).save(heavenCaptor.capture());

        Heaven saveHeaven = heavenCaptor.getValue();
        Memorial saveMemorial = saveHeaven.getMemorial();

        assertNotNull(saveHeaven);
        assertEquals(letterWriter, saveHeaven.getLetterWriter());
        assertEquals(letterPasscode, saveHeaven.getLetterPasscode());
        assertEquals(donorName, saveHeaven.getDonorName());
        assertEquals(letterTitle, saveHeaven.getLetterTitle());
        assertEquals(letterContents, saveHeaven.getLetterContents());

        assertEquals(donorName, saveMemorial.getDonorName());
        assertEquals(donateSeq, saveMemorial.getDonateSeq());
        assertEquals(donateTitle, saveMemorial.getDonateTitle());
        assertEquals(contents, saveMemorial.getContents());
    }

    @Test
    @DisplayName("게시물 수정 테스트")
    public void updateHeavenTest() {
        /* given */
        // 매개변수 값 설정
        int letterSeq = 14;
        String letterWriter = "수정 편지 작성자";
        String donorName = "수정 기증자 명";
        int donateSeq = 29;
        String letterTitle = "수정 편지 제목";
        String letterContents = "수정 편지 내용";
        HeavenUpdateRequest heavenUpdateRequest = HeavenUpdateRequest.builder()
                .letterWriter(letterWriter)
                .donorName(donorName)
                .donateSeq(donateSeq)
                .letterTitle(letterTitle)
                .letterContents(letterContents)
                .build();

        // memorialRepository.findById 결과 설정
        String donateTitle = "추모관 제목";
        String contents = "추모관 내용";
        Memorial memorial = Memorial.builder()
                .donateSeq(donateSeq)
                .donorName(donorName)
                .donateTitle(donateTitle)
                .contents(contents)
                .build();

        // heavenFinder.findByIdOrThrow 결과 설정
        String beforeLetterWriter = "편지 작성자";
        String beforeLetterTitle = "편지 제목";
        String beforeLetterContents = "편지 내용";
        Heaven heaven = Heaven.builder()
                .letterSeq(letterSeq)
                .letterWriter(beforeLetterWriter)
                .letterTitle(beforeLetterTitle)
                .letterContents(beforeLetterContents)
                .build();

        when(heavenFinder.findByIdOrThrow(letterSeq)).thenReturn(heaven);
        when(memorialRepository.findById(donateSeq)).thenReturn(Optional.of(memorial));

        /* when */
        Heaven updateHeaven = heavenServiceImpl.updateHeaven(letterSeq, heavenUpdateRequest);
        Memorial updateMemorial = updateHeaven.getMemorial();

        /* then */
        assertNotNull(updateHeaven);
        assertEquals(letterSeq, updateHeaven.getLetterSeq());
        assertEquals(letterTitle, updateHeaven.getLetterTitle());
        assertEquals(donorName, updateHeaven.getDonorName());
        assertEquals(letterContents, updateHeaven.getLetterContents());

        assertEquals(donateSeq, updateMemorial.getDonateSeq());
        assertEquals(donorName, updateMemorial.getDonorName());
        assertEquals(donateTitle, updateMemorial.getDonateTitle());
        assertEquals(contents, updateMemorial.getContents());
    }

    @Test
    @DisplayName("게시물 삭제 테스트")
    public void deleteHeavenTest() {
        /* given */
        // 매개변수 값 설정
        int letterSeq = 14;
        String letterPasscode = "qwer1234";

        // heavenFinder.findByIdOrThrow() 결과 설정
        Heaven heaven = mock(Heaven.class);

        when(heavenFinder.findByIdOrThrow(letterSeq)).thenReturn(heaven);

        /* when */
        heavenServiceImpl.deleteHeaven(letterSeq, letterPasscode);

        /* then */
        verify(heaven, times(1)).verifyPasscode(letterPasscode);
        verify(heaven, times(1)).softDelete();
    }
}