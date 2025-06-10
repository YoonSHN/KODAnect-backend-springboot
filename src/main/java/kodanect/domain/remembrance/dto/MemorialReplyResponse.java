package kodanect.domain.remembrance.dto;

import kodanect.common.util.CursorIdentifiable;
import lombok.*;

import java.time.LocalDateTime;

/**
 *
 * 기증자 추모관 게시글 댓글 응답 dto
 *
 * <p>replySeq : 댓글 번호</p>
 * <p>replyWriter : 댓글 작성자 닉네임</p>
 * <p>replyContents : 댓글 내용</p>
 * <p>replyWriteTime : 댓글 등록일시</p>
 *
 * */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @ToString
public class MemorialReplyResponse implements CursorIdentifiable<Integer> {

    /* 댓글 일련번호 */
    private Integer replySeq;

    /* 댓글 작성 닉네임 */
    private String replyWriter;

    /* 댓글 내용 */
    private String replyContents;

    /* 댓글 등록일시 */
    private LocalDateTime replyWriteTime;

    @Override
    public Integer getCursorId() {
        return replySeq;
    }

    /** 2020-12-13T02:11:12 -> 2020-12-13 형식 변경 */
    public String getReplyWriteTime() {
        return replyWriteTime.toLocalDate().toString();
    }
}

