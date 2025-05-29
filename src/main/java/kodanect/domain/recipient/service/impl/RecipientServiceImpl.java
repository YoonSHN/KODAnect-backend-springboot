package kodanect.domain.recipient.service.impl;

import kodanect.common.exception.InvalidPasscodeException;
import kodanect.common.exception.RecipientInvalidDataException;
import kodanect.common.exception.RecipientNotFoundException;
import kodanect.common.exception.custom.InvalidIntegerConversionException;
import kodanect.common.util.HcaptchaService;
import kodanect.domain.recipient.dto.RecipientResponseDto;
import kodanect.domain.recipient.dto.RecipientSearchCondition;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
import kodanect.domain.recipient.repository.RecipientRepository;
import kodanect.domain.recipient.service.RecipientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.*;
import java.util.stream.Collectors;

@Service("recipientService")
public class RecipientServiceImpl implements RecipientService {
    // 상수 정의
    private static final String ORGAN_CODE_DIRECT_INPUT = "ORGAN000";  // 직접입력 코드
    private static final String ANONYMOUS_WRITER_VALUE = "익명";
    private static final String CAPTCHA_FAILED_MESSAGE = "캡차 인증에 실패했습니다. 다시 시도해주세요.";
    private static final String RECIPIENT_NOT_FOUND_MESSAGE = "해당 게시물이 존재하지 않거나 이미 삭제되었습니다.";

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
    public RecipientResponseDto updateRecipient(RecipientEntity recipientEntityRequest, Integer letterSeq, String requestPasscode, String captchaToken) {
        // --- 0. hCaptcha 인증 검증 추가 ---
        if (!hcaptchaService.verifyCaptcha(captchaToken)) {
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
        recipientEntityold.setOrganCode(recipientEntityRequest.getOrganCode());
        recipientEntityold.setOrganEtc(recipientEntityRequest.getOrganEtc());
        recipientEntityold.setLetterTitle(recipientEntityRequest.getLetterTitle());
        recipientEntityold.setRecipientYear(recipientEntityRequest.getRecipientYear());

        // --- 익명 처리 로직 추가 시작 ---
        String writerToSave;
        if ("Y".equalsIgnoreCase(recipientEntityRequest.getAnonymityFlag())) {
            writerToSave = ANONYMOUS_WRITER_VALUE;
        }
        else {
            writerToSave = recipientEntityRequest.getLetterWriter();
        }
        recipientEntityold.setLetterWriter(writerToSave);
        // 비밀번호는 수정 시 변경될 수 있으므로, 요청 DTO에 새로운 비밀번호가 있다면 업데이트
        if (recipientEntityRequest.getLetterPasscode() != null && !recipientEntityRequest.getLetterPasscode().isEmpty()) {
            recipientEntityold.setLetterPasscode(recipientEntityRequest.getLetterPasscode());
        }
        recipientEntityold.setAnonymityFlag(recipientEntityRequest.getAnonymityFlag());

        // 내용(HTML) 필터링
        String originalContents = recipientEntityRequest.getLetterContents();
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

        recipientEntityold.setFileName(recipientEntityRequest.getFileName());
        recipientEntityold.setOrgFileName(recipientEntityRequest.getOrgFileName());
        recipientEntityold.setModifierId(recipientEntityRequest.getModifierId()); // 수정자 ID 업데이트

        RecipientEntity updatedEntity = recipientRepository.save(recipientEntityold); // 변경사항 저장
        logger.info("게시물 성공적으로 수정됨: letterSeq={}", updatedEntity.getLetterSeq());
        return RecipientResponseDto.fromEntity(updatedEntity); // DTO로 변환하여 반환
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
    public RecipientResponseDto insertRecipient(RecipientEntity recipientEntityRequest, String captchaToken) {
        // --- 0. hCaptcha 인증 검증 추가 ---
        if (!hcaptchaService.verifyCaptcha(captchaToken)) {
            logger.warn("hCaptcha 인증 실패: 유효하지 않은 캡차 토큰입니다.");
            throw new RecipientInvalidDataException(CAPTCHA_FAILED_MESSAGE);
        }
        logger.info("hCaptcha 인증 성공. 게시물 등록 진행.");

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

        // 2. 익명 처리 로직 및 작성자(letterWriter) 유효성 검사 (기존 코드 유지)
        String writerToSave = recipientEntityRequest.getLetterWriter();
        if ("Y".equalsIgnoreCase(recipientEntityRequest.getAnonymityFlag())) {
            writerToSave = ANONYMOUS_WRITER_VALUE;
        }
        else {
            // @Valid에서 검증된 값이 들어오므로 여기서는 추가 유효성 검사 불필요
            writerToSave = recipientEntityRequest.getLetterWriter();
        }
        recipientEntityRequest.setLetterWriter(writerToSave);

        // 3. organCode가 "ORGAN000" (직접입력) 일 경우 organEtc 설정, 아니면 null
        if (!ORGAN_CODE_DIRECT_INPUT.equals(recipientEntityRequest.getOrganCode())) {
            recipientEntityRequest.setOrganEtc(null);
        }
        else {
            // ORGAN000인데 organEtc가 null이거나 비어있을 경우 (이전 NullPointerException의 다른 원인 가능성)
            if (recipientEntityRequest.getOrganEtc() == null || recipientEntityRequest.getOrganEtc().trim().isEmpty()) {
                logger.warn("ORGAN000 선택 시 organEtc는 필수 입력 항목입니다.");
                throw new RecipientInvalidDataException("ORGAN000 선택 시 organEtc는 필수 입력 항목입니다.");
            }
        }

        // 4. RecipientEntity 저장
        RecipientEntity savedEntity = recipientRepository.save(recipientEntityRequest);

        return RecipientResponseDto.fromEntity(savedEntity);
    }

    // 특정 게시물 조회
    @Override
    public RecipientResponseDto selectRecipient(int letterSeq) {
        // 1. 해당 게시물 조회 (삭제되지 않은 게시물만 조회하도록 필터링)
        RecipientEntity recipientEntity = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new RecipientNotFoundException(RECIPIENT_NOT_FOUND_MESSAGE));

        // 2. 조회수 증가
        recipientEntity.incrementReadCount();
        recipientRepository.save(recipientEntity); // 변경된 엔티티 저장 (조회수 업데이트)

        // 3. Entity를 ResponseDto로 변환
        RecipientResponseDto responseDto = RecipientResponseDto.fromEntity(recipientEntity);

        // 4. 댓글 수 설정 (RecipientResponseDto에만 존재)
        int commentCount = Optional.ofNullable(recipientRepository.countCommentsByLetterSeq(letterSeq)).orElse(0);
        responseDto.setCommentCount(commentCount);

        return responseDto;
    }

    // 페이징 처리된 게시물 목록 조회 (댓글 수 포함)
    @Override
    public Page<RecipientResponseDto> selectRecipientListPaged(RecipientSearchCondition searchCondition, Pageable pageable){

        // 1. 조건에 맞는 RecipientVO 목록 조회 (페이징 및 정렬 적용)
        Specification<RecipientEntity> spec = getRecipientSpecification(searchCondition);
        Page<RecipientEntity> recipientPage = recipientRepository.findAll(spec, pageable);

        // 2. 각 RecipientVO에 대한 댓글 수를 조회
        // 조회된 게시물들의 letter_seq를 추출
        List<Integer> letterSeqs = recipientPage.getContent().stream()
                .map(RecipientEntity::getLetterSeq)
                .collect(Collectors.toList());

        // Map<letter_seq, commentCount> 형태로 댓글 수 저장
        final Map<Integer, Integer> commentCountMap = new HashMap<>();
        if (!letterSeqs.isEmpty()) {
            List<Object[]> commentCountsRaw = recipientRepository.countCommentsByLetterSeqs(letterSeqs);

            for (Object[] arr : commentCountsRaw) {
                Integer key = extractAsInteger(arr[0], "letterSeq");
                Integer value = extractAsInteger(arr[1], "commentCount");
                commentCountMap.put(key, value);
            }
        }

        // 3. RecipientEntity를 RecipientResponseDto로 변환하고 commentCount 필드를 채우기
        return recipientPage.map(entity -> {
            RecipientResponseDto dto = RecipientResponseDto.fromEntity(entity);
            dto.setCommentCount(commentCountMap.getOrDefault(entity.getLetterSeq(), 0));
            return dto;
        });
    }

    // 제목, 내용, 전체 검색
    @Override
    public List<RecipientResponseDto> selectRecipientList(RecipientSearchCondition searchCondition) {
        Specification<RecipientEntity> spec = getRecipientSpecification(searchCondition);
        List<RecipientEntity> recipientList = recipientRepository.findAll(spec);

        // 댓글 수 맵 생성
        final Map<Integer, Integer> commentCountMap = getCommentCountMap(recipientList);

        // DTO 변환 + 댓글 수 세팅
        return recipientList.stream()
                .map(entity -> {
                    RecipientResponseDto dto = RecipientResponseDto.fromEntity(entity);
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
                Integer letterSeq = extractAsInteger(arr[0], "letterSeq");
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
            logger.error("Invalid number format for field '{}': {}", fieldName, obj, e);
            throw new InvalidIntegerConversionException("Invalid number format for field: " + fieldName + ", value: " + obj, e);
        }
        catch (InvalidIntegerConversionException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Unexpected error converting {} to Integer. Value = {}, Type = {}", fieldName, obj, obj.getClass().getName(), e);
            throw new InvalidIntegerConversionException("Unexpected error during conversion of " + fieldName, e);
        }
    }

    // Spring Data JPA Specification을 활용한 동적 쿼리 생성 메서드 (이전과 동일)
    private Specification<RecipientEntity> getRecipientSpecification(RecipientSearchCondition searchCondition) {
        return (root, query, cb) -> {
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // 삭제되지 않은 게시물만 조회
            predicates.add(cb.equal(root.get("delFlag"), "N"));

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

