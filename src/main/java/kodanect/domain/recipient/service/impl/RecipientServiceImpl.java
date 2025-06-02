package kodanect.domain.recipient.service.impl;

import kodanect.domain.recipient.dto.*;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import kodanect.common.exception.custom.InvalidIntegerConversionException;
import kodanect.common.util.HcaptchaService;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
import kodanect.domain.recipient.repository.RecipientRepository;
import kodanect.domain.recipient.service.RecipientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Sort;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service("recipientService")
public class RecipientServiceImpl implements RecipientService {
    // 상수 정의
    private static final String ORGAN_CODE_DIRECT_INPUT = "ORGAN000";  // 직접입력 코드
    private static final String ANONYMOUS_WRITER_VALUE = "익명";
    private static final String CAPTCHA_FAILED_MESSAGE = "캡차 인증에 실패했습니다. 다시 시도해주세요.";
    private static final String RECIPIENT_NOT_FOUND_MESSAGE = "해당 게시물이 존재하지 않거나 이미 삭제되었습니다.";
    private static final int INITIAL_COMMENT_LOAD_LIMIT = 3; // 초기에 로딩할 댓글의 개수
    private static final String LETTER_SEQ = "letterSeq";
    private static final String DEL_FLAG = "delFlag";
    private static final String COMMENT_SEQ = "commentSeq";
    private static final String WRITE_TIME = "writeTime";

    private final RecipientRepository recipientRepository;
    private final RecipientCommentRepository recipientCommentRepository;
    private final HcaptchaService hcaptchaService; // hCaptchaService 주입

    // application.properties의 file.upload-root-dir 경로 주입
    @Value("${file.upload-root-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String fileBaseUrl;

    // 로거 선언
    private final Logger logger = LoggerFactory.getLogger(RecipientServiceImpl.class);

    public RecipientServiceImpl(RecipientRepository recipientRepository, RecipientCommentRepository recipientCommentRepository, HcaptchaService hcaptchaService) {
        this.recipientRepository = recipientRepository;
        this.recipientCommentRepository = recipientCommentRepository;
        this.hcaptchaService = hcaptchaService; // hCaptchaService 초기화
    }


    // 게시물 비밀번호 확인
    @Override
    public boolean verifyLetterPassword(Integer letterSeq, String letterPasscode) {

        // 게시물 조회 (삭제되지 않은 게시물만 조회)
        RecipientEntity recipientEntityold = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag())) // 삭제되지 않은 게시물만 필터링
                .orElseThrow(() -> new RecipientNotFoundException(RECIPIENT_NOT_FOUND_MESSAGE));

        // 비밀번호 불일치 (엔티티의 checkPasscode 메서드 활용)
        if (!recipientEntityold.checkPasscode(letterPasscode)) {
            throw new RecipientInvalidPasscodeException("비밀번호가 일치하지 않습니다.");
        }
        return true;
    }

    // 게시물 수정
    @Override
    public RecipientDetailResponseDto updateRecipient(Integer letterSeq, String requestPasscode, RecipientRequestDto requestDto) {
        // --- 0. hCaptcha 인증 검증 추가 ---
        if (!hcaptchaService.verifyCaptcha(requestDto.getCaptchaToken())) {
            logger.warn("hCaptcha 인증 실패: 유효하지 않은 캡차 토큰입니다. (수정)");
            throw new RecipientInvalidDataException(CAPTCHA_FAILED_MESSAGE);
        }
        logger.info("hCaptcha 인증 성공. 게시물 수정 진행.");

        // 게시물 조회 (삭제되지 않은 게시물만 조회)
        RecipientEntity recipientEntityold = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new RecipientNotFoundException(RECIPIENT_NOT_FOUND_MESSAGE));

        // 비밀번호 검증 (verifyLetterPassword 메서드 활용)
        verifyLetterPassword(letterSeq, requestPasscode); // 비밀번호 불일치 시 예외 발생

        // 엔티티 필드 업데이트
        recipientEntityold.setOrganCode(requestDto.getOrganCode());
        recipientEntityold.setOrganEtc(requestDto.getOrganEtc());
        recipientEntityold.setLetterTitle(requestDto.getLetterTitle());
        recipientEntityold.setRecipientYear(requestDto.getRecipientYear());

        // --- 익명 처리 로직 추가 시작 ---
        String writerToSave;
        if ("Y".equalsIgnoreCase(requestDto.getAnonymityFlag())) {
            writerToSave = ANONYMOUS_WRITER_VALUE;
        }
        else {
            writerToSave = requestDto.getLetterWriter();
        }
        recipientEntityold.setLetterWriter(writerToSave);

        // 비밀번호는 수정 시 변경될 수 있으므로, 요청 DTO에 새로운 비밀번호가 있다면 업데이트
        if (requestDto.getLetterPasscode() != null && !requestDto.getLetterPasscode().isEmpty()) {
            recipientEntityold.setLetterPasscode(requestDto.getLetterPasscode());
        }
        recipientEntityold.setAnonymityFlag(requestDto.getAnonymityFlag());

        // 내용(HTML) 필터링
        String originalContents = requestDto.getLetterContents();
        if (originalContents == null || originalContents.trim().isEmpty()) {
            logger.warn("게시물 내용이 비어있거나 null입니다.");
            throw new RecipientInvalidDataException("게시물 내용은 필수 입력 항목입니다.");
        }
        Safelist safelist = Safelist.relaxed();
        String cleanContents = Jsoup.clean(originalContents, safelist);
        String pureTextContents = Jsoup.parse(cleanContents).text();
        if (pureTextContents.trim().isEmpty()) {
            logger.warn("게시물 수정 실패: 필터링 후 내용이 비어있음");
            throw new RecipientInvalidDataException("게시물 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");
        }
        recipientEntityold.setLetterContents(cleanContents.trim());

        // --- 파일 업로드 및 교체 로직 추가 ---
        MultipartFile newImageFile = requestDto.getImageFile();
        if (newImageFile != null && !newImageFile.isEmpty()) {
            // 새로운 파일이 업로드된 경우: 기존 파일 삭제 후 새 파일 저장
            try {
                // 기존 파일이 있다면 삭제
                if (recipientEntityold.getFileName() != null && !recipientEntityold.getFileName().isEmpty()) {
                    String oldFileName = recipientEntityold.getFileName();
                    // URL에서 파일명만 추출하여 물리적 경로 구성
                    String oldFilePhysicalName = oldFileName.substring(oldFileName.lastIndexOf("/") + 1);
                    Path oldFilePath = Paths.get(uploadDir, oldFilePhysicalName).toAbsolutePath().normalize();
                    try {
                        Files.deleteIfExists(oldFilePath);
                        logger.info("기존 이미지 파일 삭제 성공: {}", oldFilePath.toString());
                    }
                    catch (IOException e) {
                        logger.warn("기존 이미지 파일 삭제 실패 (파일 없음 또는 권한 문제): {}", oldFilePath.toString(), e);
                        // 삭제 실패해도 진행은 가능하도록 (치명적 오류는 아님)
                    }
                }

                // 새 파일 저장 로직 (insertRecipient와 동일)
                String originalFilename = newImageFile.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                Path fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
                Files.createDirectories(fileStoragePath); // 디렉토리 생성

                Path targetLocation = fileStoragePath.resolve(uniqueFileName);
                Files.copy(newImageFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                logger.info("새 이미지 파일 저장 성공: {}", targetLocation.toString());

                // 엔티티에 새 파일 정보 업데이트
                String newImageUrl = fileBaseUrl + "/" + uniqueFileName;
                recipientEntityold.setFileName(newImageUrl);
                recipientEntityold.setOrgFileName(originalFilename);
            }
            catch (IOException ex) {
                logger.error("이미지 파일 저장 또는 삭제 실패: {}", ex.getMessage());
                throw new RecipientInvalidDataException("이미지 파일 처리 중 오류가 발생했습니다.");
            }
        }

        RecipientEntity updatedEntity = recipientRepository.save(recipientEntityold); // 변경사항 저장
        logger.info("게시물 성공적으로 수정됨: letterSeq={}", updatedEntity.getLetterSeq());
        return RecipientDetailResponseDto.fromEntity(updatedEntity); // DTO로 변환하여 반환
    }

    // 게시물 삭제
    // 조건 : 등록된 게시물의 비밀번호와 일치하는 경우
    @Override
    public void deleteRecipient(Integer letterSeq, String letterPasscode, String captchaToken) {
        logger.info("게시물 삭제 시도: letterSeq={}", letterSeq);

        // --- 0. hCaptcha 인증 검증 추가 ---
        if (!hcaptchaService.verifyCaptcha(captchaToken)) {
            logger.warn("hCaptcha 인증 실패: 유효하지 않은 캡차 토큰입니다. (삭제)");
            throw new RecipientInvalidDataException(CAPTCHA_FAILED_MESSAGE);
        }
        logger.info("hCaptcha 인증 성공. 게시물 삭제 진행.");

        // 게시물 조회 (삭제되지 않은 게시물만 조회)
        RecipientEntity recipientEntityold = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new RecipientNotFoundException(RECIPIENT_NOT_FOUND_MESSAGE));

        // 게시물 비번이 없거나 or 비밀번호 불일치
        if (!recipientEntityold.checkPasscode(letterPasscode)) { // 엔티티의 checkPasscode 활용
            throw new RecipientInvalidPasscodeException("비밀번호가 일치하지 않습니다.");
        }

        // 2. 게시물 소프트 삭제
        recipientEntityold.softDelete(); // 엔티티의 softDelete 메서드 활용
        recipientRepository.save(recipientEntityold);

        // 3. 해당 게시물의 모든 댓글 소프트 삭제
        List<RecipientCommentEntity> commentsToSoftDelete =
                recipientCommentRepository.findByLetterSeqAndDelFlagOrderByWriteTimeAsc(recipientEntityold, "N");

        if (commentsToSoftDelete != null && !commentsToSoftDelete.isEmpty()) {
            for (RecipientCommentEntity comment : commentsToSoftDelete) {
                comment.setDelFlag("Y"); // 댓글의 delflag를 'Y'로 변경
                recipientCommentRepository.save(comment);
            }
        }
        logger.info("게시물 성공적으로 삭제됨: letterSeq={}", letterSeq);
    }

    // 게시물 등록
    // 조건 : letter_writer 한영자 10자 제한, letter_passcode 영숫자 8자 이상, 캡챠 인증
    @Override
    public RecipientDetailResponseDto insertRecipient(RecipientRequestDto requestDto) {
        // 0. hCaptcha 인증 검증 추가
        if (!hcaptchaService.verifyCaptcha(requestDto.getCaptchaToken())) {
            logger.warn("hCaptcha 인증 실패: 유효하지 않은 캡차 토큰입니다.");
            throw new RecipientInvalidDataException(CAPTCHA_FAILED_MESSAGE);
        }
        logger.info("hCaptcha 인증 성공. 게시물 등록 진행.");

        RecipientEntity recipientEntityRequest = requestDto.toEntity(); // DTO를 Entity로 변환

        // 1. 첨부파일 등록 관련
        MultipartFile imageFile = requestDto.getImageFile();
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // 1. 파일명 생성 (고유한 UUID 사용)
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                // 2. 파일 저장 경로 생성
                Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize(); // /app/uploads 경로가 됨
                Files.createDirectories(uploadPath);

                // 3. 파일 저장
                Path targetLocation = uploadPath.resolve(uniqueFileName);
                Files.copy(imageFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                logger.info("이미지 파일 저장 성공: {}", targetLocation.toString());

                // 4. 엔티티에 파일 정보 저장 _ 클라이언트에서 접근할 수 있는 URL로 변환하여 저장
                String imageUrl = fileBaseUrl + "/" + uniqueFileName; // /uploads/고유파일명 이 됨
                recipientEntityRequest.setFileName(imageUrl);
                recipientEntityRequest.setOrgFileName(originalFilename);
            } catch (IOException ex) {
                logger.error("이미지 파일 저장 실패: {}", ex.getMessage());
                throw new RecipientInvalidDataException("이미지 파일 저장 중 오류가 발생했습니다.");
            }
        }

        // 2. Jsoup을 사용하여 HTML 필터링 및 내용 유효성 검사
        String originalContents = recipientEntityRequest.getLetterContents();

        // letterContents가 null이거나 비어있는 경우 InvalidRecipientDataException 발생
        if (originalContents == null || originalContents.trim().isEmpty()) {
            logger.warn("게시물 내용이 비어있거나 null입니다."); // 로깅 추가
            throw new RecipientInvalidDataException("게시물 내용은 필수 입력 항목입니다.");
        }
        Safelist safelist = Safelist.relaxed();
        String cleanContents = Jsoup.clean(originalContents, safelist);
        logger.debug("Cleaned contents before trim: '{}'", cleanContents); // 디버깅용
        // 필터링 후 HTML 태그를 포함한 내용이 아니라, 순수 텍스트 내용이 비어있는지 확인
        String pureTextContents = Jsoup.parse(cleanContents).text();
        if (pureTextContents.trim().isEmpty()) { // 필터링 후 내용이 실질적으로 비어있는지 다시 확인
            logger.warn("게시물 작성 실패: 필터링 후 내용이 비어있음"); // 로깅 추가
            throw new RecipientInvalidDataException("게시물 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");
        }
        recipientEntityRequest.setLetterContents(cleanContents.trim()); // 필터링되고 트림된 내용으로 설정
        logger.debug("Cleaned contents after trim: '{}'", recipientEntityRequest.getLetterContents()); // 디버깅용

        // 3. 익명 처리 로직 및 작성자(letterWriter) 유효성 검사
        String writerToSave = requestDto.getLetterWriter();
        if ("Y".equalsIgnoreCase(requestDto.getAnonymityFlag())) {
            writerToSave = ANONYMOUS_WRITER_VALUE;
        }
        recipientEntityRequest.setLetterWriter(writerToSave);

        // 4. organCode가 "ORGAN000" (직접입력) 일 경우 organEtc 설정, 아니면 null
        if (!ORGAN_CODE_DIRECT_INPUT.equals(requestDto.getOrganCode())) {
            recipientEntityRequest.setOrganEtc(null);
        }
        else {
            // ORGAN000인데 organEtc가 null이거나 비어있을 경우 (이전 NullPointerException의 다른 원인 가능성)
            if (requestDto.getOrganEtc() == null || requestDto.getOrganEtc().trim().isEmpty()) {
                logger.warn("ORGAN000 선택 시 organEtc는 필수 입력 항목입니다.");
                throw new RecipientInvalidDataException("ORGAN000 선택 시 organEtc는 필수 입력 항목입니다.");
            }
            recipientEntityRequest.setOrganEtc(requestDto.getOrganEtc()); // DTO에서 설정
        }

        // 5. RecipientEntity 저장
        RecipientEntity savedEntity = recipientRepository.save(recipientEntityRequest);

        // 상세 DTO로 변환하여 반환
        return RecipientDetailResponseDto.fromEntity(savedEntity);
    }

    // 특정 게시물 조회
    @Override
    public RecipientDetailResponseDto selectRecipient(int letterSeq) {
        // 1. 해당 게시물 조회 (삭제되지 않은 게시물만 조회하도록 필터링)
        RecipientEntity recipientEntity = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new RecipientNotFoundException(RECIPIENT_NOT_FOUND_MESSAGE));

        // 2. 조회수 증가
        recipientEntity.incrementReadCount();
        recipientRepository.save(recipientEntity); // 조회수 업데이트

        // 3. Entity를 RecipientDetailResponseDto 변환 (댓글 포함)
        RecipientDetailResponseDto responseDto = RecipientDetailResponseDto.fromEntity(recipientEntity);

        // 4. 댓글 관련 정보 별도 조회 및 DTO에 설정 _ 전체 댓글 수 조회 (NATIVE QUERY 사용)
        Integer totalCommentCount = recipientRepository.countCommentsByLetterSeq(letterSeq);
        if (totalCommentCount == null) {
            totalCommentCount = 0;
        }

        // DTO에 댓글 관련 데이터 설정
        responseDto.setCommentData(
                totalCommentCount,
                totalCommentCount > INITIAL_COMMENT_LOAD_LIMIT // 전체 댓글 수가 3개보다 많으면 더보기 버튼 활성화
        );

        return responseDto;
    }

    // 특정 게시물의 페이징된 댓글 조회 (새로운 구현)
    @Override
    public List<RecipientCommentResponseDto> selectPaginatedCommentsForRecipient(int letterSeq, Integer lastCommentId, int size) {
        logger.info("Selecting paginated comments for letterSeq: {}, lastCommentId: {}, size: {}", letterSeq, lastCommentId, size);

        // 1. 해당 게시물이 삭제되지 않았는지 확인
        // (RecipientEntity에 댓글을 포함하지 않는 findById 쿼리 사용)
        RecipientEntity activeRecipient = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new RecipientNotFoundException(RECIPIENT_NOT_FOUND_MESSAGE));

        Sort sort = Sort.by(Sort.Direction.ASC, WRITE_TIME, COMMENT_SEQ); // 작성 시간 오름차순, 동일 시간은 commentSeq로 정렬
        Pageable pageable = PageRequest.of(0, size, sort); // PageRequest는 내부적으로 OFFSET/LIMIT 쿼리를 생성

        List<RecipientCommentEntity> comments;
        if (lastCommentId == null || lastCommentId == 0) { // lastCommentId가 null이거나 0이면 처음부터 조회
            comments = recipientCommentRepository.findByLetterSeqAndDelFlag(activeRecipient, "N", pageable).getContent();
        } else {
            // lastCommentId보다 commentSeq가 큰 댓글부터 조회 (오름차순이므로)
            // findByLetterSeqAndDelFlagAndCommentSeqGreaterThanOrderByWriteTimeAscCommentSeqAsc(activeRecipient, "N", lastCommentId, pageable)
            // 또는 Specification을 사용하여 쿼리 생성
            Specification<RecipientCommentEntity> spec = (root, query, cb) -> {
                Predicate p = cb.and(
                        cb.equal(root.get(LETTER_SEQ), activeRecipient),
                        cb.equal(root.get(DEL_FLAG), "N")
                );
                if (lastCommentId != null && lastCommentId != 0) {
                    p = cb.and(p, cb.greaterThan(root.get(COMMENT_SEQ), lastCommentId));
                }
                return p;
            };
            comments = recipientCommentRepository.findAll(spec, pageable).getContent();
        }

        return comments.stream()
                .map(RecipientCommentResponseDto::fromEntity)
                .toList();
    }

    /**
     * 게시물 목록 조회 (검색 및 "더 보기" 페이징 통합)
     * searchCondition: 검색 조건 (searchType, searchKeyword)
     * lastId: "더 보기" 기능을 위한 마지막 게시물 ID (null 또는 0이면 첫 페이지 조회)
     * size: 한 번에 가져올 게시물 수
     */
    @Override
    public List<RecipientListResponseDto> selectRecipientList(
            RecipientSearchCondition searchCondition,
            Integer lastId, // lastId를 Integer 타입으로 유지 (null 허용)
            int size) {

        // 1. 기본 Specification 생성 (검색 조건 적용)
        Specification<RecipientEntity> spec = getRecipientSpecification(searchCondition);

        // 2. "더 보기" 기능 (lastId) 조건 추가
        if (lastId != null && lastId > 0) { // lastId가 유효한 경우에만 조건 추가
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get(LETTER_SEQ), lastId));
        }

        // 3. 정렬 조건 설정 (letterSeq 기준 내림차순 - 최신 게시물부터)
        Sort sort = Sort.by(Sort.Direction.DESC, LETTER_SEQ);

        // 4. Pageable 설정 (offset은 항상 0, limit은 size)
        // JPA의 Pageable은 offset/limit 기반이므로, lastId는 Specification으로 직접 처리해야 합니다.
        Pageable pageable = PageRequest.of(0, size, sort);

        // 5. 게시물 조회
        List<RecipientEntity> recipientList = recipientRepository.findAll(spec, pageable).getContent();

        // 6. 각 RecipientEntity에 대한 댓글 수를 조회
        final Map<Integer, Integer> commentCountMap = getCommentCountMap(recipientList);

        // 7. RecipientEntity를 RecipientResponseDto로 변환하고 commentCount 필드를 채우기
        return recipientList.stream()
                .map(entity -> {
                    RecipientListResponseDto dto = RecipientListResponseDto.fromEntity(entity);
                    dto.setCommentCount(commentCountMap.getOrDefault(entity.getLetterSeq(), 0));
                    return dto;
                })
                .toList(); // Stream.toList() 사용 (Java 16+)
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
            String searchKeyword = searchCondition.getSearchKeyword();
            SearchType searchType = searchCondition.getSearchType();

            if (StringUtils.hasText(searchKeyword)) {
                String likeKeyword = "%" + searchKeyword.trim().toLowerCase() + "%"; // 대소문자 무시 검색

                if (searchType == null || searchType == SearchType.ALL) { // 검색 타입이 없거나 'ALL'인 경우 (제목+내용)
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("letterTitle")), likeKeyword), // 제목 검색
                            cb.like(cb.lower(root.get("letterContents")), likeKeyword)  // 내용 검색
                    ));
                } else if (searchType == SearchType.TITLE) { // 제목만 검색
                    predicates.add(cb.like(cb.lower(root.get("letterTitle")), likeKeyword));
                } else if (searchType == SearchType.CONTENTS) { // 내용만 검색
                    predicates.add(cb.like(cb.lower(root.get("letterContents")), likeKeyword));
                }
                // SearchType.WRITER 관련 로직은 제거됨
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 댓글 수를 letterSeq 기준으로 매핑한 Map 반환
     */
    private Map<Integer, Integer> getCommentCountMap(List<RecipientEntity> recipientList) {
        List<Integer> letterSeqs = recipientList.stream()
                .map(RecipientEntity::getLetterSeq)
                .collect(Collectors.toList());

        final Map<Integer, Integer> commentCountMap = new HashMap<>();
        if (!letterSeqs.isEmpty()) {
            List<Object[]> commentCountsRaw = recipientRepository.countCommentsByLetterSeqs(letterSeqs);
            for (Object[] arr : commentCountsRaw) {
                Integer letterSeq = (Integer) arr[0];
                BigInteger commentCountBigInt = (BigInteger) arr[1];
                commentCountMap.put(letterSeq, commentCountBigInt.intValue()); // int로 변환하여 저장
            }
        }
        return commentCountMap;
    }

    // 다양한 타입(Object)에서 안전하게 Integer 추출
    private Integer extractAsInteger(Object obj, String fieldName) {
        if (obj == null) {
            logger.warn("Null value encountered for field '{}'", fieldName);
            return null;
        }

        try {
            if (obj instanceof Number number) {
                return number.intValue();
            }

            if (obj instanceof String str) {
                str = str.trim();
                if (str.isEmpty()) {
                    throw new InvalidIntegerConversionException("Empty or blank string cannot be converted to Integer for field: " + fieldName);
                }
                return Integer.parseInt(str);
            }

            throw new InvalidIntegerConversionException(
                    "Unsupported type for Integer conversion. Field: " + fieldName + ", Type: " + obj.getClass().getName()
            );
        }
        catch (NumberFormatException e) {
            throw new InvalidIntegerConversionException("Invalid number format for field: " + fieldName + ", value: " + obj, e);
        }
        catch (InvalidIntegerConversionException e) {
            throw e;
        }
        catch (Exception e) {
            throw new InvalidIntegerConversionException("Unexpected error during conversion of " + fieldName, e);
        }
    }

}

