package kodanect.common.util;


import kodanect.domain.remembrance.exception.InvalidContentsException;

/**
 *
 * 기증자 추모관 상세 페이지 내용 형식 포매터
 *
 * */
public class MemorialHtmlNormalizer {

    private static final String TEMPLATE = "<p>기증자 %s (%s, %s) 님은 %s 환자들에게 귀중한 장기를 선물해 주셨습니다.</p>" +
            "<p>한국장기조직기증원은 귀한 생명을 나눠주신 기증자와 유가족께 깊이 감사드리며,<br />앞으로도 기증자 유가족들이 건강한 삶을 유지할 수 있도록 최선을 다해 지원할 것입니다.</p>" +
            "<p>고인의 명복을 빕니다.</p>";

    private MemorialHtmlNormalizer() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String contentsFormat(Integer donateSeq, String name, String gender, Integer age, String date) throws InvalidContentsException {

        if(name == null || gender == null || age == null || date==null || donateSeq == null ||
                name.isBlank() || gender.isBlank() || age < 0 || date.isBlank() || donateSeq <= 0)
        {
            throw new InvalidContentsException(donateSeq, name, gender, date, age);
        }

        gender = genderFormat(gender);
        date = FormatUtils.formatDateForDisplay(date);

        return String.format(TEMPLATE, name, gender, age, date);
    }

    /**
     *
     * 성별 포맷
     * <p> M -> 남 </p>
     * <p> W -> 여 </p>
     *
     * */
    public static String genderFormat(String gender) {
        /* 성별 포매터 */
        if (gender == null || gender.isBlank()) {
            return gender;
        }

        return "M".equalsIgnoreCase(gender) ? "남" : "여";
    }
}
