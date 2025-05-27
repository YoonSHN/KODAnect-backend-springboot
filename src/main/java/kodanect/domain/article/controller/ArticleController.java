package kodanect.domain.article.controller;

import kodanect.common.config.GlobalsProperties;
import kodanect.common.response.ApiResponse;
import kodanect.domain.article.dto.ArticleDTO;
import kodanect.domain.article.dto.ArticleDetailDto;
import kodanect.domain.article.dto.BoardOption;
import kodanect.domain.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static kodanect.common.exception.config.MessageKeys.ARTICLE_DETAIL_SUCCESS;
import static kodanect.common.exception.config.MessageKeys.ARTICLE_LIST_SUCCESS;
import static kodanect.common.exception.config.MessageKeys.FILE_NOT_FOUND;
import static kodanect.common.exception.config.MessageKeys.FILE_DOWNLOAD_ERROR;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/newKoda/")
public class ArticleController {

    public static final int DEFAULT_NOTICLE_PAGE_SIZE = 20;

    private final GlobalsProperties globalsProperties;

    private final ArticleService service;

    private final MessageSourceAccessor messageSourceAccessor;

    /**
     * 게시글 목록 조회
     *
     * @param optionStr 게시판 옵션 (1=공지사항, 2=채용공고, all=전체)
     * @param search    검색어 (제목/내용)
     * @param pageable  페이징 정보
     * @return 게시글 목록 응답
     */
    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticles(
            @RequestParam(defaultValue = "all") BoardOption optionStr,
            @RequestParam(required = false) String search,
            @PageableDefault(size = DEFAULT_NOTICLE_PAGE_SIZE, sort = "writeTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        List<String> boardCodes = optionStr.resolveBoardCodes();
        Page<ArticleDTO> result = service.getArticles(boardCodes, search, pageable);
        String message = messageSourceAccessor.getMessage(ARTICLE_LIST_SUCCESS);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, result));
    }

    /**
     * 게시글 상세 조회
     *
     * @param articleSeq 게시글 ID
     * @param optionStr  게시판 옵션 (1=공지사항, 2=채용공고)
     * @return 게시글 상세 응답
     */
    @GetMapping("/notices/{articleSeq}")
    public ResponseEntity<ApiResponse<ArticleDetailDto>> getArticle(
            @PathVariable Integer articleSeq,
            @RequestParam BoardOption optionStr
    ) {
        String boardCode = optionStr.getBoardCode();
        ArticleDetailDto article = service.getArticle(boardCode, articleSeq);
        String message = messageSourceAccessor.getMessage(ARTICLE_DETAIL_SUCCESS);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, article));
    }

    /**
     * 첨부파일 다운로드
     * <p>
     * 저장 경로: {uploadBasePath}/{boardCode}/{articleSeq}/{fileName}
     *
     * @param articleSeq 게시글 ID
     * @param fileName   저장된 파일명
     * @param optionStr  게시판 옵션 (1=공지사항, 2=채용공고)
     * @return 파일 리소스 또는 오류 응답
     */
    @GetMapping("/notices/{articleSeq}/files/{fileName}")
    public ResponseEntity<?> downloadFile(
            @PathVariable Integer articleSeq,
            @PathVariable String fileName,
            @RequestParam BoardOption optionStr
    ) {
        String boardCode = optionStr.getBoardCode();

        try {
            Path basePath = Paths.get(globalsProperties.getFileStorePath(), boardCode, articleSeq.toString()).toAbsolutePath().normalize();
            Path filePath = basePath.resolve(fileName).normalize();

            // 경로 탈출 방지
            if (!filePath.startsWith(basePath)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "잘못된 접근입니다.");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                String message = messageSourceAccessor.getMessage(FILE_NOT_FOUND);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(HttpStatus.NOT_FOUND, message));
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);

        }
        catch (Exception e) {
            log.error("파일 다운로드 오류 발생: {}", e.getMessage());
            String message = messageSourceAccessor.getMessage(FILE_DOWNLOAD_ERROR);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR, message));
        }
    }
}
