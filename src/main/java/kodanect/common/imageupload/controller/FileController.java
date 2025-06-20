package kodanect.common.imageupload.controller;

import kodanect.common.imageupload.service.FileService;
import kodanect.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file/upload")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final MessageSourceAccessor messageSourceAccessor;

    /* 파일 서버에 업로드 */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> fileUpload(
            @RequestParam MultipartFile file
    ) {
        String fileUrl = fileService.uploadFile(file);

        String message = messageSourceAccessor.getMessage("file.upload.success");

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED,message, fileUrl));
    }
}
