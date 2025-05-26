package kodanect.domain.recipient.service.impl;

import kodanect.domain.recipient.dto.RecipientResponseDto;
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

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service("recipientService")
public class RecipientServiceImpl implements RecipientService {

    @Resource(name = "recipientRepository")
    private final RecipientRepository recipientRepository;
    private final RecipientCommentRepository recipientCommentRepository;

    private final Logger logger = LoggerFactory.getLogger(RecipientService.class); // 로거 선언

    // 상수 정의
    private final String PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$";
    private final int TITLE_MAX_LENGTH_BYTES = 50;
    private final String ORGAN_CODE_REGEX = "^ORGAN(00[0-9]|01[0-4])$";
    private final String ORGAN_CODE_DIRECT_INPUT = "ORGAN000";
    private final int ORGAN_ETC_MAX_LENGTH_BYTES = 30;
    private final String ANONYMOUS_WRITER_VALUE = "익명";
    private final int WRITER_MAX_LENGTH_BYTES = 10;
    private final int RECIPIENT_YEAR_MIN = 1995;
    private final int RECIPIENT_YEAR_MAX = 2030;
    private final int FILE_NAME_MAX_LENGTH_BYTES = 600;

    public RecipientServiceImpl(RecipientRepository recipientRepository, RecipientCommentRepository recipientCommentRepository) {
        this.recipientRepository = recipientRepository;
        this.recipientCommentRepository = recipientCommentRepository;
    }

    // 게시물 비밀번호 확인
//    @Transactional
    @Override
    public boolean verifyLetterPassword(Integer letterSeq, String letterPasscode) throws Exception {

        // 게시물 조회 (삭제되지 않은 게시물만 조회)
        RecipientEntity recipientEntityold = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag())) // 삭제되지 않은 게시물만 필터링
                .orElseThrow(() -> new Exception("해당 게시물이 존재하지 않거나 이미 삭제되었습니다."));

        // 비밀번호 불일치
        if (letterPasscode == null || !letterPasscode.equals(recipientEntityold.getLetterPasscode())) {
            return false;
        }
        else {
            return true;
        }
    }

    // 게시물 수정
//    @Transactional
    @Override
    public RecipientResponseDto updateRecipient(RecipientEntity recipientEntityRequest, Integer letterSeq, String requestPasscode) throws Exception {
        // 게시물 조회 (삭제되지 않은 게시물만 조회)
        RecipientEntity recipientEntityold = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new NoSuchElementException("해당 게시물이 존재하지 않거나 이미 삭제되었습니다."));

        // 비밀번호 검증
        if (requestPasscode == null || !requestPasscode.equals(recipientEntityold.getLetterPasscode())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

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
        recipientEntityold.setLetterContents(recipientEntityRequest.getLetterContents());
        recipientEntityold.setFileName(recipientEntityRequest.getFileName());
        recipientEntityold.setOrgFileName(recipientEntityRequest.getOrgFileName());
        recipientEntityold.setModifierId(recipientEntityRequest.getModifierId()); // 수정자 ID 업데이트

        RecipientEntity updatedEntity = recipientRepository.save(recipientEntityold); // 변경사항 저장

        return RecipientResponseDto.fromEntity(updatedEntity); // DTO로 변환하여 반환
    }

    // 게시물 삭제
    // 조건 : 등록된 게시물의 비밀번호와 일치하는 경우
//    @Transactional
    @Override
    public void deleteRecipient(Integer letterSeq, String letterPasscode) throws Exception {

        // 게시물 조회 (삭제되지 않은 게시물만 조회)
        RecipientEntity recipientEntityold = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new NoSuchElementException("해당 게시물이 존재하지 않거나 이미 삭제되었습니다."));

        // 게시물 비번이 없거나 or 비밀번호 불일치
        if (letterPasscode == null || !letterPasscode.equals(recipientEntityold.getLetterPasscode())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        else {
            // 게시물 소프트 삭제
            recipientEntityold.setDelFlag("Y");
            recipientRepository.save(recipientEntityold);

            // 해당 게시물의 모든 댓글 소프트 삭제
            List<RecipientCommentEntity> commentsToSoftDelete =
                    recipientCommentRepository.findByLetterLetterSeqAndDelFlagOrderByWriteTimeAsc(letterSeq, "N");

            if (commentsToSoftDelete != null && !commentsToSoftDelete.isEmpty()) {
                for (RecipientCommentEntity comment : commentsToSoftDelete) {
                    comment.setDelFlag("Y"); // 댓글의 delflag를 'Y'로 변경
                    // 각 댓글을 저장하여 변경사항을 DB에 반영 (벌크 업데이트가 더 효율적일 수 있으나, 현재 상황에서 단일 저장)
                    recipientCommentRepository.save(comment);
                }
                // logger.info("게시물 {} 에 연결된 {}개의 댓글이 소프트 삭제 처리되었습니다.", letterSeq, commentsToSoftDelete.size());
            }
            else {
                // logger.info("게시물 {} 에 연결된 활성 댓글이 없습니다. 소프트 삭제할 댓글 없음.", letterSeq);
            }
        }
    }

    // 게시물 등록
    // 조건 : letter_writer 한영자 10자 제한, letter_passcode 영숫자 8자 이상, 캡챠 인증
//    @Transactional
    @Override
    public RecipientResponseDto insertRecipient(RecipientEntity recipientEntityRequest) throws Exception {
        // 게시판 등록조건 확인
        // 1. 비밀번호 유효성 검사
        if (recipientEntityRequest.getLetterPasscode() == null || !recipientEntityRequest.getLetterPasscode().matches(PASSWORD_REGEX)) {
            throw new Exception("비밀번호는 영문 숫자 8자 이상 이어야 합니다.");
        }
        // 2. 제목 유효성 검사 (한영 50자 제한)
        if (recipientEntityRequest.getLetterTitle() == null || recipientEntityRequest.getLetterTitle().getBytes("UTF-8").length > TITLE_MAX_LENGTH_BYTES) {
            throw new Exception("제목은 한글 영문 50자 이하여야 합니다.");
        }
        // 3. 내용 널 체크
        if (recipientEntityRequest.getLetterContents() == null || recipientEntityRequest.getLetterContents().trim().isEmpty()) {
            logger.warn("내용 유효성 검사 실패: 내용이 비어있음");
            throw new Exception("내용은 필수 입력 항목입니다.");
        }

        // Jsoup을 사용하여 HTML 필터링
        // Safelist.relaxed(): 기본적인 안전한 HTML 태그 (a, b, blockquote, br, cite, code, dd, dl, dt, em, i, li, ol, p, pre, q, small, span, strike, strong, sub, sup, u, ul, img) 허용
        Safelist safelist = Safelist.relaxed();
        String cleanContents = Jsoup.clean(recipientEntityRequest.getLetterContents(), safelist);
        // 필터링 후 HTML 태그를 포함한 내용이 아니라, 순수 텍스트 내용이 비어있는지 확인 (HTML 태그를 제거하고 순수 텍스트만 얻음)
        String pureTextContents = Jsoup.parse(cleanContents).text();
        recipientEntityRequest.setLetterContents(cleanContents.trim());

        // 필터링 후 내용이 비어있는지 다시 확인 (모든 태그가 제거될 경우 대비)
        if (pureTextContents.trim().isEmpty()) {
            logger.warn("내용 유효성 검사 실패: 필터링 후 내용이 비어있음");
            throw new Exception("내용은 필수 입력 항목입니다.");
        }

        // 4. 장기 코드 (organCode) 널 체크
        String organCode = recipientEntityRequest.getOrganCode();
        if (organCode == null || organCode.trim().isEmpty()) {
            throw new Exception("기증받은 장기를 선택해주세요.");
        }
        // 코드 형식 확인, 패턴: "ORGAN"으로 시작하고, 뒤에 000부터 014까지의 숫자 (000, 001, ..., 014)
        Pattern organCodePattern = Pattern.compile(ORGAN_CODE_REGEX);
        if (organCode == null || !organCodePattern.matcher(organCode).matches()) {
            throw new Exception("유효하지 않은 장기 코드입니다.");
        }
        // ORGAN000 (직접입력)일 경우 organEtc 필수 및 길이 제한
        if (organCode.equals(ORGAN_CODE_DIRECT_INPUT)) {
            if (recipientEntityRequest.getOrganEtc() == null || recipientEntityRequest.getOrganEtc().trim().isEmpty()) {
                throw new Exception("직접입력 선택 시 기타 장기를 입력해야 합니다.");
            }
            if (recipientEntityRequest.getOrganEtc().getBytes(StandardCharsets.UTF_8).length > ORGAN_ETC_MAX_LENGTH_BYTES) {
                throw new Exception("기타 장기는 한글/영문 30자 이하여야 합니다.");
            }
        }
        else {
            // ORGAN001~ORGAN014와 같은 유효한 코드일 경우 organEtc는 null로 클리어
            recipientEntityRequest.setOrganEtc(null);
        }
        // 5. 익명 처리 로직 및 작성자(letterWriter) 유효성 검사
        String writerToSave = recipientEntityRequest.getLetterWriter(); // 기본적으로 입력된 작성자 사용
        // anonymityFlag가 'Y'이면 (char 타입이므로 'Y'와 비교)
        if ("Y".equalsIgnoreCase(recipientEntityRequest.getAnonymityFlag())) {
            writerToSave = ANONYMOUS_WRITER_VALUE;
        }
        else {
            // 익명이 아닐 경우에만 작성자 유효성 검사
            if (writerToSave == null || writerToSave.trim().isEmpty()) {
                throw new Exception("작성자는 필수 입력 항목입니다.");
            }
            // 한영 10자 제한
            if (writerToSave.getBytes(StandardCharsets.UTF_8).length > WRITER_MAX_LENGTH_BYTES) {
                throw new Exception("작성자는 한글/영문 10자 이하여야 합니다.");
            }
        }
        // 6. 기증받은 년도 (recipientYear) 유효성 검사
        String recipientYear = recipientEntityRequest.getRecipientYear(); // String 타입으로 가져옴
        // 널 체크
        if (recipientYear == null || recipientYear.trim().isEmpty()) {
            throw new Exception("기증받은 년도는 필수 입력 항목입니다.");
        }
        // 년도범위 체크
        int year;
        try {
            year = Integer.parseInt(recipientYear); // String을 int로 변환
        }
        catch (NumberFormatException e) {
            throw new Exception("기증받은 년도는 유효한 숫자 형식이어야 합니다.");
        }
        if (year < RECIPIENT_YEAR_MIN || year > RECIPIENT_YEAR_MAX) {
            throw new Exception("기증받은 년도는 1995년에서 2030년 사이의 값이어야 합니다.");
        }
        // 7. 파일 이름 유효성 검사
        if (StringUtils.hasText(recipientEntityRequest.getFileName())) {
            // DB 컬럼 길이(varchar(600))를 고려한 길이 검사
            if (recipientEntityRequest.getOrgFileName().getBytes(StandardCharsets.UTF_8).length > FILE_NAME_MAX_LENGTH_BYTES){
                throw new Exception("저장 파일명이 너무 깁니다.");
            }
        }

        if (StringUtils.hasText(recipientEntityRequest.getOrgFileName())) {
            // DB 컬럼 길이(varchar(600))를 고려한 길이 검사
            if (recipientEntityRequest.getOrgFileName().getBytes(StandardCharsets.UTF_8).length > FILE_NAME_MAX_LENGTH_BYTES){
                throw new Exception("원본 파일명이 너무 깁니다.");
            }
        }

        // 빌더 패턴을 사용하여 RecipientEntity 객체 생성
        RecipientEntity newRecipient = RecipientEntity.builder()
                .letterWriter(writerToSave)
                .letterTitle(recipientEntityRequest.getLetterTitle())
                .letterContents(recipientEntityRequest.getLetterContents())
                .letterPasscode(recipientEntityRequest.getLetterPasscode())
                .anonymityFlag(recipientEntityRequest.getAnonymityFlag())
                .organCode(recipientEntityRequest.getOrganCode())
                .organEtc(recipientEntityRequest.getOrganEtc())
                .recipientYear(recipientEntityRequest.getRecipientYear())
                .fileName(recipientEntityRequest.getFileName())
                .orgFileName(recipientEntityRequest.getOrgFileName())
                .build();

        // 저장
        RecipientEntity savedEntity = recipientRepository.save(newRecipient);

        // DTO로 변환 후 반환
        return RecipientResponseDto.fromEntity(savedEntity);
    }

    // 특정 게시물 조회
//    @Transactional
    @Override
    public RecipientResponseDto selectRecipient(int letterSeq) throws Exception {
        // 1. 해당 게시물 조회 (삭제되지 않은 게시물만 조회하도록 필터링)
        RecipientEntity recipientEntity = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new NoSuchElementException("해당 게시물이 존재하지 않거나 이미 삭제되었습니다."));

        // 2. 조회수 증가
        recipientEntity.setReadCount(recipientEntity.getReadCount() + 1);
        recipientRepository.save(recipientEntity); // 변경된 엔티티 저장 (조회수 업데이트)

        // 3. Entity를 ResponseDto로 변환
        RecipientResponseDto responseDto = RecipientResponseDto.fromEntity(recipientEntity);

        // 4. 댓글 수 설정 (RecipientResponseDto에만 존재)
        int commentCount = Optional.ofNullable(recipientRepository.countCommentsByLetterSeq(letterSeq)).orElse(0);
        responseDto.setCommentCount(commentCount);

        return responseDto;
    }

    // 페이징 처리된 게시물 목록 조회 (댓글 수 포함)
//    @Transactional(readOnly = true)
    @Override
    public Page<RecipientResponseDto> selectRecipientListPaged(RecipientEntity searchCondition, Pageable pageable) throws Exception {

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
        Page<RecipientResponseDto> responseDtoPage = recipientPage.map(entity -> {
            RecipientResponseDto dto = RecipientResponseDto.fromEntity(entity);
            dto.setCommentCount(commentCountMap.getOrDefault(entity.getLetterSeq(), 0));
            return dto;
        });

        return responseDtoPage;
    }

    // 제목, 내용, 전체 검색
    @Override
    public List<RecipientResponseDto> selectRecipientList(RecipientEntity searchCondition) throws Exception {
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
                .collect(Collectors.toList());
    }

    // 제목, 내용, 전체 검색 결과 수
    @Override
    public int selectRecipientListTotCnt(RecipientEntity searchCondition) throws Exception {
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
        try {
            if (obj instanceof Number) {
                return ((Number) obj).intValue();
            }
            else if (obj instanceof String) {
                return Integer.parseInt((String) obj);
            }
        }
        catch (Exception e) {
//            RecipientServiceImpl.logger.error("Error converting {} to Integer: value = {}, type = {}", fieldName, obj, obj.getClass().getName(), e);
            throw new RuntimeException("Invalid value for " + fieldName + ": " + obj, e);
        }
        throw new ClassCastException("Unsupported type for " + fieldName + ": " + obj.getClass().getName());
    }

}

