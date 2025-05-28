package kodanect.domain.remembrance.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

@Entity(name = "tb25_400_memorial")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class Memorial {

    /* 상수 */
    /* 기증자 명 길이 */
    private static final int DONOR_NAME_MAX_LENGTH = 150;
    /* 플래그 길이 (Y, N) */
    private static final int FLAG_LENGTH = 1;
    /* 추모 제목 길이 */
    private static final int DONATE_TITLE_MAX_LENGTH = 600;
    /* 권역 코드 길이 */
    private static final int AREA_CODE_MAX_LENGTH = 10;
    /* 파일 명 길이 */
    private static final int FILE_NAME_MAX_LENGTH = 600;
    /* 생성자 아이디 길이 */
    private static final int DONATE_DATE_LENGTH = 8;
    /* 생성자 아이디 길이 */
    private static final int WRITER_ID_MAX_LENGTH = 60;

    /* 디폴트 값 */
    /* 초기화 기본값 (0) */
    private static final int DEFAULT_COUNT = 0;
    /* 삭제 여부 기본값 (N) */
    private static final String DEFAULT_DEL_FLAG = "N";

    /* 기증자 일련 번호 */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int donateSeq;

    /* 기증자 명 */
    @Column(nullable = true, length = DONOR_NAME_MAX_LENGTH)
    private String donorName;

    /* 익명 여부 Y, N */
    @Column(nullable = true, length = FLAG_LENGTH)
    private String anonymityFlag;

    /* 추모합니다. */
    @Column(nullable = true, length = DONATE_TITLE_MAX_LENGTH)
    private String donateTitle;

    /* 권역 코드 */
    @Column(nullable = true, length = AREA_CODE_MAX_LENGTH)
    private String areaCode;

    /* 기증자 내용 */
    @Column(nullable = true, columnDefinition = "TEXT")
    private String contents;

    /* 이미지 파일 명 */
    @Column(nullable = true, length = FILE_NAME_MAX_LENGTH)
    private String fileName;

    /* 이미지 원본 파일 명 */
    @Column(nullable = true, length = FILE_NAME_MAX_LENGTH)
    private String orgFileName;

    /* 운영자 */
    @Column(nullable = true, length = DONOR_NAME_MAX_LENGTH)
    private String writer;

    /* 기증자 기증 일시 20120101 */
    @Column(nullable = true, length = DONATE_DATE_LENGTH)
    private String donateDate;

    /* 기증자 성별 */
    @Column(nullable = true, length = FLAG_LENGTH)
    private String genderFlag;

    /* 기증자 나이 */
    @Column(nullable = true)
    private Integer donateAge;

    /* 추모수 헌화 */
    @Column(nullable = true)
    @Builder.Default private int flowerCount = DEFAULT_COUNT;

    /* 추모수 사랑해요 */
    @Column(nullable = true)
    @Builder.Default private int loveCount = DEFAULT_COUNT;

    /* 추모수 보고싶어요 */
    @Column(nullable = true)
    @Builder.Default private int seeCount = DEFAULT_COUNT;

    /* 추모수 그리워요 */
    @Column(nullable = true)
    @Builder.Default private int missCount = DEFAULT_COUNT;

    /* 추모수 자랑스러워요 */
    @Column(nullable = true)
    @Builder.Default private int proudCount = DEFAULT_COUNT;

    /* 추모수 힘들어요 */
    @Column(nullable = true)
    @Builder.Default private int hardCount = DEFAULT_COUNT;

    /* 추모수 슬퍼요 */
    @Column(nullable = true)
    @Builder.Default private int sadCount = DEFAULT_COUNT;

    /* 생성 일시 */
    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime writeTime;

    /* 생성자 아이디 */
    @Column(nullable = false, length = WRITER_ID_MAX_LENGTH)
    private String writerId;

    /* 수정 일시 */
    @Column(nullable = true, insertable = false, updatable = false)
    private LocalDateTime modifyTime;

    /* 수정자 아이디 */
    @Column(nullable = false, length = WRITER_ID_MAX_LENGTH)
    private String modifierId;

    /* 삭제 여부 */
    @Column(nullable = false, length = FLAG_LENGTH)
    @Builder.Default private String delFlag = DEFAULT_DEL_FLAG;

    public void setFlowerCount(int flowerCount) {
        this.flowerCount = flowerCount;
    }
    public void setLoveCount(int loveCount) {
        this.loveCount = loveCount;
    }
    public void setSeeCount(int seeCount) {
        this.seeCount = seeCount;
    }
    public void setMissCount(int missCount) {
        this.missCount = missCount;
    }
    public void setProudCount(int proudCount) {
        this.proudCount = proudCount;
    }
    public void setHardCount(int hardCount) {
        this.hardCount = hardCount;
    }
    public void setSadCount(int sadCount) {
        this.sadCount = sadCount;
    }
}

