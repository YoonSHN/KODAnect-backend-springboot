package kodanect.domain.heaven.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileService {

    /* 파일 서버에 업로드 */
    String uploadFile(MultipartFile file);

    /* 내용에서 이미지 파싱하여 DB에 저장 */
    Map<String, String> saveFile(String contents);

    /* 파일 삭제 */
    void deleteFile(String fileName);

    /* 파일 수정 */
    Map<String, String> updateFile(String contents, String oldFileName);
}
