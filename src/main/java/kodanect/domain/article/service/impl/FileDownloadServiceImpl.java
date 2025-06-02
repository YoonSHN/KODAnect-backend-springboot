package kodanect.domain.article.service.impl;

import kodanect.common.config.GlobalsProperties;
import kodanect.common.exception.custom.FileAccessViolationException;
import kodanect.common.exception.custom.FileMissingException;
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

@RequiredArgsConstructor
@Service
public class FileDownloadServiceImpl implements FileDownloadService {

    private final GlobalsProperties globalsProperties;

    /**
     * 주어진 게시판 코드 및 게시글 ID, 파일명을 기반으로 다운로드할 파일 정보를 반환
     *
     * @param boardCode 게시판 코드
     * @param articleSeq 게시글 ID
     * @param fileName 파일 이름
     * @return DownloadFile 다운로드 리소스 및 응답에 필요한 정보
     * @throws FileAccessViolationException 경로 탈출 등 보안 위반 시
     * @throws FileMissingException 파일이 없거나 읽을 수 없을 때
     */
    public DownloadFile loadDownloadFile(String boardCode, Integer articleSeq, String fileName) {
        Path basePath = Paths.get(globalsProperties.getFileStorePath(), boardCode, articleSeq.toString()).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(fileName).normalize();

        if (!filePath.startsWith(basePath)) {
            throw new FileAccessViolationException(filePath.toString());
        }

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new FileMissingException(fileName);
        }
        if (!resource.exists() || !resource.isReadable()) {
            throw new FileMissingException(fileName);
        }

        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        return new DownloadFile(resource, contentType, encodedFileName);
    }
}
