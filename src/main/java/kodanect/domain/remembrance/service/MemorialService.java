package kodanect.domain.remembrance.service;

import kodanect.domain.remembrance.dto.MemorialDetailDto;
import kodanect.domain.remembrance.dto.MemorialListDto;
import org.springframework.data.domain.Page;

public interface MemorialService {
    /* 이모지 카운트 수 업데이트 */
    void emotionCountUpdate(Integer donateSeq, String emotion) throws Exception;
    /* 게시글 검색 조건 조회 */
    Page<MemorialListDto> getSearchMemorialList(String page, String size, String startDate, String endDate, String searchWord) throws Exception;
    /* 게시글 리스트 조회 */
    Page<MemorialListDto> getMemorialList(String page, String size) throws Exception;
    /* 게시글 상세 조회 */
    MemorialDetailDto getMemorialByDonateSeq(Integer donateSeq) throws Exception;
}
