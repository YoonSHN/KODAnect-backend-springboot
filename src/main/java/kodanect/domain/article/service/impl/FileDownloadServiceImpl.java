package kodanect.domain.article.service.impl;

import kodanect.common.config.GlobalsProperties;
import kodanect.common.exception.config.SecureLogger;
import kodanect.common.exception.custom.FileAccessViolationException;
import kodanect.common.exception.custom.FileMissingException;
import kodanect.common.exception.custom.InvalidFileNameException;
import kodanect.domain.article.dto.DownloadFile;
import kodanect.domain.article.service.FileDownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * {@inheritDoc}
 */
@RequiredArgsConstructor
@Service
public class FileDownloadServiceImpl implements FileDownloadService {

    private static final SecureLogger log = SecureLogger.getLogger(FileDownloadServiceImpl.class);

    private final GlobalsProperties globalsProperties;

    /**
     * 주어진 게시판 코드 및 게시글 ID, 파일명을 기반으로 다운로드할 파일 정보를 반환합니다.
     *
     * <p>이 메서드는 다음과 같은 절차로 파일을 제공합니다:
     * <ul>
     *     <li>기반 경로에서 파일 경로를 조립하고 정규화</li>
     *     <li>{@code filePath.startsWith(basePath)}를 통해 경로 탈출 시도를 차단</li>
     *     <li>실제 {@link UrlResource}를 생성하고 파일 존재 여부 및 접근 가능성 검증</li>
     *     <li>{@link Files#probeContentType(Path)}로 MIME 타입 추론 </li>
     *     <li>파일명을 UTF-8로 인코딩</li>
     * </ul>
     * </p>
     *
     * @param boardCode 게시판 코드
     * @param articleSeq 게시글 ID
     * @param fileName 다운로드할 파일 이름
     * @return {@link DownloadFile} 파일 리소스와 응답을 위한 정보가 포함된 DTO
     * @throws FileAccessViolationException 경로 탈출 등 보안 위반 시 발생
     * @throws FileMissingException 파일이 없거나 접근 불가할 경우 발생
     * @see java.net.URLEncoder
     * @see java.nio.file.Files
     */
    public DownloadFile loadDownloadFile(String boardCode, Integer articleSeq, String fileName) {

        if (!fileName.matches("^[a-zA-Z0-9가-힣_.-]+$")) {
            throw new InvalidFileNameException("잘못된 파일명입니다.");
        }

        Path basePath = Paths.get(globalsProperties.getFileStorePath(), boardCode, articleSeq.toString())
                .toAbsolutePath().normalize();

        Path filePath = basePath.resolve(fileName).normalize();

        if (!filePath.startsWith(basePath) || Files.isSymbolicLink(filePath)) {
            throw new FileAccessViolationException(basePath, filePath, boardCode, articleSeq, fileName);
        }

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new FileMissingException("URL 변환 실패", filePath, boardCode, articleSeq, fileName, e);
        }

        if (!resource.exists() || !resource.isReadable()) {
            throw new FileMissingException("파일 없음 또는 읽기 불가", filePath, boardCode, articleSeq, fileName);
        }

        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            log.warn("[MIME 타입 추론 실패] 기본값으로 처리됨. 경로: {}, 오류: {}", filePath, e.getMessage());
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        return new DownloadFile(resource, contentType, encodedFileName);
    }

}
