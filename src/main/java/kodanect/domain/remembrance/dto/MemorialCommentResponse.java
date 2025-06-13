package kodanect.domain.remembrance.dto;

import kodanect.common.util.CursorIdentifiable;
import lombok.*;

import java.time.LocalDateTime;

/**
 *
 * 기증자 추모관 게시글 댓글 응답 dto
 *
 * <p>commentSeq : 댓글 번호</p>
 * <p>commentWriter : 댓글 작성자 닉네임</p>
 * <p>contents : 댓글 내용</p>
 * <p>writeTime : 댓글 등록일시</p>
 *
 * */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialCommentResponse implements CursorIdentifiable<Integer> {

    /* 댓글 일련번호 */
    private Integer commentSeq;

    /* 댓글 작성 닉네임 */
    private String commentWriter;

    /* 댓글 내용 */
    private String contents;

    /* 댓글 등록일시 */
    private LocalDateTime writeTime;

    @Override
    public Integer getCursorId() {
        return commentSeq;
    }

    /** 2020-12-13T02:11:12 -> 2020-12-13 형식 변경 */
    public String getWriteTime() {
        return writeTime.toLocalDate().toString();
    }
}

