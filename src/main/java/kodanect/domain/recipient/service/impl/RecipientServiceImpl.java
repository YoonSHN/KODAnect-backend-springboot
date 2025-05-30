package kodanect.domain.recipient.service.impl;

import kodanect.domain.recipient.dto.*;
import kodanect.domain.recipient.exception.InvalidPasscodeException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Sort;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.persistence.criteria.Predicate;
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
            throw new InvalidPasscodeException("비밀번호가 일치하지 않습니다.");
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

        recipientEntityold.setFileName(requestDto.getFileName());
        recipientEntityold.setOrgFileName(requestDto.getOrgFileName());

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
            throw new InvalidPasscodeException("비밀번호가 일치하지 않습니다.");
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
        // --- 0. hCaptcha 인증 검증 추가 ---
        if (!hcaptchaService.verifyCaptcha(requestDto.getCaptchaToken())) {
            logger.warn("hCaptcha 인증 실패: 유효하지 않은 캡차 토큰입니다.");
            throw new RecipientInvalidDataException(CAPTCHA_FAILED_MESSAGE);
        }
        logger.info("hCaptcha 인증 성공. 게시물 등록 진행.");

        RecipientEntity recipientEntityRequest = requestDto.toEntity(); // DTO를 Entity로 변환

        // 1. Jsoup을 사용하여 HTML 필터링 및 내용 유효성 검사
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

        // 2. 익명 처리 로직 및 작성자(letterWriter) 유효성 검사
        String writerToSave = requestDto.getLetterWriter();
        if ("Y".equalsIgnoreCase(requestDto.getAnonymityFlag())) {
            writerToSave = ANONYMOUS_WRITER_VALUE;
        }
        recipientEntityRequest.setLetterWriter(writerToSave);

        // 3. organCode가 "ORGAN000" (직접입력) 일 경우 organEtc 설정, 아니면 null
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

        // 4. RecipientEntity 저장
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

    // "더 보기" 기능을 위한 게시물 목록 조회
    // lastId가 null이면 첫 페이지, 아니면 lastId보다 작은 게시물 조회 (최신순)
    @Override
    public List<RecipientListResponseDto> selectRecipientList(RecipientSearchCondition searchCondition, Integer lastId, int size) {
        Specification<RecipientEntity> spec = getRecipientSpecification(searchCondition);

        // 정렬 조건 추가: letterSeq를 기준으로 내림차순 정렬 (최신 게시물부터)
        Sort sort = Sort.by(Sort.Direction.DESC, LETTER_SEQ);
        // Pageable을 사용하여 limit (size)만 적용하고 offset은 JPA 내부에서 lastId를 통해 처리
        Pageable pageable = PageRequest.of(0, size, sort); // offset은 0으로 고정하고, size와 정렬만 설정

        List<RecipientEntity> recipientList;

        if (lastId == null || lastId == 0) {
            // 첫 조회: 전체 게시물 중 최신순으로 size만큼 가져옴
            recipientList = recipientRepository.findAll(spec, pageable).getContent();
        }
        else {
            // 추가 조회: lastId보다 작은 게시물 중 최신순으로 size만큼 가져옴
            // Specification에 lastId 조건을 추가해야 합니다.
            Specification<RecipientEntity> lastIdSpec = (root, query, cb) -> {
                // 기존 Specification에 lastId < letterSeq 조건을 추가
                return cb.and(
                        spec.toPredicate(root, query, cb),          // 기존 조건
                        cb.lessThan(root.get(LETTER_SEQ), lastId)  // lastId보다 작은 ID
                );
            };
            recipientList = recipientRepository.findAll(lastIdSpec, pageable).getContent();
        }

        // 2. 각 RecipientEntity에 대한 댓글 수를 조회
        final Map<Integer, Integer> commentCountMap = getCommentCountMap(recipientList);

        // 3. RecipientEntity를 RecipientResponseDto로 변환하고 commentCount 필드를 채우기
        return recipientList.stream()
                .map(entity -> {
                    RecipientListResponseDto dto = RecipientListResponseDto.fromEntity(entity);
                    dto.setCommentCount(commentCountMap.getOrDefault(entity.getLetterSeq(), 0));
                    return dto;
                })
                .toList();
    }

    // 제목, 내용, 전체 검색
    @Override
    public List<RecipientListResponseDto> selectRecipientList(RecipientSearchCondition searchCondition) {
        Specification<RecipientEntity> spec = getRecipientSpecification(searchCondition);
        List<RecipientEntity> recipientList = recipientRepository.findAll(spec);

        // 댓글 수 맵 생성
        final Map<Integer, Integer> commentCountMap = getCommentCountMap(recipientList);

        // DTO 변환 + 댓글 수 세팅
        return recipientList.stream()
                .map(entity -> {
                    RecipientListResponseDto dto = RecipientListResponseDto.fromEntity(entity);
                    dto.setCommentCount(commentCountMap.getOrDefault(entity.getLetterSeq(), 0));
                    return dto;
                })
                .toList(); // Stream.toList() 사용
    }

    // 제목, 내용, 전체 검색 결과 수
    @Override
    public int selectRecipientListTotCnt(RecipientSearchCondition searchCondition) {
        Specification<RecipientEntity> spec = getRecipientSpecification(searchCondition);
        return (int) recipientRepository.count(spec);
    }

    // 댓글 수를 letterSeq 기준으로 매핑한 Map 반환
    private Map<Integer, Integer> getCommentCountMap(List<RecipientEntity> recipientList) {
        List<Integer> letterSeqs = recipientList.stream()
                .map(RecipientEntity::getLetterSeq)
                .collect(Collectors.toList());

        final Map<Integer, Integer> commentCountMap = new HashMap<>();
        if (!letterSeqs.isEmpty()) {
            List<Object[]> commentCountsRaw = recipientRepository.countCommentsByLetterSeqs(letterSeqs);
            for (Object[] arr : commentCountsRaw) {
                Integer letterSeq = extractAsInteger(arr[0], LETTER_SEQ);
                Integer commentCount = extractAsInteger(arr[1], "commentCount");
                commentCountMap.put(letterSeq, commentCount);
            }
        }
        return commentCountMap;
    }

    // 다양한 타입(Object)에서 안전하게 Integer 추출
    private Integer extractAsInteger(Object obj, String fieldName) {
        if (obj == null) {
            logger.warn("Null value encountered for field '{}'", fieldName);
            return null; // 또는 throw new InvalidIntegerConversionException(...)
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

    // Spring Data JPA Specification을 활용한 동적 쿼리 생성 메서드 (이전과 동일)
    private Specification<RecipientEntity> getRecipientSpecification(RecipientSearchCondition searchCondition) {
        return (root, query, cb) -> {
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // 삭제되지 않은 게시물만 조회
            predicates.add(cb.equal(root.get(DEL_FLAG), "N"));

            // 검색 조건 (제목, 내용, 작성자)
            String searchType = searchCondition.getSearchType();
            String searchKeyword = searchCondition.getSearchKeyword();

            if (StringUtils.hasText(searchType) && StringUtils.hasText(searchKeyword)) {
                String likeKeyword = "%" + searchKeyword.toLowerCase() + "%";
                switch (searchType) {
                    case "title":
                        predicates.add(cb.like(cb.lower(root.get("letterTitle")), likeKeyword));
                        break;
                    case "contents":
                        predicates.add(cb.like(cb.lower(root.get("letterContents")), likeKeyword));
                        break;
                    case "writer":
                        predicates.add(cb.like(cb.lower(root.get("letterWriter")), likeKeyword));
                        break;
                    case "all":
                        predicates.add(cb.or(
                                cb.like(cb.lower(root.get("letterTitle")), likeKeyword),
                                cb.like(cb.lower(root.get("letterContents")), likeKeyword),
                                cb.like(cb.lower(root.get("letterWriter")), likeKeyword)
                        ));
                        break;
                    default:
                        // 유효하지 않은 searchType은 무시하거나 예외 처리
                        break;
                }
            }
            return cb.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
    }

}

