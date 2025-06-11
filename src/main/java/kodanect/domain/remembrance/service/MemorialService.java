package kodanect.domain.remembrance.service;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.remembrance.dto.MemorialDetailResponse;
import kodanect.domain.remembrance.dto.MemorialResponse;
import kodanect.domain.remembrance.exception.*;

public interface MemorialService {
    /**
     *
     * 기증자 추모관 이모지 카운팅 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @param emotion  추가 카운트 될 이모지
     *
     * */
    void emotionCountUpdate(Integer donateSeq, String emotion)
            throws  InvalidEmotionTypeException,
                    MemorialNotFoundException;
    /**
     *
     * 기증자 추모관 게시글 검색 조건 조회 메서드
     *
     * @param startDate 시작 일
     * @param endDate 종료 일
     * @param keyWord 검색 문자
     * @param cursor 조회할 댓글 페이지 번호(이 ID보다 작은 번호의 댓글을 조회)
     * @param size 조회할 댓글 페이지 사이즈
     * @return 조건에 맞는 게시글 리스트(최신순)
     *
     * */
    CursorPaginationResponse<MemorialResponse, Integer> getSearchMemorialList(String startDate, String endDate, String keyWord, Integer cursor, int size);
    /**
     *
     * 기증자 추모관 게시글 조회 메서드
     *
     * @param cursor 조회할 댓글 페이지 번호(이 ID보다 작은 번호의 댓글을 조회)
     * @param size 조회할 댓글 페이지 사이즈
     * @return 조건에 맞는 게시글 리스트(최신순)
     *
     * */
    CursorPaginationResponse<MemorialResponse, Integer> getMemorialList(Integer cursor, int size);
    /**
     *
     * 기증자 추모관 게시글 상세 조회 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @return 조건에 맞는 게시글
     *
     * */
    MemorialDetailResponse getMemorialByDonateSeq(Integer donateSeq)
            throws  MemorialNotFoundException;
}
