package kodanect.common.validation;

import kodanect.domain.remembrance.exception.InvalidDonateSeqException;

public class DonateSeqValidator {

    private DonateSeqValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void validateDonateSeq(Integer donateSeq) throws InvalidDonateSeqException{
        /* 게시글 ID 검증 */
        if(donateSeq == null || donateSeq < 1) {
            throw new InvalidDonateSeqException();
        }
    }
}
