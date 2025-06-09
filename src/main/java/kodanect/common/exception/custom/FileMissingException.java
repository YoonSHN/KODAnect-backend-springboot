package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

import java.nio.file.Path;

import static kodanect.common.exception.config.MessageKeys.FILE_NOT_FOUND;

/**
 * 파일이 존재하지 않거나 읽을 수 없을 때 발생하는 예외.
 * 파일 다운로드 시 실제 파일이 없거나 접근 불가한 경우에 사용됨.
 */
public class FileMissingException extends AbstractCustomException {

    private final String reason;
    private final transient Path filePath;
    private final String boardCode;
    private final Integer articleSeq;
    private final String fileName;

    public FileMissingException(String reason, Path filePath,
                                String boardCode, Integer articleSeq, String fileName) {
        super(FILE_NOT_FOUND);
        this.reason = reason;
        this.filePath = filePath;
        this.boardCode = boardCode;
        this.articleSeq = articleSeq;
        this.fileName = fileName;
    }

    public FileMissingException(String reason, Path filePath,
                                String boardCode, Integer articleSeq, String fileName,
                                Throwable cause) {
        super(FILE_NOT_FOUND, cause);
        this.reason = reason;
        this.filePath = filePath;
        this.boardCode = boardCode;
        this.articleSeq = articleSeq;
        this.fileName = fileName;
    }

    @Override
    public String getMessage() {
        return String.format("""
            [파일 오류] %s
            - 경로: %s
            - 요청 정보: boardCode=%s, articleSeq=%d, fileName=%s
            """, reason, filePath, boardCode, articleSeq, fileName);
    }

    @Override
    public String getMessageKey() {
        return FILE_NOT_FOUND;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{
            reason,
            filePath.toString(),
            boardCode,
            articleSeq,
            fileName
        };
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
