package kodanect.domain.recipient.service;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.recipient.dto.RecipientDetailResponseDto;
import kodanect.domain.recipient.dto.RecipientListResponseDto;
import kodanect.domain.recipient.dto.RecipientRequestDto;
import kodanect.domain.recipient.dto.RecipientSearchCondition;

public interface RecipientService {
    // 게시물 비밀번호 확인
    void verifyLetterPassword(Integer letterSeq, String letterPasscode);

    // 게시물 수정
    RecipientDetailResponseDto updateRecipient(Integer letterSeq, RecipientRequestDto requestDto);

    // 게시물 삭제
    // 조건 : 등록된 게시물의 비밀번호와 일치하는 경우
    void deleteRecipient(Integer letterSeq, String letterPasscode);

    // 게시물 등록
    // 조건 : letter_writer 한영자 10자 제한, letter_passcode 영숫자 8자 이상
    RecipientDetailResponseDto insertRecipient(RecipientRequestDto requestDto);

    // 특정 게시물 조회
    RecipientDetailResponseDto selectRecipient(Integer letterSeq);

    CursorPaginationResponse<RecipientListResponseDto, Integer> selectRecipientList(
            RecipientSearchCondition searchCondition,
            Integer cursor,
            int size);

    int selectRecipientListTotCnt(RecipientSearchCondition searchCondition);
}
