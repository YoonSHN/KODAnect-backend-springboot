package kodanect.domain.remembrance;

import kodanect.common.util.FormatUtils;
import kodanect.domain.remembrance.dto.HeavenMemorialResponse;

public class TestHeavenMemorialResponse implements HeavenMemorialResponse {

    private final Integer donateSeq;
    private final String donorName;
    private final String donateDate;
    private final String genderFlag;
    private final Integer donateAge;

    public TestHeavenMemorialResponse(Integer donateSeq, String donorName, String donateDate,
                                String genderFlag, Integer donateAge) {
        this.donateSeq = donateSeq;
        this.donorName = donorName;
        this.donateDate = donateDate;
        this.genderFlag = genderFlag;
        this.donateAge = donateAge;
    }

    @Override public Integer getDonateSeq() { return donateSeq; }
    @Override public String getDonorName() { return donorName; }
    @Override public String getGenderFlag() { return genderFlag; }
    @Override public Integer getDonateAge() { return donateAge; }

    @Override
    public String getDonateDate() {
        return FormatUtils.formatDonateDate(donateDate); // "20230101" → "2023-01-01"
    }
}
