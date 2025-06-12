package kodanect.domain.article.service;

import kodanect.domain.article.dto.DownloadFile;

/**
 * 게시글 첨부 파일 다운로드 기능을 정의하는 서비스 인터페이스
 */
public interface FileDownloadService {

    /**
     * 지정된 게시판, 게시글, 파일명을 기반으로 다운로드 가능한 파일을 제공합니다.
     *
     * <p>파일 경로를 조립하고 보안 검증을 거친 뒤,
     * {@link org.springframework.core.io.Resource} 형태로 반환합니다.</p>
     *
     * @param boardCode 게시판 코드
     * @param articleSeq 게시글 번호
     * @param fileName 다운로드할 파일 이름
     * @return {@link DownloadFile} 응답 정보 및 실제 파일 자원이 포함된 DTO
     * @throws kodanect.common.exception.custom.FileAccessViolationException 경로 위반 또는 접근 불가 시
     * @throws kodanect.common.exception.custom.FileMissingException 파일이 존재하지 않거나 읽기 불가할 경우
     */
    DownloadFile loadDownloadFile(String boardCode, Integer articleSeq, String fileName);
}