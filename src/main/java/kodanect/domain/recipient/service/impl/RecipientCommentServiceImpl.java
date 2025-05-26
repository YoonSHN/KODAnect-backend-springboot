package kodanect.domain.recipient.service.impl;

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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("recipientCommentService")
@RequiredArgsConstructor // final 필드에 대한 생성자 주입
public class RecipientCommentServiceImpl implements RecipientCommentService {

    @Resource(name = "recipientCommentRepository")
    private final RecipientCommentRepository recipientCommentRepository;
    private final RecipientRepository recipientRepository;

    private static final Logger logger = LoggerFactory.getLogger(RecipientCommentServiceImpl.class); // 로거 선언

    // 특정 게시물의 댓글 조회
    @Transactional(readOnly = true)
    @Override
    public List<RecipientCommentResponseDto> selectRecipientCommentByLetterSeq(int letterSeq) throws Exception{
        logger.info("Selecting comments for letterSeq: {}", letterSeq);
        // 삭제되지 않은 댓글만 조회하고, 작성시간 기준 오름차순으로 정렬
        List<RecipientCommentEntity> comments = recipientCommentRepository.findByLetter_LetterSeqAndDelFlagOrderByWriteTimeAsc(letterSeq, "N"); // "N"으로 비교

        return comments.stream()
                .map(RecipientCommentResponseDto::fromEntity) // Entity를 Response DTO로 변환
                .collect(java.util.stream.Collectors.toList());
    }

    // 댓글 작성
    @Transactional
    @Override
    public RecipientCommentResponseDto insertComment(RecipientCommentEntity commentEntityRequest) throws Exception {
        logger.info("Inserting comment for letterSeq: {}", commentEntityRequest.getLetter().getLetterSeq());

        // 1. 댓글을 달 게시물(RecipientVO)이 실제로 존재하는지 확인
        Integer letterSeq = Optional.ofNullable(commentEntityRequest.getLetter())
                .map(RecipientEntity::getLetterSeq)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 달 게시물 정보가 누락되었습니다."));

        RecipientEntity recipientEntity = recipientRepository.findById(letterSeq)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다: " + letterSeq));

        if ("Y".equalsIgnoreCase(recipientEntity.getDelFlag())) { // delflag가 'Y'이면 삭제된 게시물
            logger.warn("댓글 작성 실패: 삭제된 게시물에 댓글을 달 수 없습니다. letterSeq: {}", letterSeq);
            throw new IllegalArgumentException("삭제된 게시물에는 댓글을 달 수 없습니다.");
        }

        // 2. 댓글 저장을 위한 부모레터 세팅
        commentEntityRequest.setLetter(recipientEntity); // JPA 연관관계 설정을 위해 실제 엔티티 참조

        // 3. 댓글 비밀번호 유효성 검사 (필수 입력, 영문 숫자 8자 이상)
        if (commentEntityRequest.getCommentPasscode() == null || !commentEntityRequest.getCommentPasscode().matches("^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$")) {
            logger.warn("댓글 작성 실패: 비밀번호 유효성 검사 실패");
            throw new Exception("비밀번호는 영문 숫자 8자 이상 이어야 합니다.");
        }

        // 4. 댓글 내용 유효성 검사 (필수 입력, HTML 필터링, 길이 제한)
        if (commentEntityRequest.getContents() == null || commentEntityRequest.getContents().trim().isEmpty()) {
            logger.warn("댓글 작성 실패: 내용이 비어있음");
            throw new Exception("댓글 내용은 필수 입력 항목입니다.");
        }
        // Jsoup을 사용하여 댓글 내용 HTML 필터링
        Safelist commentSafelist = Safelist.none();
        commentSafelist.addTags("br"); // 줄바꿈 태그만 허용 (댓글에 주로 사용)
        String cleanCommentContents = Jsoup.clean(commentEntityRequest.getContents(), commentSafelist);
        // 필터링 후 내용이 비어있는지 다시 확인
        if (cleanCommentContents.trim().isEmpty()) {
            logger.warn("댓글 작성 실패: 필터링 후 내용이 비어있음");
            throw new Exception("댓글 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");
        }
        commentEntityRequest.setContents(cleanCommentContents); // 필터링된 내용으로 설정

        RecipientCommentEntity savedComment = recipientCommentRepository.save(commentEntityRequest);
        logger.info("Comment inserted with commentSeq: {}", savedComment.getCommentSeq());
        return RecipientCommentResponseDto.fromEntity(savedComment);    // 저장된 Entity를 Response DTO로 변환하여 반환
    }

    // 댓글 수정
    @Transactional
    @Override
    public RecipientCommentResponseDto updateComment(RecipientCommentEntity commentEntityRequest, String inputPassword) throws Exception {
        logger.info("Attempting to update commentSeq: {}", commentEntityRequest.getCommentSeq());

        // 삭제되지 않은 기존 댓글 조회
        RecipientCommentEntity existingComment = recipientCommentRepository.findByCommentSeqAndDelFlag(commentEntityRequest.getCommentSeq(), "N")
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없거나 이미 삭제되었습니다."));

        // 비밀번호 검증
        if (!existingComment.getCommentPasscode().equals(inputPassword)) {
            logger.warn("Password mismatch for commentSeq: {}", commentEntityRequest.getCommentSeq());
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 수정 필드 업데이트 및 HTML 필터링
        String cleanContents = Jsoup.clean(commentEntityRequest.getContents(), Safelist.none().addTags("br"));
        if (cleanContents.trim().isEmpty()) {
            logger.warn("댓글 수정 실패: 필터링 후 내용이 비어있음");
            throw new IllegalArgumentException("수정할 댓글 내용은 필수 입력 항목입니다. (HTML 태그 필터링 후)");
        }
        existingComment.setContents(cleanContents);
        existingComment.setModifyTime(LocalDateTime.now());
        existingComment.setModifierId(commentEntityRequest.getModifierId());

        RecipientCommentEntity updatedComment = recipientCommentRepository.save(existingComment);
        logger.info("Comment updated for commentSeq: {}", updatedComment.getCommentSeq());
        return RecipientCommentResponseDto.fromEntity(updatedComment); // 업데이트된 Entity를 Response DTO로 변환
    }

    // 댓글 삭제
    @Transactional
    @Override
    public void deleteComment(int commentSeq, String inputPassword) throws Exception {
        logger.info("Attempting to delete commentSeq: {}", commentSeq);
        RecipientCommentEntity existingComment = recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq,"N")
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없거나 이미 삭제되었습니다."));

        // 비밀번호 검증
        if (!existingComment.getCommentPasscode().equals(inputPassword)) {
            logger.warn("Password mismatch for commentSeq: {}", commentSeq);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 논리적 삭제 (delFlag를 "Y"로 설정)
        existingComment.setDelFlag("Y");
        existingComment.setModifyTime(LocalDateTime.now()); // 삭제 시간 기록
        existingComment.setModifierId(existingComment.getCommentWriter());
        recipientCommentRepository.save(existingComment);   // 변경된 상태 저장
        logger.info("Comment logically deleted for commentSeq: {}, modifierId set to: {}", commentSeq, existingComment.getModifierId());
    }

    // 댓글 비밀번호 확인
    @Transactional(readOnly = true)
    @Override
    public boolean verifyCommentPassword(int commentSeq, String inputPassword) throws Exception {
        logger.info("Verifying password for commentSeq: {}", commentSeq);
        // commentSeq로 댓글을 찾고, 삭제되지 않았는지 확인하며 비밀번호 일치 여부 반환
        return recipientCommentRepository.findByCommentSeqAndDelFlag(commentSeq, "N")
                .filter(comment -> "N".equalsIgnoreCase(comment.getDelFlag())) // 삭제되지 않은 댓글만
                .map(comment -> comment.getCommentPasscode().equals(inputPassword))
                .orElse(false); // 댓글이 없거나 삭제되었거나 비밀번호 불일치 시 false
    }


}
