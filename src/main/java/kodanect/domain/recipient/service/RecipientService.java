package kodanect.domain.recipient.service;

import kodanect.domain.recipient.dto.*;

import java.util.List;

public interface RecipientService {
    // 게시물 비밀번호 확인
    boolean verifyLetterPassword(Integer letterSeq, String letterPasscode);

    // 게시물 수정
    RecipientDetailResponseDto updateRecipient(Integer letterSeq, String requestPasscode, RecipientRequestDto requestDto);

    // 게시물 삭제
    // 조건 : 등록된 게시물의 비밀번호와 일치하는 경우
    void deleteRecipient(Integer letterSeq, String letterPasscode, String captchaToken);

    // 게시물 등록
    // 조건 : letter_writer 한영자 10자 제한, letter_passcode 영숫자 8자 이상, 캡챠 인증
    RecipientDetailResponseDto insertRecipient(RecipientRequestDto requestDto);

    // 특정 게시물 조회
    RecipientDetailResponseDto selectRecipient(int letterSeq);

    // 특정 게시물의 페이징된 댓글 조회 (새로운 구현)
    List<RecipientCommentResponseDto> selectPaginatedCommentsForRecipient(int letterSeq, Integer lastCommentId, int size);

    // "더 보기" 기능을 위한 게시물 목록 조회
    // lastId가 null이면 첫 페이지, 아니면 lastId보다 작은 게시물 조회 (최신순)
    List<RecipientListResponseDto> selectRecipientList(RecipientSearchCondition searchCondition, Integer lastId, int size);

    // 제목, 내용, 전체 검색
    List<RecipientListResponseDto> selectRecipientList(RecipientSearchCondition searchCondition);

    // 제목, 내용, 전체 검색 결과 수
    int selectRecipientListTotCnt(RecipientSearchCondition searchCondition);
}
