package kodanect.domain.remembrance.service;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.remembrance.dto.HeavenMemorialResponse;
import kodanect.domain.remembrance.dto.MemorialDetailResponse;
import kodanect.domain.remembrance.dto.MemorialResponse;
import kodanect.domain.remembrance.dto.common.MemorialNextCursor;
import kodanect.domain.remembrance.exception.*;
import org.springframework.data.domain.Page;

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
    CursorPaginationResponse<MemorialResponse, MemorialNextCursor> getSearchMemorialList(String startDate, String endDate, String keyWord, MemorialNextCursor cursor, int size);
    /**
     * 하늘나라 편지 팝업 조회 ->
     * 기증자 추모관 게시글 검색 조회 메서드
     *
     * @param startDate 시작 일
     * @param endDate 종료 일
     * @param keyWord 검색할 문자
     * @param page 페이지 번호
     * @param size 페이지 사이즈
     *
     * */
    Page<HeavenMemorialResponse> getSearchHeavenMemorialList(String startDate, String endDate, String keyWord, Integer page, int size);
    /**
     *
     * 기증자 추모관 게시글 조회 메서드
     *
     * @param nextCursor 조회할 댓글 페이지 번호(이 ID보다 작은 번호의 댓글을 조회)
     * @param size 조회할 댓글 페이지 사이즈
     * @return 조건에 맞는 게시글 리스트(최신순)
     *
     * */
    CursorPaginationResponse<MemorialResponse, MemorialNextCursor> getMemorialList(MemorialNextCursor nextCursor, int size);
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
