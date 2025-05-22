package kodanect.domain.remembrance.entity;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 게시글 테스트 데이터를 표현하기 위한 클래스입니다.
 *
 * 실제 DB와 연동되는 JPA 엔티티가 아니며,
 * 컨트롤러 내부의 더미 데이터 생성을 위해 사용됩니다.
 *
 * 게시글의 제목, 내용, 작성자, 생성/수정 시각 필드를 포함합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class BoardTest {

    private Long id;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}