package kodanect.domain.heaven.service.impl;

import kodanect.common.response.CursorCommentCountPaginationResponse;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.util.CursorFormatter;
import kodanect.common.util.HeavenFinder;
import kodanect.common.util.MemorialFinder;
import kodanect.common.validation.HeavenValidator;
import kodanect.domain.heaven.dto.HeavenDto;
import kodanect.domain.heaven.dto.request.HeavenCreateRequest;
import kodanect.domain.heaven.dto.request.HeavenUpdateRequest;
import kodanect.domain.heaven.dto.response.HeavenCommentResponse;
import kodanect.domain.heaven.dto.response.HeavenDetailResponse;
import kodanect.domain.heaven.dto.response.HeavenResponse;
import kodanect.domain.heaven.dto.response.MemorialHeavenResponse;
import kodanect.domain.heaven.entity.Heaven;
import kodanect.domain.heaven.exception.InvalidTypeException;
import kodanect.domain.heaven.repository.HeavenCommentRepository;
import kodanect.domain.heaven.repository.HeavenRepository;
import kodanect.domain.heaven.service.FileService;
import kodanect.domain.heaven.service.HeavenCommentService;
import kodanect.domain.heaven.service.HeavenService;
import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.repository.MemorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HeavenServiceImpl implements HeavenService {

    /* 기본값 */
    private static final int COMMENT_SIZE = 3;
    private static final String FILE_NAME_KEY = "fileName";
    private static final String ORG_FILE_NAME_KEY = "orgFileName";

    private final HeavenRepository heavenRepository;
    private final HeavenCommentRepository heavenCommentRepository;
    private final HeavenCommentService heavenCommentService;
    private final MemorialRepository memorialRepository;
    private final HeavenFinder heavenFinder;
    private final MemorialFinder memorialFinder;
    private final FileService fileService;

    /* 게시물 전체 조회 (페이징) */
    @Override
    public CursorPaginationResponse<HeavenResponse, Integer> getHeavenList(Integer cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);

        List<HeavenResponse> heavenResponseList = heavenRepository.findByCursor(cursor, pageable);

        long count = heavenRepository.countByDelFlag();

        return CursorFormatter.cursorFormat(heavenResponseList, size, count);
    }

    /* 검색을 통한 게시물 전체 조회 (페이징) */
    @Override
    public CursorPaginationResponse<HeavenResponse, Integer> getHeavenListSearchResult(String type, String keyWord, Integer cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);

        long count = countByType(type, keyWord);

        List<HeavenResponse> heavenResponseList = findByType(type, keyWord, cursor, pageable);

        return CursorFormatter.cursorFormat(heavenResponseList, size, count);
    }

    /* 게시물 상세 조회 */
    @Transactional(value = Transactional.TxType.NOT_SUPPORTED)
    @Override
    public HeavenDetailResponse getHeavenDetail(Integer letterSeq) {
        /* 게시물 상세 조회 */
        HeavenDto heavenDto = heavenFinder.findAnonymizedByIdOrThrow(letterSeq);

        /* 조회수 증가 */
        heavenRepository.updateReadCount(letterSeq);

        /* 댓글 리스트 조회 */
        List<HeavenCommentResponse> heavenCommentList = heavenCommentService.getHeavenCommentList(letterSeq, null, COMMENT_SIZE + 1);

        /* 댓글 개수 조회 */
        long commentCount = heavenCommentRepository.countByLetterSeq(letterSeq);

        /* 파일 조회 */
        String imageUrl = fileService.getFile(heavenDto.getFileName());

        CursorCommentCountPaginationResponse<HeavenCommentResponse, Integer> cursorCommentCountPaginationResponse =
                CursorFormatter.cursorCommentCountFormat(heavenCommentList, COMMENT_SIZE, commentCount);

        return HeavenDetailResponse.of(heavenDto, cursorCommentCountPaginationResponse, imageUrl);
    }

    /* 기증자 추모관 상세 조회 시 하늘나라 편지 전체 조회 */
    @Override
    public CursorPaginationResponse<MemorialHeavenResponse, Integer> getMemorialHeavenList(Integer donateSeq, Integer cursor, int size) {
        Memorial memorial = memorialFinder.findByIdOrThrow(donateSeq);

        Pageable pageable = PageRequest.of(0, size + 1);

        List<MemorialHeavenResponse> memorialHeavenResponseList = heavenRepository.findMemorialHeavenResponseById(memorial, cursor, pageable);

        long count = heavenRepository.countByMemorial(memorial);

        return CursorFormatter.cursorFormat(memorialHeavenResponseList, size, count);
    }

    /* 게시물 생성 */
    @Override
    public void createHeaven(HeavenCreateRequest heavenCreateRequest) {
        Memorial memorial = memorialRepository.findById(heavenCreateRequest.getDonateSeq()).orElse(null);

        /* memorial과 Request DonorName 유효성 검사 */
        HeavenValidator.validateDonorNameMatches(heavenCreateRequest.getDonorName(), memorial);

        /* 파일 생성 */
        Map<String, String> fileMap = fileService.saveFile(heavenCreateRequest.getFile());
        String fileName = fileMap.get(FILE_NAME_KEY);
        String orgFileName = fileMap.get(ORG_FILE_NAME_KEY);

        Heaven heaven = Heaven.builder()
                .memorial(memorial)
                .letterTitle(heavenCreateRequest.getLetterTitle())
                .donorName(heavenCreateRequest.getDonorName())
                .letterPasscode(heavenCreateRequest.getLetterPasscode())
                .letterWriter(heavenCreateRequest.getLetterWriter())
                .anonymityFlag(heavenCreateRequest.getAnonymityFlag())
                .readCount(0)
                .letterContents(heavenCreateRequest.getLetterContents())
                .fileName(fileName)
                .orgFileName(orgFileName)
                .build();

        heavenRepository.save(heaven);
    }

    /* 게시물 수정 인증 */
    @Override
    public void verifyHeavenPasscode(Integer letterSeq, String letterPasscode) {
        Heaven heaven = heavenFinder.findByIdOrThrow(letterSeq);

        heaven.verifyPasscode(letterPasscode);
    }

    /* 게시물 수정 */
    @Override
    public void updateHeaven(Integer letterSeq, HeavenUpdateRequest heavenUpdateRequest) {
        Heaven heaven = heavenFinder.findByIdOrThrow(letterSeq);
        Memorial memorial = memorialRepository.findById(heavenUpdateRequest.getDonateSeq()).orElse(null);

        /* 유효성 검사 */
        HeavenValidator.validateDonorNameMatches(heavenUpdateRequest.getDonorName(), memorial);

        Map<String, String> fileMap = fileService.updateFile(heavenUpdateRequest.getFile(), heaven.getFileName());

        heaven.updateHeaven(heavenUpdateRequest, memorial, fileMap);
    }

    /* 게시물 삭제 */
    @Override
    public void deleteHeaven(Integer letterSeq, String letterPasscode) {
        Heaven heaven = heavenFinder.findByIdOrThrow(letterSeq);

        /* 비밀번호 일치 검증 */
        heaven.verifyPasscode(letterPasscode);

        /* 파일 삭제 */
        fileService.deleteFile(heaven.getFileName());

        /* 게시물 및 해당 댓글 소프트 삭제 */
        heaven.softDelete();
    }

    /* 검색 조건에 따른 게시물 개수 조회 */
    private long countByType(String type, String keyWord) {
        return switch (type) {
            case "ALL"    -> heavenRepository.countByTitleOrContentsContaining(keyWord);
            case "TITLE"  -> heavenRepository.countByTitleContaining(keyWord);
            case "CONTENTS"-> heavenRepository.countByContentsContaining(keyWord);
            default       -> throw new InvalidTypeException(type);
        };
    }

    /* 검색 조건에 따른 게시물 조회 */
    private List<HeavenResponse> findByType(String type, String keyWord, Integer cursor, Pageable pageable) {
        return switch (type) {
            case "ALL"    -> heavenRepository.findByTitleOrContentsContaining(keyWord, cursor, pageable);
            case "TITLE"  -> heavenRepository.findByTitleContaining(keyWord, cursor, pageable);
            case "CONTENTS"-> heavenRepository.findByContentsContaining(keyWord, cursor, pageable);
            default       -> throw new InvalidTypeException(type);
        };
    }
}
