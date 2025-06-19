package kodanect.domain.heaven.entity;

import kodanect.domain.heaven.dto.request.HeavenUpdateRequest;
import kodanect.domain.heaven.exception.PasswordMismatchException;
import kodanect.domain.remembrance.entity.Memorial;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity(name = "Heaven")
@Table(name = "tb25_410_heaven_letter")
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
@Builder
public class Heaven {

    /* 기본값 */
    private static final String DEFAULT_DEL_FLAG = "N";

    /* 편지 일련번호*/
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int letterSeq;

    /* 기증자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donate_seq")
    private Memorial memorial;

    /* 편지 제목 */
    @Column(length = 600)
    private String letterTitle;

    /* 기증자 명 */
    @Column(length = 150)
    private String donorName;

    /* 편지 비밀번호 */
    @Column(length = 60)
    private String letterPasscode;

    /* 편지 작성자 */
    @Column(length = 150)
    private String letterWriter;

    /* 편지 익명 여부 */
    @Column(length = 1)
    private String anonymityFlag;

    /* 조회 건수 */
    private Integer readCount;

    /* 편지 내용 */
    @Column(columnDefinition = "LONGTEXT")
    private String letterContents;

    /* deHTML로 변환된 편지 내용 */
    @Column(name = "deHTML_letter_contents", length = 3000)
    private String deHTMLLetterContents;

    /* 이미지 경로 */
    @Column(length = 600)
    private String imagePath;

    /* 이미지 파일 명 */
    @Column(length = 600)
    private String fileName;

    /* 이미지 원본 파일 명 */
    @Column(length = 600)
    private String orgFileName;

    /* 생성 일시 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime writeTime;

    /* 생성자 아이디 */
    @Column(length = 60)
    private String writerId;

    /* 수정 일시 */
    @Column(insertable = false, updatable = false)
    private LocalDateTime modifyTime;

    /* 수정자 아이디 */
    @Column(length = 60)
    private String modifierId;

    /* 삭제 여부 */
    @Column(nullable = false, length = 1)
    @Builder.Default
    private String delFlag = DEFAULT_DEL_FLAG;

    @OneToMany(mappedBy = "heaven")
    private List<HeavenComment> comments = new ArrayList<>();

    /* 영속성 컨텍스트에 처음 저장되기 직전에 실행되는 메서드 */
    @PrePersist
    private void prePersist() {
        if (writeTime == null) {
            writeTime = LocalDateTime.now();
        }

        if (modifyTime == null) {
            modifyTime = LocalDateTime.now();
        }
    }

    /* 조회수 증가 */
    public void addReadCount() {
        this.readCount++;
    }

    /* 비밀번호 일치 검증 */
    public void verifyPasscode(String passcode) {
        if (!Objects.equals(this.letterPasscode, passcode)) {
            throw new PasswordMismatchException(passcode);
        }
    }

    /* 편지 수정 */
    public void updateHeaven(HeavenUpdateRequest heavenUpdateRequest, Memorial memorial, Map<String, String> fileMap) {
        letterWriter = heavenUpdateRequest.getLetterWriter();
        anonymityFlag = heavenUpdateRequest.getAnonymityFlag();
        donorName = heavenUpdateRequest.getDonorName();
        this.memorial = memorial;
        letterTitle = heavenUpdateRequest.getLetterTitle();
        letterContents = heavenUpdateRequest.getLetterContents();
        fileName = fileMap.get("fileName");
        orgFileName = fileMap.get("orgFileName");
    }

    /* 게시물 및 해당 댓글 소프트 삭제 */
    public void softDelete() {
        this.delFlag = "Y";
        this.comments.forEach(HeavenComment::softDelete);
    }
}
