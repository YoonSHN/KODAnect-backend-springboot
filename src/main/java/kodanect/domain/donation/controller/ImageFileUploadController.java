package kodanect.domain.donation.controller;

import kodanect.domain.donation.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/app/upload")
@RequiredArgsConstructor
public class ImageFileUploadController {

    private static final String PROD_DOMAIN = "https://www.koda1458.kr";
    private static final String DEV_DOMAIN  = "http://localhost:8080";

    private final MessageSourceAccessor msg;
    private final Environment env;

    @PostMapping("/upload_img/{category}")
    public ResponseEntity<Map<String,String>> uploadImage(
            @PathVariable("category") String category,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        // 1) 빈 파일 체크
        if (file.isEmpty()) {
            throw new BadRequestException(msg.getMessage("upload.error.empty"));
        }

        // 2) MIME 타입 체크
        String contentType = file.getContentType();
        if (!List.of("image/jpeg","image/png","image/gif").contains(contentType)) {
            throw new BadRequestException(msg.getMessage("upload.error.invalidType"));
        }

        // 3) 크기 제한 체크 (dev/prod 공통 프로퍼티 키 모두 시도)
        long maxSize = Long.parseLong(
                Optional.ofNullable(env.getProperty("globals.posbl-atch-file-size"))   // dev
                        .orElse(env.getProperty("Globals.posblAtchFileSize", "5242880")) // prod
        );
        if (file.getSize() > maxSize) {
            throw new BadRequestException(msg.getMessage("upload.error.sizeExceeded"));
        }

        // 4) 파일명 생성
        String rawName = Optional.ofNullable(file.getOriginalFilename()).orElse("unknown.jpg");
        String safeName = Paths.get(rawName).getFileName().toString();
        String ext = safeName.contains(".") ? safeName.substring(safeName.lastIndexOf(".")) : ".jpg";
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String storedFileName = ts + ext;

        // 5) 저장 경로 결정 (dev/prod 키 둘 다 시도)
        String storePath = Optional.ofNullable(env.getProperty("Globals.fileStorePath"))   // prod
                .orElse(env.getProperty("globals.file-store-path", "./uploads"));          // dev
        String absStore = Paths.get(storePath).toAbsolutePath().normalize().toString();
        Path target = Paths.get(absStore, "upload_img",category,  storedFileName);
        Files.createDirectories(target.getParent());
        file.transferTo(target.toFile());

        // 6) baseUrl 결정 (dev/prod 키 둘 다 시도)
        String baseUrl = Optional.ofNullable(env.getProperty("globals.file-base-url"))   // dev
                .orElse(env.getProperty("globals.fileBaseUrl", "/upload_img"));          // prod

        // 7) 현재 프로파일로 도메인 분기
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");
        String domain = isProd ? PROD_DOMAIN : DEV_DOMAIN;

        // 8) 최종 URL 조합
        String fileUrl = domain + baseUrl + "/" + storedFileName;

        // 9) 응답
        return ResponseEntity.ok(Map.of("url", fileUrl));
    }
}