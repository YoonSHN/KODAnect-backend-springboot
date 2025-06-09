package kodanect.common.exception.custom;

import org.springframework.http.HttpStatus;

import java.nio.file.Path;

import static kodanect.common.exception.config.MessageKeys.FILE_ACCESS_VIOLATION;

/**
 * 파일 시스템 접근 보안 위반 시 발생하는 예외.
 * 예를 들어, 디렉터리 탈출(path traversal) 등의 시도를 차단하는 데 사용됨.
 */
public class FileAccessViolationException extends AbstractCustomException {

    private final transient Path basePath;
    private final transient Path requestedPath;
    private final String boardCode;
    private final Integer articleSeq;
    private final String fileName;

    public FileAccessViolationException(Path basePath, Path requestedPath,
                                        String boardCode, Integer articleSeq, String fileName) {
        super(FILE_ACCESS_VIOLATION);
        this.basePath = basePath;
        this.requestedPath = requestedPath;
        this.boardCode = boardCode;
        this.articleSeq = articleSeq;
        this.fileName = fileName;
    }

    public FileAccessViolationException(Path basePath, Path requestedPath,
                                        String boardCode, Integer articleSeq, String fileName,
                                        Throwable cause) {
        super(FILE_ACCESS_VIOLATION, cause);
        this.basePath = basePath;
        this.requestedPath = requestedPath;
        this.boardCode = boardCode;
        this.articleSeq = articleSeq;
        this.fileName = fileName;
    }

    @Override
    public String getMessage() {
        return String.format("""
            [보안 오류] 허용되지 않은 경로 접근 시도
            - 허용된 경로: %s
            - 요청된 경로: %s
            - 요청 정보: boardCode=%s, articleSeq=%d, fileName=%s
            """, basePath, requestedPath, boardCode, articleSeq, fileName);
    }

    @Override
    public String getMessageKey() {
        return FILE_ACCESS_VIOLATION;
    }

    @Override
    public Object[] getArguments() {
        return new Object[]{
            basePath.toString(),
            requestedPath.toString(),
            boardCode,
            articleSeq,
            fileName
        };
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
