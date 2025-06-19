package kodanect.domain.heaven.service.impl;

import kodanect.domain.heaven.exception.FileDeleteFailException;
import kodanect.domain.heaven.exception.FileNotFoundException;
import kodanect.domain.heaven.exception.FileSaveFailException;
import kodanect.domain.heaven.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final String FILE_STORE_PATH = "/app/uploads";
    private static final String FILE_BASE_URL = "/image/uploads";


    /* 파일 생성 */
    public Map<String, String> saveFile(MultipartFile file) {
        String fileName = "";
        String orgFileName = "";

        if (file != null && !file.isEmpty()) {
            orgFileName = file.getOriginalFilename();
            String extension = Objects.requireNonNull(orgFileName).substring(orgFileName.lastIndexOf("."));
            fileName = UUID.randomUUID().toString().replace("-", "").toUpperCase() + extension;
            Path filePath = Paths.get(FILE_STORE_PATH).resolve(fileName).normalize();

            try {
                Files.createDirectories(filePath.getParent());
                file.transferTo(filePath.toFile());
            } catch (IOException e) {
                throw new FileSaveFailException(fileName);
            }
        }

        return Map.of("fileName", fileName, "orgFileName", orgFileName);
    }

    /* 파일 삭제 */
    public void deleteFile(String fileName) {
        Path filePath = Paths.get(FILE_STORE_PATH).resolve(fileName).normalize();

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException(fileName);
        }

        try {
            Files.delete(filePath);
        } catch (IOException e) {
            throw new FileDeleteFailException(fileName);
        }
    }

    /* 파일 조회 */
    public String getFile(String fileName) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(FILE_BASE_URL)
                .path("/")
                .path(fileName)
                .toUriString();
    }

    /* 파일 수정 */
    public Map<String, String> updateFile(MultipartFile newFile, String oldFileName) {
        // 수정하려는 파일이 이전 파일과 같다면 그대로 사용하는 로직 구현!!!!!!!!!!

        // 1. 기존 파일 삭제
        if (oldFileName != null && !oldFileName.isBlank()) {
            deleteFile(oldFileName);
        }

        // 2. 새 파일 저장 및 URL 반환
        return saveFile(newFile);
    }
}
