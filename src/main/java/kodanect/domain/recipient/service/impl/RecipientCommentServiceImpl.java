package kodanect.domain.recipient.service.impl;

import kodanect.domain.recipient.exception.RecipientCommentNotFoundException;
import kodanect.domain.recipient.exception.RecipientInvalidDataException;
import kodanect.domain.recipient.exception.RecipientInvalidPasscodeException;
import kodanect.domain.recipient.exception.RecipientNotFoundException;
import kodanect.common.util.HcaptchaService;
import kodanect.domain.recipient.dto.RecipientCommentRequestDto;
import kodanect.domain.recipient.dto.RecipientCommentResponseDto;
import kodanect.domain.recipient.entity.RecipientCommentEntity;
import kodanect.domain.recipient.entity.RecipientEntity;
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

    // 비밀번호 영숫자 8자 이상
    private static final String COMMENT_PASSCODE_PATTERN = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$";
    // 중복되는 에러 메시지
    private static final String COMMENT_NOT_FOUND_MESSAGE = "댓글을 찾을 수 없거나 이미 삭제되었습니다.";
    // 캡차 에러 메시지
    private static final String CAPTCHA_FAILED_MESSAGE = "캡차 인증에 실패했습니다. 다시 시도해주세요.";

    @Resource(name = "recipientCommentRepository")
    private final RecipientCommentRepository recipientCommentRepository;
    private final RecipientRepository recipientRepository;
    private final HcaptchaService hcaptchaService;

    private final Logger logger = LoggerFactory.getLogger(RecipientCommentServiceImpl.class); // 로거 선언

    // 게시물 삭제 여부를 확인하는 헬퍼 메서드
    private RecipientEntity getActiveRecipient(Integer letterSeq) {
        return recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag())) // 삭제되지 않은 게시물만 필터링
                .orElseThrow(() -> new RecipientNotFoundException("게시물을 찾을 수 없거나 이미 삭제된 게시물입니다: " + letterSeq));
    }

    // 특정 게시물의 댓글 조회
    @Override
    public List<RecipientCommentResponseDto> selectRecipientCommentByLetterSeq(int letterSeq) {
        logger.info("Selecting comments for letterSeq: {}", letterSeq);

        // 1. 해당 게시물이 삭제되지 않았는지 확인
        // 게시물이 없거나 삭제되었다면 RecipientNotFoundException 발생
        RecipientEntity activeRecipient = getActiveRecipient(letterSeq);

        // 2. 삭제되지 않은 댓글만 조회하고, 작성시간 기준 오름차순으로 정렬
        List<RecipientCommentEntity> comments = recipientCommentRepository.findByLetterSeqAndDelFlagOrderByWriteTimeAsc(activeRecipient, "N"); // "N"으로 비교

        return comments.stream()
                .map(RecipientCommentResponseDto::fromEntity) // Entity를 Response DTO로 변환
                .toList();
    }

    // 댓글 작성
    @Override
    public RecipientCommentResponseDto insertComment(int letterSeq, RecipientCommentRequestDto requestDto, String captchaToken) {
        // 로그 출력: requestDto.getLetterSeq() 대신 letterSeq 파라미터 사용
        logger.info("Inserting comment for letterSeq: {}", letterSeq);

        // 1. hCaptcha 인증
        if (!hcaptchaService.verifyCaptcha(captchaToken)) {
            logger.warn(CAPTCHA_FAILED_MESSAGE);
            throw new RecipientInvalidDataException(CAPTCHA_FAILED_MESSAGE);
        }
        logger.info("hCaptcha 인증 성공. 댓글 등록 진행.");

        // 2. 게시물 유효성 확인 (삭제되지 않은 게시물인지)
        // requestDto.getLetterSeq() 대신 letterSeq 파라미터 사용
        RecipientEntity parentLetter = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new RecipientNotFoundException("댓글을 등록할 게시물을 찾을 수 없거나 이미 삭제되었습니다."));

        // 3. HTML 태그 필터링 및 내용 검증
        String originalContents = requestDto.getCommentContents();
        Safelist safelist = Safelist.relaxed();
        String cleanContents = Jsoup.clean(originalContents, safelist);
        if (cleanContents.trim().isEmpty()) {
            logger.warn("댓글 작성 실패: 필터링 후 내용이 비어있음");
            throw new RecipientInvalidDataException("댓글 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");
        }

        // 4. DTO를 Entity로 변환 (댓글 내용, 작성자, 비밀번호 설정)
        RecipientCommentEntity commentEntity = requestDto.toEntity();
        commentEntity.setLetterSeq(parentLetter); // 부모 게시물 엔티티 연결
        commentEntity.setCommentContents(cleanContents.trim()); // 클린한 내용으로 설정

        // 5. writerId는 사용하지 않을 경우, 필요하다면 여기에 null 또는 특정 값 설정
        commentEntity.setWriterId(null); // 사용하지 않을 필드라면 null 처리

        RecipientCommentEntity savedComment = recipientCommentRepository.save(commentEntity);
        logger.info("댓글 성공적으로 등록됨: commentSeq={}", savedComment.getCommentSeq());
        return RecipientCommentResponseDto.fromEntity(savedComment);
    }

    // 댓글 수정
    @Override
    public RecipientCommentResponseDto updateComment(int commentSeq, String newContents, String newWriter, String inputPasscode, String captchaToken) {
        logger.info("Attempting to update commentSeq: {}", commentSeq);

        // hCaptcha 인증
        if (!hcaptchaService.verifyCaptcha(captchaToken)) {
            logger.warn(CAPTCHA_FAILED_MESSAGE );
            throw new RecipientInvalidDataException(CAPTCHA_FAILED_MESSAGE);
        }
        logger.info("hCaptcha 인증 성공. 댓글 수정 진행.");

        // 1. 삭제되지 않은 기존 댓글 조회
        RecipientCommentEntity existingComment = recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq, "N")
                .orElseThrow(() -> new RecipientCommentNotFoundException(COMMENT_NOT_FOUND_MESSAGE));

        // 2. 비밀번호 검증
        if (!existingComment.checkPasscode(inputPasscode)) {
            logger.warn("댓글 비밀번호 불일치: commentSeq={}", commentSeq);
            throw new RecipientInvalidPasscodeException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 입력받은 데이터로 댓글 정보 업데이트
        // HTML 태그 필터링
        Safelist safelist = Safelist.relaxed();
        String cleanContents = Jsoup.clean(newContents, safelist);
        if (cleanContents.trim().isEmpty()) {
            logger.warn("댓글 수정 실패: 필터링 후 내용이 비어있음");
            throw new RecipientInvalidDataException("댓글 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");
        }

        existingComment.setCommentContents(cleanContents.trim());
        existingComment.setCommentWriter(newWriter); // 작성자 수정 허용
        // existingComment.setModifierId(null); // @Transient 처리하여 DB에 저장되지 않음. 여기서는 값을 설정할 필요 없음.

        // 4. 업데이트된 댓글 저장
        RecipientCommentEntity updatedComment = recipientCommentRepository.save(existingComment);
        logger.info("댓글 성공적으로 수정됨: commentSeq={}", updatedComment.getCommentSeq());
        return RecipientCommentResponseDto.fromEntity(updatedComment);
    }

    // 댓글 삭제
    @Override
    public void deleteComment(Integer letterSeq, Integer commentSeq, String inputPasscode, String captchaToken) {
        logger.info("Attempting to delete commentSeq: {}", commentSeq);

        // hCaptcha 인증
        if (!hcaptchaService.verifyCaptcha(captchaToken)) {
            logger.warn(CAPTCHA_FAILED_MESSAGE );
            throw new RecipientInvalidDataException(CAPTCHA_FAILED_MESSAGE);
        }
        logger.info("hCaptcha 인증 성공. 댓글 삭제 진행.");

        // 1. 삭제되지 않은 기존 댓글 조회
        RecipientCommentEntity existingComment = recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq, "N")
                .orElseThrow(() -> new RecipientCommentNotFoundException(COMMENT_NOT_FOUND_MESSAGE));

        // 2. 비밀번호 확인
        if (!existingComment.checkPasscode(inputPasscode)) {
            logger.warn("댓글 삭제 실패: 비밀번호 불일치 (commentSeq={})", commentSeq);
            throw new RecipientInvalidPasscodeException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 댓글 소프트 삭제
        existingComment.setDelFlag("Y");
        recipientCommentRepository.save(existingComment);

        logger.info("댓글 성공적으로 삭제됨: commentSeq={}", commentSeq);
    }
}
