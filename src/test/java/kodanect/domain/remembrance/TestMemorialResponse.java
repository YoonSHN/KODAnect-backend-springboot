package kodanect.domain.remembrance;

import kodanect.common.util.FormatUtils;
import kodanect.domain.remembrance.dto.MemorialResponse;

public class TestMemorialResponse implements MemorialResponse {

    private final Integer donateSeq;
    private final String donorName;
    private final String donateDate;
    private final String genderFlag;
    private final Integer donateAge;
    private final long commentCount;
    private final long letterCount;

    public TestMemorialResponse(Integer donateSeq, String donorName, String donateDate,
                                String genderFlag, Integer donateAge, long commentCount, long letterCount) {
        this.donateSeq = donateSeq;
        this.donorName = donorName;
        this.donateDate = donateDate;
        this.genderFlag = genderFlag;
        this.donateAge = donateAge;
        this.commentCount = commentCount;
        this.letterCount = letterCount;
    }

    @Override public Integer getDonateSeq() { return donateSeq; }
    @Override public String getDonorName() { return donorName; }
    @Override public String getGenderFlag() { return genderFlag; }
    @Override public Integer getDonateAge() { return donateAge; }
    @Override public long getCommentCount() { return commentCount; }
    @Override public long getLetterCount() { return letterCount; }

    @Override
    public String getDonateDate() {
        return FormatUtils.formatDonateDate(donateDate); // "20230101" → "2023-01-01"
    }
}
