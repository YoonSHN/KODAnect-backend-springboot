package kodanect.domain.remembrance.service;

import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.remembrance.dto.MemorialDetailResponse;
import kodanect.domain.remembrance.dto.MemorialResponse;
import kodanect.domain.remembrance.exception.*;

public interface MemorialService {
    /* 이모지 카운트 수 업데이트 */
    void emotionCountUpdate(Integer donateSeq, String emotion)
            throws  InvalidEmotionTypeException,
                    MemorialNotFoundException,
                    InvalidDonateSeqException;
    /* 게시글 검색 조건 조회 */
    CursorPaginationResponse<MemorialResponse> getSearchMemorialList(Integer cursor, int size, String startDate, String endDate, String searchWord)
            throws  InvalidPaginationRangeException,
                    MissingSearchDateParameterException,
                    InvalidSearchDateFormatException,
                    InvalidSearchDateRangeException;
    /* 게시글 리스트 조회 */
    CursorPaginationResponse<MemorialResponse> getMemorialList(Integer cursor, int size)
            throws  InvalidPaginationRangeException;
    /* 게시글 상세 조회 */
    MemorialDetailResponse getMemorialByDonateSeq(Integer donateSeq)
            throws  MemorialNotFoundException,
                    InvalidDonateSeqException;
}
