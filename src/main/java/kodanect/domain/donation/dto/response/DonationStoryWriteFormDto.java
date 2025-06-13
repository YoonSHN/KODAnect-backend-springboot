package kodanect.domain.donation.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/*
글쓰기 페이지 응답용 데이터
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DonationStoryWriteFormDto {

    private List<AreaCode> areaOptions; //db에 저장된 권역 정보
}
