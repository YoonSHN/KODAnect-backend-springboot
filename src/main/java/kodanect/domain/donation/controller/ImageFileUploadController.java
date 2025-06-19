package kodanect.domain.donation.controller;

import kodanect.common.config.GlobalsProperties;
import kodanect.domain.donation.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.security.SecureRandom;

@RestController
@RequestMapping("/app/upload")
@RequiredArgsConstructor
public class ImageFileUploadController {

    private static final String PROD_DOMAIN = "https://koda1.elementsoft.biz";
    private static final String DEV_DOMAIN  = "http://localhost:8080";
    private static final Integer EXT_LENGTH = 10;
    private static final Long POSSIBLE_MAX_SIZE = 5242880L;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RANDOM_MIN = 100;
    private static final int RANDOM_BOUND = 900;

    private final MessageSourceAccessor msg;
    private final GlobalsProperties globals;


    @PostMapping("/upload_img/{category}")
    public ResponseEntity<Map<String, String>> uploadImage(
            @PathVariable("category") String category,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        // 1) 빈 파일 체크
        if (file.isEmpty()) {
            throw new BadRequestException(msg.getMessage("upload.error.empty"));
        }

        // 2) MIME 타입 체크
        String contentType = file.getContentType();
        if (!List.of("image/jpeg", "image/png", "image/gif").contains(contentType)) {
            throw new BadRequestException(msg.getMessage("upload.error.invalidType"));
        }

        // 3) 크기 제한 체크
        long maxSize = Optional.ofNullable(globals.getPosblAtchFileSize()).orElse(POSSIBLE_MAX_SIZE);
        if (file.getSize() > maxSize) {
            throw new BadRequestException(msg.getMessage("upload.error.sizeExceeded"));
        }

        // 4) 경로 검증
        if (category.contains("..") || category.contains("/") || category.contains("\\")) {
            throw new BadRequestException(msg.getMessage("error.wrong.path"));
        }

        // 5) 파일명 생성
        String rawName = Optional.ofNullable(file.getOriginalFilename()).orElse("unknown.jpg");
        String safeName = Paths.get(rawName).getFileName().toString();
        String ext = safeName.contains(".")
                ? safeName.substring(safeName.lastIndexOf("."))
                : ".jpg";
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = SECURE_RANDOM.nextInt(RANDOM_BOUND) + RANDOM_MIN; //100 ~ 999까지의 수자

        if (ext.length() > EXT_LENGTH || ext.contains("/") || ext.contains("\\")) {
            throw new BadRequestException("error.wrong.ext");
        }
        String storedFileName = ts + "_" + randomNum + ext;

        // 6) 저장 경로 생성 및 저장
        String storePath = Optional.ofNullable(globals.getFileStorePath()).orElse("./uploads");
        String absStore = Paths.get(storePath).toAbsolutePath().normalize().toString();
        Path target = Paths.get(absStore, "upload_img", category, storedFileName);
        Files.createDirectories(target.getParent());
        file.transferTo(target.toFile());

        // 7) 도메인 결정
        boolean isProd = Optional.ofNullable(globals.getFileStorePath())
                .map(path -> path.startsWith("/app")) // 또는 프로파일 기반 조건
                .orElse(false);
        String domain = isProd ? PROD_DOMAIN : DEV_DOMAIN;


        // 8) 반환 URL 생성
        String fileUrl = domain
                + ensureStartsWithSlash(globals.getFileBaseUrl())
                + "upload_img/"
                + category + "/"
                + storedFileName;

        return ResponseEntity.ok(Map.of("url", fileUrl));
    }

    // 깔끔한 주소를 만들어주는 유틸함수( // 제거)
    private String ensureStartsWithSlash(String path) {
        if (path == null || path.isBlank()){
            return "/image/uploads/";
        }
        return path.endsWith("/") ? path : path + "/";
    }
}