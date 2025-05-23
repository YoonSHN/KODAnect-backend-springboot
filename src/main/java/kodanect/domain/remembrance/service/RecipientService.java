package kodanect.domain.remembrance.service;

import kodanect.domain.remembrance.dto.RecipientResponseDto;
import kodanect.domain.remembrance.entity.RecipientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public interface RecipientService {
    // 게시물 비밀번호 확인
    @Transactional
    boolean verifyLetterPassword(Integer letterSeq, String letterPasscode) throws Exception;

    // 게시물 수정
    @Transactional
    RecipientResponseDto updateRecipient(RecipientEntity recipientEntityRequest, Integer letterSeq, String requestPasscode) throws Exception;

    // 게시물 삭제
    // 조건 : 등록된 게시물의 비밀번호와 일치하는 경우
    @Transactional
    void deleteRecipient(Integer letterSeq, String letterPasscode) throws Exception;

    // 게시물 등록
    // 조건 : letter_writer 한영자 10자 제한, letter_passcode 영숫자 8자 이상, 캡챠 인증
    @Transactional
    RecipientResponseDto insertRecipient(RecipientEntity recipientEntityRequest) throws Exception;

    // 특정 게시물 조회
    @Transactional
    RecipientResponseDto selectRecipient(int letterSeq) throws Exception;

    // 페이징 처리된 게시물 목록 조회 (댓글 수 포함)
    @Transactional(readOnly = true)
    Page<RecipientResponseDto> selectRecipientListPaged(RecipientEntity searchCondition, Pageable pageable) throws Exception;

    // 제목, 내용, 전체 검색
    List<RecipientResponseDto> selectRecipientList(RecipientEntity searchCondition) throws Exception;

    // 제목, 내용, 전체 검색 결과 수
    int selectRecipientListTotCnt(RecipientEntity searchCondition) throws Exception;

    // Specification 조건 생성
    default Specification<RecipientEntity> getRecipientSpecification(RecipientEntity searchCondition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 삭제되지 않은 게시물만 조회
            predicates.add(cb.equal(root.get("delFlag"), false));

            // 제목 조건
            if (StringUtils.hasText(searchCondition.getLetterTitle())) {
                predicates.add(cb.like(root.get("letterTitle"), "%" + searchCondition.getLetterTitle() + "%"));
            }

            // 내용 조건
            if (StringUtils.hasText(searchCondition.getLetterContents())) {
                predicates.add(cb.like(root.get("letterContents"), "%" + searchCondition.getLetterContents() + "%"));
            }

            // 전체 검색 조건 (제목 또는 내용)
            if (StringUtils.hasText(searchCondition.getSearchKeyword())) {
                Predicate titlePredicate = cb.like(root.get("letterTitle"), "%" + searchCondition.getSearchKeyword() + "%");
                Predicate contentPredicate = cb.like(root.get("letterContents"), "%" + searchCondition.getSearchKeyword() + "%");
                predicates.add(cb.or(titlePredicate, contentPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
