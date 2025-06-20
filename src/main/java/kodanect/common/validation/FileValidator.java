package kodanect.common.validation;

import kodanect.domain.heaven.exception.FileNotFoundException;
import kodanect.domain.heaven.exception.FileSizeExceededException;
import kodanect.domain.heaven.exception.UnsupportedImageTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileValidator {

    private FileValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /* 파일 존재 여부 (파일로 확인) */
    public static void validateEmptyFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileNotFoundException(file);
        }
    }

    /* 파일 존재 여부 (파일 경로로 확인) */
    public static void validateEmptyFile(Path filePath) {
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException(filePath.toFile());
        }
    }

    /* 파일 타입 체크 */
    public static void validateImageFileType(MultipartFile file) {
        String contentType = file.getContentType();

        if (!List.of("image/jpeg","image/png","image/gif").contains(contentType)) {
            throw new UnsupportedImageTypeException(contentType);
        }
    }

    /* 파일 크기 체크 */
    public static void validateFileSize(MultipartFile file, long maxSize) {
        long fileSize = file.getSize();

        if (fileSize > maxSize) {
            throw new FileSizeExceededException(fileSize);
        }
    }
}
