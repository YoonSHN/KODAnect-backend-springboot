package kodanect.domain.heaven.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileService {

    /* 파일 생성 */
    Map<String, String> saveFile(MultipartFile file);

    /* 파일 삭제 */
    void deleteFile(String fileName);

    /* 파일 조회 */
    String getFile(String fileName);

    /* 파일 수정 */
    Map<String, String> updateFile(MultipartFile file, String oldFileName);
}
