package kodanect.domain.recipient.service;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.recipient.dto.RecipientDetailResponseDto;
import kodanect.domain.recipient.dto.RecipientListResponseDto;
import kodanect.domain.recipient.dto.RecipientRequestDto;
import kodanect.domain.recipient.dto.RecipientSearchCondition;

public interface RecipientService {
    // 게시물 비밀번호 확인
    boolean verifyLetterPassword(Integer letterSeq, String letterPasscode);

    // 게시물 수정
    RecipientDetailResponseDto updateRecipient(Integer letterSeq, String requestPasscode, RecipientRequestDto requestDto);

    // 게시물 삭제
    // 조건 : 등록된 게시물의 비밀번호와 일치하는 경우
    void deleteRecipient(Integer letterSeq, String letterPasscode);

    // 게시물 등록
    // 조건 : letter_writer 한영자 10자 제한, letter_passcode 영숫자 8자 이상, 캡챠 인증
    RecipientDetailResponseDto insertRecipient(RecipientRequestDto requestDto);

    // 특정 게시물 조회
    RecipientDetailResponseDto selectRecipient(int letterSeq);

    CursorPaginationResponse<RecipientListResponseDto, Long> selectRecipientList(
            RecipientSearchCondition searchCondition,
            Integer lastId,
            int size);

    int selectRecipientListTotCnt(RecipientSearchCondition searchCondition);
}
