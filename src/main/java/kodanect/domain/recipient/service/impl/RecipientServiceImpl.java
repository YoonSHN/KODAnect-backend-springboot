package kodanect.domain.recipient.service.impl;

import kodanect.common.config.GlobalsProperties;
import kodanect.common.exception.config.SecureLogger;
import kodanect.common.response.CursorCommentCountPaginationResponse;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.util.CursorFormatter;
import kodanect.domain.recipient.dto.*;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
import kodanect.domain.recipient.repository.RecipientRepository;
import kodanect.domain.recipient.service.RecipientService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Sort;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static kodanect.common.exception.config.MessageKeys.RECIPIENT_NOT_FOUND;

@Service("recipientService")
public class RecipientServiceImpl implements RecipientService {

    // 로거 선언 (가장 먼저)
    private static final SecureLogger logger = SecureLogger.getLogger(RecipientServiceImpl.class);

    // 정적(static) 상수 정의
    private static final String RECIPIENT_NOT_FOUND_MESSAGE = "해당 게시물이 존재하지 않거나 이미 삭제되었습니다.";
    private static final int INITIAL_COMMENT_LOAD_LIMIT = 3; // 초기에 로딩할 댓글의 개수
    private static final String LETTER_SEQ = "letterSeq";
    private static final String DEL_FLAG = "delFlag";
    private static final String COMMENT_SEQ = "commentSeq";
    private static final String WRITE_TIME = "writeTime";

    // 의존성 주입 (final 필드)
    private final String organCodeDirectInput;
    private final String anonymousWriterValue;
    private final RecipientRepository recipientRepository;
    private final RecipientCommentRepository recipientCommentRepository;
    private final GlobalsProperties globalsProperties; // GlobalsProperties 주입

    public RecipientServiceImpl(
            RecipientRepository recipientRepository,
            RecipientCommentRepository recipientCommentRepository,
            GlobalsProperties globalsProperties,
            @Value("${recipient.organ-code-direct-input:ORGAN000}") String organCodeDirectInput,
            @Value("${recipient.anonymous-writer-value:익명}") String anonymousWriterValue) {
        this.recipientRepository = recipientRepository;
        this.recipientCommentRepository = recipientCommentRepository;
        this.globalsProperties = globalsProperties;
        this.organCodeDirectInput = organCodeDirectInput;
        this.anonymousWriterValue = anonymousWriterValue;
    }

    // 게시물 비밀번호 확인
    @Override
    public void verifyLetterPassword(Integer letterSeq, String letterPasscode) {

        // 게시물 조회 (삭제되지 않은 게시물만 조회)
        RecipientEntity recipientEntityold = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag())) // 삭제되지 않은 게시물만 필터링
                .orElseThrow(() -> new RecipientNotFoundException(RECIPIENT_NOT_FOUND, letterSeq));

        // 비밀번호 불일치 (엔티티의 checkPasscode 메서드 활용)
        if (!recipientEntityold.checkPasscode(letterPasscode)) {
            // inputData를 받지 않는 생성자로 예외 발생
            throw new RecipientInvalidPasscodeException(letterSeq); // commentId를 받는 생성자 사용
        }
    }

    // 게시물 수정
    @Override
    public RecipientDetailResponseDto updateRecipient(Integer letterSeq, RecipientRequestDto requestDto) {

        // 1. 게시물 조회 (삭제되지 않은 게시물만 조회)
        RecipientEntity recipientEntityOld = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new RecipientNotFoundException(RECIPIENT_NOT_FOUND, letterSeq));

        String oldContents = recipientEntityOld.getLetterContents(); // 이전 내용
        String validatedAndCleanedContents = cleanAndValidateContents(requestDto.getLetterContents());
        requestDto.setLetterContents(validatedAndCleanedContents);

        // 기존 이미지 정보 파싱
        ImageInfo oldImageInfo = parseImagesFromContents(oldContents);
        // 새로운 이미지 정보 파싱
        ImageInfo newImageInfo = parseImagesFromContents(requestDto.getLetterContents());

        // 엔티티 필드 업데이트 (비밀번호 일치 시에만 진행)
        recipientEntityOld.setOrganCode(requestDto.getOrganCode());
        recipientEntityOld.setOrganEtc(requestDto.getOrganEtc());
        recipientEntityOld.setLetterTitle(requestDto.getLetterTitle());
        recipientEntityOld.setRecipientYear(requestDto.getRecipientYear());
        recipientEntityOld.setLetterContents(validatedAndCleanedContents);

        // 이미지 필드 업데이트
        recipientEntityOld.setImageUrl(String.join(",", newImageInfo.imageUrls));
        recipientEntityOld.setFileName(String.join(",", newImageInfo.fileNames));
        recipientEntityOld.setOrgFileName(String.join(",", newImageInfo.orgFileNames));

        // organCode 및 organEtc 로직 분리
        handleOrganCodeAndEtc(recipientEntityOld, requestDto); // 별도 메서드로 분리

        RecipientEntity updatedEntity = recipientRepository.save(recipientEntityOld); // 변경사항 저장

        // 변경된 이미지 파일 처리: 삭제된 이미지 파일은 물리적으로 삭제
        handleImageFilesDeletion(oldImageInfo.fileNames, newImageInfo.fileNames);

        logger.info("게시물 성공적으로 수정됨: letterSeq={}", updatedEntity.getLetterSeq());
        return RecipientDetailResponseDto.fromEntity(updatedEntity, globalsProperties.getFileBaseUrl()); // DTO로 변환하여 반환
    }

    // 게시물 삭제
    // 조건 : 등록된 게시물의 비밀번호와 일치하는 경우
    @Override
    public void deleteRecipient(Integer letterSeq, String letterPasscode) {
        logger.info("게시물 삭제 시도: letterSeq={}", letterSeq);

        // 게시물 조회 (삭제되지 않은 게시물만 조회)
        RecipientEntity recipientEntityold = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new RecipientNotFoundException(RECIPIENT_NOT_FOUND, letterSeq));

        // 게시물 비밀번호 검증
        if (!recipientEntityold.checkPasscode(letterPasscode)) {
            throw new RecipientInvalidPasscodeException(letterSeq);
        }

        // 게시물 소프트 삭제
        recipientEntityold.softDelete();
        recipientRepository.save(recipientEntityold);

        // 해당 게시물의 모든 댓글 소프트 삭제
        List<RecipientCommentEntity> commentsToSoftDelete =
                recipientCommentRepository.findCommentsByLetterSeqAndDelFlagSorted(recipientEntityold, "N");

        if (commentsToSoftDelete != null && !commentsToSoftDelete.isEmpty()) {
            for (RecipientCommentEntity comment : commentsToSoftDelete) {
                comment.setDelFlag("Y"); // 댓글의 delflag를 'Y'로 변경
                recipientCommentRepository.save(comment);
            }
        }
        logger.info("게시물 성공적으로 삭제됨: letterSeq={}", letterSeq);
    }

    // 게시물 등록
    @Override
    public RecipientDetailResponseDto insertRecipient(RecipientRequestDto requestDto) {

        String validatedAndCleanedContents = cleanAndValidateContents(requestDto.getLetterContents());
        requestDto.setLetterContents(validatedAndCleanedContents);

        // 이미지 정보 파싱
        ImageInfo parsedImageInfo = parseImagesFromContents(requestDto.getLetterContents());

        RecipientEntity recipientEntityRequest = requestDto.toEntity();
        recipientEntityRequest.setImageUrl(String.join(",", parsedImageInfo.imageUrls));
        recipientEntityRequest.setFileName(String.join(",", parsedImageInfo.fileNames));
        recipientEntityRequest.setOrgFileName(String.join(",", parsedImageInfo.orgFileNames));

        handleOrganCodeAndEtc(recipientEntityRequest, requestDto);

        RecipientEntity savedEntity = recipientRepository.save(recipientEntityRequest);
        return RecipientDetailResponseDto.fromEntity(savedEntity, globalsProperties.getFileBaseUrl());
    }

    // 특정 게시물 조회
    @Transactional
    @Override
    public RecipientDetailResponseDto selectRecipient(Integer letterSeq) {
        // 1. 해당 게시물 조회 (삭제되지 않은 게시물만 조회하도록 필터링)
        RecipientEntity recipientEntity = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new RecipientNotFoundException(RECIPIENT_NOT_FOUND, letterSeq));

        // 2. 조회수 증가
        recipientEntity.incrementReadCount();
        recipientRepository.save(recipientEntity); // 조회수 업데이트

        // 3. Entity를 RecipientDetailResponseDto 변환 (댓글 포함)
        RecipientDetailResponseDto responseDto = RecipientDetailResponseDto.fromEntity(recipientEntity, globalsProperties.getFileBaseUrl());

        // 4. 상위 INITIAL_COMMENT_LOAD_LIMIT 개 댓글 조회
        // lastCommentId는 첫 조회이므로 0 (또는 null), size는 INITIAL_COMMENT_LOAD_LIMIT + 1 (다음 커서 확인용)
        Pageable commentPageable = PageRequest.of(0, INITIAL_COMMENT_LOAD_LIMIT + 1, // +1 하여 다음 커서 존재 여부 확인
                Sort.by(Sort.Direction.DESC, COMMENT_SEQ)); // COMMENT_SEQ 상수가 정의되어 있다고 가정

        List<RecipientCommentEntity> initialComments = recipientCommentRepository.findPaginatedComments(
                recipientEntity, // letterSeq 대신 RecipientEntity 객체를 전달 (RecipientCommentRepository 확인)
                0, // lastCommentId는 첫 조회이므로 0
                commentPageable // Pageable을 사용하여 LIMIT 적용
        );
        logger.info("조회된 초기 댓글 수: {}", initialComments.size());
        initialComments.forEach(c -> logger.info("commentSeq={}, delFlag={}", c.getCommentSeq(), c.getDelFlag()));

        // 5. 초기 댓글 Entity를 DTO로 변환
        List<RecipientCommentResponseDto> initialCommentDtos = initialComments.stream()
                .map(RecipientCommentResponseDto::fromEntity)
                .toList();

        // 8. 게시물 전체 댓글 수 설정 <--- 이 부분이 제대로 실행되어야 합니다.
        long totalCommentCount = recipientCommentRepository.countActiveCommentsByLetterSeq(letterSeq);

        // 6. CursorFormatter를 사용하여 댓글 응답 포맷 생성
        CursorCommentCountPaginationResponse<RecipientCommentResponseDto, Integer> commentPaginationResponse =
                CursorFormatter.cursorCommentCountFormat(initialCommentDtos, INITIAL_COMMENT_LOAD_LIMIT, totalCommentCount); // 실제 클라이언트 요청 size는 INITIAL_COMMENT_LOAD_LIMIT

        // 7. DTO에 댓글 관련 데이터 설정
        responseDto.setInitialCommentData(commentPaginationResponse);



        return responseDto;
    }

    /**
     * 게시물 목록 조회 (검색 및 커서 기반 페이징으로 변경)
     * @param searchCondition 검색 조건 (searchType, searchKeyword)
     * @param cursor "더 보기" 기능을 위한 마지막 게시물 ID (null 또는 0이면 첫 페이지 조회)
     * @param size 한 번에 가져올 게시물 수
     * @return 커서 기반 페이지네이션 응답 (게시물)
     */
    @Override
    public CursorPaginationResponse<RecipientListResponseDto, Integer> selectRecipientList(
            RecipientSearchCondition searchCondition,
            Integer cursor,
            int size) {

        // 1. 쿼리할 데이터의 실제 size (클라이언트 요청 size + 1 하여 다음 커서 존재 여부 확인)
        int querySize = size + 1;

        // 2. 기본 Specification 생성 (검색 조건 적용)
        Specification<RecipientEntity> spec = getRecipientSpecification(searchCondition);

        // 3. "더 보기" 기능 (cursor) 조건 추가: letterSeq 기준 내림차순이므로 cursor보다 작은 것 조회
        if (cursor != null && cursor > 0) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get(LETTER_SEQ), cursor));
        }

        // 4. 정렬 조건 설정 (letterSeq 기준 내림차순 - 최신 게시물부터)
        Sort sort = Sort.by(Sort.Direction.DESC, LETTER_SEQ);

        // 5. Pageable 설정 (offset은 항상 0, limit은 querySize)
        Pageable pageable = PageRequest.of(0, querySize, sort);

        // 6. 게시물 조회
        List<RecipientEntity> recipientList = recipientRepository.findAll(spec, pageable).getContent(); // Page 객체에서 List 추출

        // 7. RecipientEntity를 RecipientListResponseDto로 변환
        List<RecipientListResponseDto> recipientResponseDtos = new ArrayList<>();

        for (int i = 0; i < recipientList.size(); i++) {
            RecipientEntity entity = recipientList.get(i);
            RecipientListResponseDto dto = RecipientListResponseDto.fromEntity(entity, anonymousWriterValue);

            recipientResponseDtos.add(dto);
        }

        // 8. 검색 조건에 맞는 전체 게시물 총 개수 조회
        Integer totalCount = (int) recipientRepository.count(getRecipientSpecification(searchCondition)
                .and((root, query, cb) -> cb.equal(root.get(DEL_FLAG), "N")) // 전체 개수 셀 때도 delFlag 조건 추가
        );

        // 9. CursorFormatter 사용하여 응답 포맷팅
        return CursorFormatter.<RecipientListResponseDto, Integer> cursorFormat(recipientResponseDtos, size, totalCount);
    }

    /**
     * 게시물 검색 결과 총 개수 조회 (검색 조건 적용)
     */
    @Override
    public int selectRecipientListTotCnt(RecipientSearchCondition searchCondition) {
        Specification<RecipientEntity> spec = getRecipientSpecification(searchCondition);
        return (int) recipientRepository.count(spec);
    }

    /**
     * RecipientSearchCondition에 따라 동적인 Specification 생성
     * (검색 타입: 제목, 내용, ALL만 지원)
     */
    private Specification<RecipientEntity> getRecipientSpecification(RecipientSearchCondition searchCondition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 삭제되지 않은 게시물만 조회 (기본 조건)
            predicates.add(cb.equal(root.get(DEL_FLAG), "N"));

            // 검색어 (searchKeyword)가 있고 검색 타입 (searchType)이 있는 경우
            String keyword = searchCondition.getKeyWord();
            SearchType type = searchCondition.getType();

            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%"; // 대소문자 무시 검색

                if (type == null || type == SearchType.ALL) { // 검색 타입이 없거나 'ALL'인 경우 (제목+내용)
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("letterTitle")), likeKeyword), // 제목 검색
                            cb.like(cb.lower(root.get("letterContents")), likeKeyword)  // 내용 검색
                    ));
                } else if (type == SearchType.TITLE) { // 제목만 검색
                    predicates.add(cb.like(cb.lower(root.get("letterTitle")), likeKeyword));
                } else if (type == SearchType.CONTENTS) { // 내용만 검색
                    predicates.add(cb.like(cb.lower(root.get("letterContents")), likeKeyword));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Jsoup을 사용하여 HTML 내용을 정제하고 유효성을 검사하는 공통 메서드.
     * @param originalContents 원본 HTML 내용.
     * @return 정제되고 트림된 내용.
     * @throws RecipientInvalidDataException 내용이 null이거나 비어있거나, 필터링 후 비어있을 경우 발생.
     */
    private String cleanAndValidateContents(String originalContents) {
        // 1. 초기 null 또는 공백 검사 (가장 먼저 수행)
        if (originalContents == null || originalContents.trim().isEmpty()) {
            logger.warn("게시물 내용이 비어있거나 null입니다.");
            throw new RecipientInvalidDataException("게시물 내용은 필수 입력 항목입니다.");
        }

        // Jsoup 필터링 전에 불필요한 이스케이프 문자(\\)를 제거
        String processedContents = originalContents.replace("\\\"", "\"");

        // 2. Jsoup을 사용하여 HTML 필터링
        Safelist safelist = Safelist.relaxed()
                .addTags("img")
                .addAttributes("img", "src", "data-cke-saved-src", "style", "width", "height");
        String cleanContents = Jsoup.clean(processedContents, safelist);

        // 3. 필터링된 HTML에서 순수 텍스트 추출 후 최종 유효성 검사
        String pureTextContents = Jsoup.parse(cleanContents).text();
        if (pureTextContents.trim().isEmpty()) {
            logger.warn("게시물 작업 실패: 필터링 후 내용이 비어있음");
            throw new RecipientInvalidDataException("게시물 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");
        }
        // 최종적으로 정제된 HTML 내용 반환
        return cleanContents.trim();
    }

    /**
     * CKEditor 내용에서 이미지 URL, 파일명, 원본 파일명을 파싱합니다.
     * @param contents 게시물 내용 (HTML)
     * @return 파싱된 이미지 정보 (URL, 파일명, 원본 파일명 리스트)
     */
    private ImageInfo parseImagesFromContents(String contents) {
        List<String> imageUrls = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        List<String> orgFileNames = new ArrayList<>();

        if (contents == null || contents.isBlank()) {
            return new ImageInfo(imageUrls, fileNames, orgFileNames);
        }

        Document doc = Jsoup.parse(contents);
        Elements imgTags = doc.body().select("img[src], img[data-cke-saved-src]");

        for (Element img : imgTags) {
            String src = img.attr("src");
            String dataCkeSavedSrc = img.attr("data-cke-saved-src");
            String finalSrc = StringUtils.hasText(dataCkeSavedSrc) ? dataCkeSavedSrc : src;

            if (StringUtils.hasText(finalSrc)) {
                imageUrls.add(finalSrc);

                // URL에서 파일명 추출 (도메인/경로 제거 후)
                String fileNameWithExt = finalSrc.substring(finalSrc.lastIndexOf("/") + 1);
                fileNames.add(fileNameWithExt);
                orgFileNames.add(fileNameWithExt);
            }
        }
        return new ImageInfo(imageUrls, fileNames, orgFileNames);
    }

    // 이미지 정보 저장을 위한 내부 클래스
    private static class ImageInfo {
        List<String> imageUrls;
        List<String> fileNames;
        List<String> orgFileNames;

        public ImageInfo(List<String> imageUrls, List<String> fileNames, List<String> orgFileNames) {
            this.imageUrls = imageUrls;
            this.fileNames = fileNames;
            this.orgFileNames = orgFileNames;
        }
    }

    /**
     * 이전 콘텐츠에 있었으나 현재 콘텐츠에 없는 이미지 파일을 물리적으로 삭제합니다.
     * @param oldFileNames 이전 게시물에 있었던 이미지 파일명 리스트
     * @param newFileNames 현재 게시물에 있는 이미지 파일명 리스트
     */
    private void handleImageFilesDeletion(List<String> oldFileNames, List<String> newFileNames) {
        logger.info("handleImageFilesDeletion 시작 - 기존 파일: {}, 새 파일: {}", oldFileNames, newFileNames);
        if (oldFileNames == null || oldFileNames.isEmpty()) {
            logger.info("기존 파일이 없어 삭제할 파일 없음.");
            return; // 이전 파일이 없으면 삭제할 것도 없음
        }

        // 이전 파일명 리스트에서 현재 파일명 리스트에 없는 파일들을 찾습니다.
        Set<String> newFileNamesSet = new HashSet<>(newFileNames);

        List<String> filesToDelete = oldFileNames.stream()
                .filter(fileName -> !newFileNamesSet.contains(fileName))
                .toList(); // Java 16 이상에서 사용 가능

        logger.info("삭제할 파일 목록: {}", filesToDelete);

        for (String fileName : filesToDelete) {
            deleteExistingFile(fileName);
        }
        logger.info("handleImageFilesDeletion 종료.");
    }

    /**
     * organCode와 organEtc 필드 관련 로직을 처리합니다.
     * @param entity 게시물 엔티티
     * @param requestDto 요청 DTO
     */
    private void handleOrganCodeAndEtc(RecipientEntity entity, RecipientRequestDto requestDto) {
        // organCode : "ORGAN000" (직접입력) 일 경우 organEtc 설정, 아니면 null
        if (!organCodeDirectInput.equals(requestDto.getOrganCode())) {
            entity.setOrganEtc(null);
        } else {
            if (requestDto.getOrganEtc() == null || requestDto.getOrganEtc().trim().isEmpty()) {
                logger.warn("ORGAN000 선택 시 organEtc는 필수 입력 항목입니다.");
                throw new RecipientInvalidDataException("ORGAN000 선택 시 organEtc는 필수 입력 항목입니다.");
            }
            entity.setOrganEtc(requestDto.getOrganEtc());
        }
    }

    /**
     * 물리 파일을 삭제합니다.
     * @param fileName 삭제할 파일명
     */
    private void deleteExistingFile(String fileName) {
        try {
            String storePath = globalsProperties.getFileStorePath();

            Path filePath = Paths.get(storePath).toAbsolutePath().normalize();

            Path fullPath = filePath.resolve("upload_img").resolve(fileName);

            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                logger.info("기존 이미지 파일 삭제 성공: {}", fullPath);
            } else {
                logger.warn("삭제하려는 파일이 존재하지 않습니다: {}", fullPath);
            }
        } catch (IOException e) {
            logger.error("기존 이미지 파일 삭제 중 오류 발생: {}", fileName, e);
        }
    }

}

