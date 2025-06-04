package kodanect.domain.recipient.service.impl;

import kodanect.common.util.HcaptchaService;
import kodanect.domain.recipient.dto.RecipientCommentRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
import kodanect.domain.recipient.exception.RecipientCommentNotFoundException;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import kodanect.domain.recipient.repository.RecipientCommentRepository;
import kodanect.domain.recipient.repository.RecipientRepository;
import kodanect.domain.recipient.service.RecipientCommentService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("recipientCommentService")
@RequiredArgsConstructor // final 필드에 대한 생성자 주입
public class RecipientCommentServiceImpl implements RecipientCommentService {

    // 비밀번호 영숫자 8자 이상 (현재 사용되지 않음)
    // private static final String COMMENT_PASSCODE_PATTERN = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$";

    // 중복되는 에러 메시지
    private static final String COMMENT_NOT_FOUND_MESSAGE = "댓글을 찾을 수 없거나 이미 삭제되었습니다.";

    // 캡차 에러 메시지
    private static final String CAPTCHA_FAILED_MESSAGE = "캡차 인증에 실패했습니다. 다시 시도해주세요.";

    // 스택 트레이스에서 호출한 메서드의 인덱스 상수
    private static final int CALLING_METHOD_STACK_INDEX = 2;

    @Resource(name = "recipientCommentRepository")
    private final RecipientCommentRepository recipientCommentRepository;
    private final RecipientRepository recipientRepository;
    private final HcaptchaService hcaptchaService;

    private final Logger logger = LoggerFactory.getLogger(RecipientCommentServiceImpl.class);

    // --- 헬퍼 메서드 추출 ---

    /**
     * 특정 letterSeq에 해당하는 활성화된 게시물을 조회합니다.
     * @param letterSeq 게시물 시퀀스
     * @return 활성화된 RecipientEntity
     * @throws RecipientNotFoundException 게시물을 찾을 수 없거나 삭제된 경우
     */
    private RecipientEntity getActiveRecipient(Integer letterSeq) {
        return recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> {
                    logger.warn("게시물을 찾을 수 없거나 이미 삭제된 게시물입니다: {}", letterSeq);
                    return new RecipientNotFoundException("게시물을 찾을 수 없거나 이미 삭제된 게시물입니다: " + letterSeq);
                });
    }

    /**
     * hCaptcha 인증을 수행합니다.
     * @param captchaToken hCaptcha 토큰
     * @throws RecipientInvalidDataException 캡차 인증에 실패한 경우
     */
    private void verifyHcaptcha(String captchaToken) {
        if (!hcaptchaService.verifyCaptcha(captchaToken)) {
            logger.warn(CAPTCHA_FAILED_MESSAGE);
            throw new RecipientInvalidDataException(CAPTCHA_FAILED_MESSAGE);
        }
        logger.info("hCaptcha 인증 성공. {} 진행.",
                Thread.currentThread().getStackTrace()[CALLING_METHOD_STACK_INDEX].getMethodName()); // 호출한 메서드 이름을 로그에 남김
    }

    /**
     * 댓글 내용을 HTML 태그로부터 필터링하고 공백을 정리하여 반환합니다.
     * 내용이 비어있으면 예외를 발생시킵니다.
     * @param contents 원본 댓글 내용
     * @return 필터링되고 정리된 댓글 내용
     * @throws RecipientInvalidDataException 필터링 후 내용이 비어있는 경우
     */
    private String cleanAndValidateCommentContents(String contents) {
        // Jsoup을 사용하여 HTML 태그 필터링 (relaxed Safelist로 <p> 태그 등 허용)
        // clean() 결과가 원본 내용에 따라 줄바꿈이나 추가 공백을 포함할 수 있으므로,
        // \\s+를 이용해 모든 연속된 공백(공백, 탭, 줄바꿈 등)을 하나의 공백으로 치환하고
        // trim()을 통해 앞뒤 공백을 최종적으로 제거합니다.
        Safelist safelist = Safelist.relaxed();
        String cleanContents = Jsoup.clean(contents, safelist);
        String finalContents = cleanContents.replaceAll("\\s+", " ").trim();

        if (finalContents.isEmpty()) {
            logger.warn("댓글 내용 필터링 실패: 필터링 후 내용이 비어있음");
            throw new RecipientInvalidDataException("댓글 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");
        }
        return finalContents;
    }

    /**
     * 댓글 비밀번호를 검증합니다.
     * @param existingComment 기존 댓글 엔티티
     * @param inputPasscode 사용자 입력 비밀번호
     * @throws RecipientInvalidPasscodeException 비밀번호가 일치하지 않는 경우
     */
    private void validateCommentPasscode(RecipientCommentEntity existingComment, String inputPasscode) {
        if (!existingComment.checkPasscode(inputPasscode)) {
            logger.warn("댓글 비밀번호 불일치: commentSeq={}", existingComment.getCommentSeq());
            throw new RecipientInvalidPasscodeException("비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * 특정 시퀀스에 해당하는 삭제되지 않은 댓글을 조회합니다.
     * @param commentSeq 댓글 시퀀스
     * @return 삭제되지 않은 RecipientCommentEntity
     * @throws RecipientCommentNotFoundException 댓글을 찾을 수 없거나 이미 삭제된 경우
     */
    private RecipientCommentEntity getActiveComment(Integer commentSeq) {
        return recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq, "N")
                .orElseThrow(() -> {
                    logger.warn("댓글 조회 실패: {}", COMMENT_NOT_FOUND_MESSAGE);
                    return new RecipientCommentNotFoundException(COMMENT_NOT_FOUND_MESSAGE);
                });
    }

    // --- 서비스 메서드 구현 ---

    // 특정 게시물의 댓글 조회
    @Override
    public List<RecipientCommentResponseDto> selectRecipientCommentByLetterSeq(int letterSeq) {
        logger.info("댓글 목록 조회 시작: letterSeq={}", letterSeq);

        // 1. 해당 게시물이 삭제되지 않았는지 확인 (헬퍼 메서드 사용)
        RecipientEntity activeRecipient = getActiveRecipient(letterSeq);

        // 2. 삭제되지 않은 댓글만 조회하고, 작성시간 기준 오름차순으로 정렬
        List<RecipientCommentEntity> comments = recipientCommentRepository.findCommentsByLetterSeqAndDelFlagSorted(activeRecipient, "N");

        logger.info("댓글 목록 조회 성공. {}개의 댓글 반환.", comments.size());
        return comments.stream()
                .map(RecipientCommentResponseDto::fromEntity) // Entity를 Response DTO로 변환
                .toList();
    }

    // 댓글 작성
    @Override
    public RecipientCommentResponseDto insertComment(int letterSeq, RecipientCommentRequestDto requestDto, String captchaToken) {
        logger.info("댓글 작성 요청 시작: letterSeq={}", letterSeq);

        // 1. hCaptcha 인증 (헬퍼 메서드 사용)
        verifyHcaptcha(captchaToken);

        // 2. 게시물 유효성 확인 (삭제되지 않은 게시물인지) (헬퍼 메서드 사용)
        RecipientEntity parentLetter = getActiveRecipient(letterSeq);

        // 3. HTML 태그 필터링 및 내용 검증 (헬퍼 메서드 사용)
        String finalContents = cleanAndValidateCommentContents(requestDto.getCommentContents());

        // 4. DTO를 Entity로 변환 (댓글 내용, 작성자, 비밀번호 설정)
        RecipientCommentEntity commentEntity = requestDto.toEntity();
        commentEntity.setLetterSeq(parentLetter); // 부모 게시물 엔티티 연결
        commentEntity.setCommentContents(finalContents); // 클린한 내용으로 설정

        // 5. writerId는 사용하지 않을 경우, 필요하다면 여기에 null 또는 특정 값 설정
        commentEntity.setWriterId(null); // 사용하지 않을 필드라면 null 처리

        RecipientCommentEntity savedComment = recipientCommentRepository.save(commentEntity);
        logger.info("댓글 성공적으로 등록됨: commentSeq={}", savedComment.getCommentSeq());
        return RecipientCommentResponseDto.fromEntity(savedComment);
    }

    // 댓글 수정
    @Override
    public RecipientCommentResponseDto updateComment(int commentSeq, String newContents, String newWriter, String inputPasscode, String captchaToken) {
        logger.info("댓글 수정 요청 시작: commentSeq={}", commentSeq);

        // 1. hCaptcha 인증 (헬퍼 메서드 사용)
        verifyHcaptcha(captchaToken);

        // 2. 삭제되지 않은 기존 댓글 조회 (헬퍼 메서드 사용)
        RecipientCommentEntity existingComment = getActiveComment(commentSeq);

        // 3. 비밀번호 검증 (헬퍼 메서드 사용)
        validateCommentPasscode(existingComment, inputPasscode);

        // 4. 입력받은 데이터로 댓글 정보 업데이트
        // HTML 태그 필터링 및 내용 검증 (헬퍼 메서드 사용)
        String finalContents = cleanAndValidateCommentContents(newContents);

        existingComment.setCommentContents(finalContents);
        existingComment.setCommentWriter(newWriter); // 작성자 수정 허용

        // 5. 업데이트된 댓글 저장
        RecipientCommentEntity updatedComment = recipientCommentRepository.save(existingComment);
        logger.info("댓글 성공적으로 수정됨: commentSeq={}", updatedComment.getCommentSeq());
        return RecipientCommentResponseDto.fromEntity(updatedComment);
    }

    // 댓글 삭제
    @Override
    public void deleteComment(Integer letterSeq, Integer commentSeq, String inputPasscode, String captchaToken) {
        logger.info("댓글 삭제 요청 시작: commentSeq={}", commentSeq);

        // 1. hCaptcha 인증 (헬퍼 메서드 사용)
        verifyHcaptcha(captchaToken);

        // 2. 삭제되지 않은 기존 댓글 조회 (헬퍼 메서드 사용)
        RecipientCommentEntity existingComment = getActiveComment(commentSeq);

        // 3. 비밀번호 확인 (헬퍼 메서드 사용)
        validateCommentPasscode(existingComment, inputPasscode);

        // 4. 댓글 소프트 삭제
        existingComment.setDelFlag("Y");
        recipientCommentRepository.save(existingComment);

        logger.info("댓글 성공적으로 삭제됨: commentSeq={}", commentSeq);
    }
}