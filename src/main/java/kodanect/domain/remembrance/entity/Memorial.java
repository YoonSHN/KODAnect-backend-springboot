package kodanect.domain.remembrance.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

/** 기증자 추모관 게시글 엔티티 클래스 */
@Entity(name = "Memorial")
@Table(name = "tb25_400_memorial")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class Memorial {

    /* 디폴트 값 */
    /* 초기화 기본값 (0) */
    private static final int DEFAULT_COUNT = 0;
    /* 삭제 여부 기본값 (N) */
    private static final String DEFAULT_DEL_FLAG = "N";

    /* 기증자 일련 번호 */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "donate_seq")
    private int donateSeq;

    /* 기증자 명 */
    @Column(nullable = true, length = 150, name = "donor_name")
    private String donorName;

    /* 익명 여부 Y, N */
    @Column(nullable = true, length = 1, name = "anonymity_flag")
    private String anonymityFlag;

    /* 추모합니다. */
    @Column(nullable = true, length = 600, name = "donate_title")
    private String donateTitle;

    /* 권역 코드 */
    @Column(nullable = true, length = 10, name = "area_code")
    private String areaCode;

    /* 기증자 내용 */
    @Column(nullable = true, columnDefinition = "TEXT", name = "contents")
    private String contents;

    /* 이미지 파일 명 */
    @Column(nullable = true, length = 600, name = "file_name")
    private String fileName;

    /* 이미지 원본 파일 명 */
    @Column(nullable = true, length = 600, name = "org_file_name")
    private String orgFileName;

    /* 운영자 */
    @Column(nullable = true, length = 150, name = "writer")
    private String writer;

    /* 기증자 기증 일시 20120101 */
    @Column(nullable = true, length = 8, name = "donate_date")
    private String donateDate;

    /* 기증자 성별 */
    @Column(nullable = true, length = 1, name = "gender_flag")
    private String genderFlag;

    /* 기증자 나이 */
    @Column(nullable = true, name = "donate_age")
    private Integer donateAge;

    /* 추모수 헌화 */
    @Column(nullable = true, name = "flower_count")
    @Builder.Default private int flowerCount = DEFAULT_COUNT;

    /* 추모수 사랑해요 */
    @Column(nullable = true, name = "love_count")
    @Builder.Default private int loveCount = DEFAULT_COUNT;

    /* 추모수 보고싶어요 */
    @Column(nullable = true, name = "see_count")
    @Builder.Default private int seeCount = DEFAULT_COUNT;

    /* 추모수 그리워요 */
    @Column(nullable = true, name = "miss_count")
    @Builder.Default private int missCount = DEFAULT_COUNT;

    /* 추모수 자랑스러워요 */
    @Column(nullable = true, name = "proud_count")
    @Builder.Default private int proudCount = DEFAULT_COUNT;

    /* 추모수 힘들어요 */
    @Column(nullable = true, name = "hard_count")
    @Builder.Default private int hardCount = DEFAULT_COUNT;

    /* 추모수 슬퍼요 */
    @Column(nullable = true, name = "sad_count")
    @Builder.Default private int sadCount = DEFAULT_COUNT;

    /* 생성 일시 */
    @Column(nullable = false, insertable = false, updatable = false, name = "write_time")
    private LocalDateTime writeTime;

    /* 생성자 아이디 */
    @Column(nullable = false, length = 60, name = "writer_id")
    private String writerId;

    /* 수정 일시 */
    @Column(nullable = true, insertable = false, updatable = false, name = "modify_time")
    private LocalDateTime modifyTime;

    /* 수정자 아이디 */
    @Column(nullable = false, length = 60, name = "modifier_id")
    private String modifierId;

    /* 삭제 여부 */
    @Column(nullable = false, length = 1, name = "del_flag")
    @Builder.Default private String delFlag = DEFAULT_DEL_FLAG;

}

