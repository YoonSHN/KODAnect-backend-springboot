package kodanect.domain.heaven.dto.request;

import kodanect.domain.remembrance.dto.common.BlankGroup;
import kodanect.domain.remembrance.dto.common.PatternGroup;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static kodanect.common.exception.config.MessageKeys.BOARD_WRITER_EMPTY;
import static kodanect.common.exception.config.MessageKeys.BOARD_WRITER_INVALID;
import static kodanect.common.exception.config.MessageKeys.BOARD_ANONYMITY_INVALID;
import static kodanect.common.exception.config.MessageKeys.BOARD_TITLE_EMPTY;
import static kodanect.common.exception.config.MessageKeys.BOARD_TITLE_INVALID;
import static kodanect.common.exception.config.MessageKeys.BOARD_CONTENTS_EMPTY;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @ToString
@Builder
public class HeavenUpdateRequest {

    /* 편지 작성자 */
    @NotBlank(message = BOARD_WRITER_EMPTY, groups = BlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z가-힣\\s]{1,10}$", message = BOARD_WRITER_INVALID, groups = PatternGroup.class)
    private String letterWriter;

    /* 편지 익명 여부 */
    @Pattern(regexp = "[YN]", message = BOARD_ANONYMITY_INVALID, groups = PatternGroup.class)
    private String anonymityFlag;

    /* 기증자 명 */
    private String donorName;

    /* 기증자 일련번호 */
    private int donateSeq;

    /* 편지 제목 */
    @NotBlank(message = BOARD_TITLE_EMPTY, groups = BlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z가-힣0-9]{1,50}$", message = BOARD_TITLE_INVALID, groups = PatternGroup.class)
    private String letterTitle;

    /* 편지 내용 */
    @NotBlank(message = BOARD_CONTENTS_EMPTY, groups = BlankGroup.class)
    private String letterContents;
}
