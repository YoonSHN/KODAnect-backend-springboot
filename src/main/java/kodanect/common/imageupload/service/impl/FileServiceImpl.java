package kodanect.common.imageupload.service.impl;

import kodanect.common.config.GlobalsProperties;
import kodanect.common.imageupload.service.FileService;
import kodanect.common.validation.FileValidator;
import kodanect.domain.heaven.exception.FileDeleteFailException;
import kodanect.domain.heaven.exception.FileSaveFailException;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RANDOM_MIN = 100;
    private static final int RANDOM_BOUND = 900;

    private final GlobalsProperties globalsProperties;


    /* 파일 서버에 업로드 */
    public String uploadFile(MultipartFile file) {
        String fileStorePath = globalsProperties.getFileStorePath();
        String fileBaseUrl = globalsProperties.getFileBaseUrl();

        /* 파일 존재 여부 */
        FileValidator.validateEmptyFile(file);
        /* 파일 타입 체크 */
        FileValidator.validateImageFileType(file);
        /* 파일 크기 제한 */
        FileValidator.validateFileSize(file, globalsProperties.getPosblAtchFileSize());

        /* 파일 설정 */
        String orgFileName = file.getOriginalFilename();
        String extension = Objects.requireNonNull(orgFileName).substring(orgFileName.lastIndexOf("."));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = SECURE_RANDOM.nextInt(RANDOM_BOUND) + RANDOM_MIN; //100 ~ 999까지의 수자
        String storedFileName = timestamp + "_" + randomNum + extension;

        /* 저장 경로 설정 */
        String storePath = Paths.get(fileStorePath).toAbsolutePath().normalize().toString();
        Path filePath = Paths.get(storePath).resolve(storedFileName);

        /* 파일 서버에 저장 */
        try {
            Files.createDirectories(filePath.getParent());
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new FileSaveFailException(orgFileName);
        }

        /* 파일 URL 반환 */
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(fileBaseUrl)
                .path("/")
                .path(storedFileName)
                .toUriString();
    }

    /* 내용에서 이미지 파싱하여 DB에 저장 */
    @Override
    public Map<String, String> saveFile(String contents) {
        List<String> fileNames = new ArrayList<>();
        List<String> orgFileNames = new ArrayList<>();

        /* 내용 */
        if (contents == null || contents.isBlank()) {
            return Map.of("fileName", "", "orgFileName", "");
        }

        /* HTML 확인 후 <img> 태그 추출 */
        Document document = Jsoup.parse(contents);
        Elements imgTags = document.select("img");

        /* <img> 태그 src 저장 */
        for (Element imgTag : imgTags) {
            String src = imgTag.attr("src");
            String orgFileName = src.substring(src.lastIndexOf("/") + 1);

            orgFileNames.add(orgFileName);
            fileNames.add(UUID.randomUUID().toString().replace("-", "").toUpperCase());
        }

        return Map.of(
                "fileName", String.join(",", fileNames),
                "orgFileName", String.join(",", orgFileNames)
        );
    }

    /* 파일 삭제 */
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        String fileStorePath = globalsProperties.getFileStorePath();

        /* 쉼표(,)로 파일 분리 */
        String[] fileNames = fileName.split(",");

        for (String storedFileName : fileNames) {
            Path filePath = Paths.get(fileStorePath).resolve(storedFileName).normalize();

            /* 파일 존재 여부 */
            FileValidator.validateEmptyFile(filePath);

            /* 파일 삭제 */
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                throw new FileDeleteFailException(fileName);
            }
        }
    }

    /* 파일 수정 */
    public Map<String, String> updateFile(String contents, String oldFileName) {
        /* 기존 파일 삭제 */
        if (oldFileName != null && !oldFileName.isBlank()) {
            deleteFile(oldFileName);
        }

        /* 새 파일 저장 및 URL 반환 */
        return saveFile(contents);
    }
}
