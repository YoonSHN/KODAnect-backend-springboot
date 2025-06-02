package kodanect.domain.article.service;

import kodanect.domain.article.dto.DownloadFile;

public interface FileDownloadService {

    DownloadFile loadDownloadFile(String boardCode, Integer articleSeq, String fileName);
}
