package kodanect.domain.recipient.service;

import kodanect.domain.recipient.dto.RecipientResponseDto;
import kodanect.domain.recipient.entity.RecipientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecipientService {
    // 게시물 비밀번호 확인
    boolean verifyLetterPassword(Integer letterSeq, String letterPasscode);

    // 게시물 수정
    RecipientResponseDto updateRecipient(RecipientEntity recipientEntityRequest, Integer letterSeq, String requestPasscode);

    // 게시물 삭제
    // 조건 : 등록된 게시물의 비밀번호와 일치하는 경우
    void deleteRecipient(Integer letterSeq, String letterPasscode);

    // 게시물 등록
    // 조건 : letter_writer 한영자 10자 제한, letter_passcode 영숫자 8자 이상, 캡챠 인증
//    @Transactional
    RecipientResponseDto insertRecipient(RecipientEntity recipientEntityRequest);

    // 특정 게시물 조회
    RecipientResponseDto selectRecipient(int letterSeq);

    // 페이징 처리된 게시물 목록 조회 (댓글 수 포함)
    Page<RecipientResponseDto> selectRecipientListPaged(RecipientEntity searchCondition, Pageable pageable);

    // 제목, 내용, 전체 검색
    List<RecipientResponseDto> selectRecipientList(RecipientEntity searchCondition);

    // 제목, 내용, 전체 검색 결과 수
    int selectRecipientListTotCnt(RecipientEntity searchCondition);
}
