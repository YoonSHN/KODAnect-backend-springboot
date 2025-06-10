package kodanect.domain.recipient.service.impl;

import kodanect.common.response.CursorReplyPaginationResponse;
import kodanect.common.util.CursorFormatter;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

import static kodanect.common.exception.config.MessageKeys.RECIPIENT_NOT_FOUND;

@Service("recipientCommentService")
@RequiredArgsConstructor // final 필드에 대한 생성자 주입
public class RecipientCommentServiceImpl implements RecipientCommentService {

    // 중복되는 에러 메시지
    private static final String COMMENT_NOT_FOUND_MESSAGE = "댓글을 찾을 수 없거나 이미 삭제되었습니다.";
    private static final String RECIPIENT_NOT_FOUND_MESSAGE = "게시물을 찾을 수 없거나 이미 삭제된 게시물입니다.";

    // 필드명 상수 (Specification에서 사용)
    private static final String LETTER_SEQ = "letterSeq";
    private static final String DEL_FLAG = "delFlag";
    private static final String COMMENT_SEQ = "commentSeq";

    @Resource(name = "recipientCommentRepository")
    private final RecipientCommentRepository recipientCommentRepository;
    private final RecipientRepository recipientRepository;

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
                    return new RecipientNotFoundException(RECIPIENT_NOT_FOUND, letterSeq);
                });
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
                    return new RecipientCommentNotFoundException(commentSeq);
                });
    }

    // --- 서비스 메서드 구현 ---

    // 댓글 작성
    @Override
    public RecipientCommentResponseDto insertComment(Integer letterSeq, RecipientCommentRequestDto requestDto) {
        logger.info("댓글 작성 요청 시작: letterSeq={}", letterSeq);

        // 1. 게시물 유효성 확인 (삭제되지 않은 게시물인지) (헬퍼 메서드 사용)
        RecipientEntity parentLetter = getActiveRecipient(letterSeq);

        // 2. HTML 태그 필터링 및 내용 검증 (헬퍼 메서드 사용)
        String finalContents = cleanAndValidateCommentContents(requestDto.getCommentContents());

        // 3. DTO를 Entity로 변환 (댓글 내용, 작성자, 비밀번호 설정)
        RecipientCommentEntity commentEntity = requestDto.toEntity();
        commentEntity.setLetterSeq(parentLetter); // 부모 게시물 엔티티 연결
        commentEntity.setCommentContents(finalContents); // 클린한 내용으로 설정

        // 4. writerId는 사용하지 않을 경우, 필요하다면 여기에 null 또는 특정 값 설정
        commentEntity.setWriterId(null); // 사용하지 않을 필드라면 null 처리

        RecipientCommentEntity savedComment = recipientCommentRepository.save(commentEntity);
        logger.info("댓글 성공적으로 등록됨: commentSeq={}", savedComment.getCommentSeq());
        return RecipientCommentResponseDto.fromEntity(savedComment);
    }

    // 댓글 수정
    @Override
    public RecipientCommentResponseDto updateComment(Integer commentSeq, String newContents, String newWriter, String inputPasscode) {
        logger.info("댓글 수정 요청 시작: commentSeq={}", commentSeq);

        // 1. 삭제되지 않은 기존 댓글 조회 (헬퍼 메서드 사용)
        RecipientCommentEntity existingComment = getActiveComment(commentSeq);

        // 2. 비밀번호 검증 (헬퍼 메서드 사용)
        validateCommentPasscode(existingComment, inputPasscode);

        // 3. 입력받은 데이터로 댓글 정보 업데이트
        // HTML 태그 필터링 및 내용 검증 (헬퍼 메서드 사용)
        String finalContents = cleanAndValidateCommentContents(newContents);

        existingComment.setCommentContents(finalContents);
        existingComment.setCommentWriter(newWriter); // 작성자 수정 허용

        // 4. 업데이트된 댓글 저장
        RecipientCommentEntity updatedComment = recipientCommentRepository.save(existingComment);
        logger.info("댓글 성공적으로 수정됨: commentSeq={}", updatedComment.getCommentSeq());
        return RecipientCommentResponseDto.fromEntity(updatedComment);
    }

    // 댓글 삭제
    @Override
    public void deleteComment(Integer letterSeq, Integer commentSeq, String inputPasscode) {
        logger.info("댓글 삭제 요청 시작: commentSeq={}", commentSeq);

        // 1. 삭제되지 않은 기존 댓글 조회 (헬퍼 메서드 사용)
        RecipientCommentEntity existingComment = getActiveComment(commentSeq);

        // 2. 비밀번호 확인 (헬퍼 메서드 사용)
        validateCommentPasscode(existingComment, inputPasscode);

        // 3. 댓글 소프트 삭제
        existingComment.setDelFlag("Y");
        recipientCommentRepository.save(existingComment);

        logger.info("댓글 성공적으로 삭제됨: commentSeq={}", commentSeq);
    }

    /**
     * 특정 게시물의 페이징된 댓글 조회 (커서 방식으로 변경)
     * @param letterSeq 게시물 ID
     * @param lastCommentId 마지막으로 조회된 댓글의 ID (커서)
     * @param size 한 번에 가져올 댓글 수
     * @return 커서 기반 페이지네이션 응답 (댓글)
     */
    @Override
    public CursorReplyPaginationResponse<RecipientCommentResponseDto, Integer> selectPaginatedCommentsForRecipient(Integer letterSeq, Integer lastCommentId, Integer size) {
        logger.info("Selecting paginated comments for letterSeq: {}, lastCommentId: {}, size: {}", letterSeq, lastCommentId, size);

        // 1. 해당 게시물이 삭제되지 않았는지 확인
        RecipientEntity activeRecipient = recipientRepository.findById(letterSeq)
                .filter(entity -> "N".equalsIgnoreCase(entity.getDelFlag()))
                .orElseThrow(() -> new RecipientNotFoundException(RECIPIENT_NOT_FOUND, letterSeq));

        // 2. 쿼리할 데이터의 실제 size (클라이언트 요청 size + 1 하여 다음 커서 존재 여부 확인)
        int querySize = size + 1;

        // 3. Specification 생성: 게시물 ID, 삭제 여부, lastCommentId 조건 포함
        Specification<RecipientCommentEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(LETTER_SEQ), activeRecipient));
            predicates.add(cb.equal(root.get(DEL_FLAG), "N"));

            // lastCommentId가 null이 아니고 0이 아니면 커서 조건 추가 (commentSeq는 int 타입이므로 intValue() 사용)
            if (lastCommentId != null && lastCommentId != 0) {
                // 커서 방식에서 commentSeq가 오름차순 정렬이므로, lastCommentId보다 큰 값을 찾습니다.
                predicates.add(cb.greaterThan(root.get(COMMENT_SEQ), lastCommentId.intValue()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 4. 정렬 조건 설정 (commentSeq 기준 오름차순 - 오래된 댓글부터)
        Sort sort = Sort.by(Sort.Direction.ASC, COMMENT_SEQ);

        // 5. Pageable 설정 (PageRequest.of(0, querySize)를 사용하여 limit만 적용)
        Pageable pageable = PageRequest.of(0, querySize, sort);

        // 6. 댓글 조회
        List<RecipientCommentEntity> comments = recipientCommentRepository.findAll(spec, pageable).getContent();

        // 7. Entity를 DTO로 변환하고, 삭제된 댓글은 필터링
        // fromEntity()에서 null을 반환하는 경우가 아니라, 실제 엔티티가 없으면 리스트에 추가되지 않으므로
        // Objects::nonNull 필터링은 필요 없을 수 있습니다.
        // 다만, 혹시 모를 상황에 대비하여 유지하는 것도 나쁘지 않습니다.
        List<RecipientCommentResponseDto> commentResponseDtos = comments.stream()
                .map(RecipientCommentResponseDto::fromEntity)
                // .filter(Objects::nonNull) // fromEntity에서 null 반환할 수 있으므로 null 필터링 (필요시 활성화)
                .toList();

        // 8. CursorFormatter를 사용하여 응답 포맷팅
        return CursorFormatter.cursorReplyFormat(commentResponseDtos, size);
    }
}