package kodanect.domain.article.controller;

import kodanect.common.config.GlobalsProperties;
import kodanect.common.exception.custom.ArticleNotFoundException;
import kodanect.common.exception.custom.FileAccessViolationException;
import kodanect.common.exception.custom.FileMissingException;
import kodanect.common.exception.custom.InvalidBoardCodeException;
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


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static kodanect.common.exception.config.MessageKeys.ARTICLE_DETAIL_SUCCESS;
import static kodanect.common.exception.config.MessageKeys.ARTICLE_LIST_SUCCESS;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/newKoda")
public class ArticleController {

    public static final int DEFAULT_ARTICLE_PAGE_SIZE = 20;

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
            @RequestParam(required = false, defaultValue = "all") String searchField,
            @RequestParam(required = false) String search,
            @PageableDefault(size = DEFAULT_ARTICLE_PAGE_SIZE, sort = "writeTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        List<String> boardCodes = optionStr.resolveBoardCodes();
        Page<ArticleDTO> result = service.getArticles(boardCodes, searchField, search, pageable);
        String message = messageSourceAccessor.getMessage(ARTICLE_LIST_SUCCESS);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, result));
    }

    /**
     * 게시판별 게시글 목록 조회 (공지/채용 외)
     *
     * @param boardCode URI 경로로 전달된 게시판 코드명
     * @param search 게시글 검색어 (제목/내용 기준)
     * @param pageable 페이징 정보 (기본: 페이지당 최신순)
     * @return 게시글 목록을 포함한 표준 API 응답
     * @throws IllegalArgumentException 존재하지 않는 코드 일경우
     */
    @GetMapping("/{boardCode}")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getOtherBoardArticles(
            @PathVariable String boardCode,
            @RequestParam(required = false, defaultValue = "all") String searchField,
            @RequestParam(required = false) String search,
            @PageableDefault(size = DEFAULT_ARTICLE_PAGE_SIZE, sort = "writeTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        List<String> boardCodes;

        if (BoardOption.isValid(boardCode)) {
            boardCodes = BoardOption.fromParam(boardCode).resolveBoardCodes();
        } else {
            boardCodes = List.of(boardCode);
        }

        Page<ArticleDTO> result = service.getArticles(boardCodes, searchField, search, pageable);
        String message = messageSourceAccessor.getMessage(ARTICLE_LIST_SUCCESS);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, result));
    }

    /**
     * 일반 게시판의 특정 게시글 상세 조회
     *
     * @param boardCode 게시판 코드 (문자열)
     * @param articleSeq 게시글 ID
     * @return 게시글 상세 데이터
     */
    @GetMapping("/{boardCode}/{articleSeq}")
    public ResponseEntity<ApiResponse<ArticleDetailDto>> getOtherBoardArticle(
            @PathVariable String boardCode,
            @PathVariable Integer articleSeq
    ) {
        if (!BoardOption.isValid(boardCode)) {
            log.warn("잘못된 게시판 코드 요청: {}", boardCode);
            throw new InvalidBoardCodeException(boardCode);
        }
        String dbBoardCode = BoardOption.fromParam(boardCode).getBoardCode();
        ArticleDetailDto article = service.getArticle(dbBoardCode, articleSeq);
        if (article == null) {
            log.warn("게시글 없음: boardCode={}, articleSeq={}", dbBoardCode, articleSeq);
            throw new ArticleNotFoundException(articleSeq);
        }

        String message = messageSourceAccessor.getMessage(ARTICLE_DETAIL_SUCCESS);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, article));
    }

    /**
     * 일반 게시판 게시글 첨부파일 다운로드
     *
     * @param boardCode 게시판 코드
     * @param articleSeq 게시글 ID
     * @param fileName 다운로드할 파일 이름
     * @return 파일 리소스 or 오류 응답
     */
    @GetMapping("/{boardCode}/{articleSeq}/files/{fileName}")
    public ResponseEntity<Object> downloadOtherBoardFile(
            @PathVariable String boardCode,
            @PathVariable Integer articleSeq,
            @PathVariable String fileName
    ) throws Exception {

        if (!BoardOption.isValid(boardCode)) {
            log.warn("잘못된 게시판 코드 요청: {}", boardCode);
            throw new InvalidBoardCodeException(boardCode);
        }

        String dbBoardCode = BoardOption.fromParam(boardCode).getBoardCode();

        Path basePath = Paths.get(globalsProperties.getFileStorePath(), dbBoardCode, articleSeq.toString()).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(fileName).normalize();

        if (!filePath.startsWith(basePath)) {
            throw new FileAccessViolationException(filePath.toString());
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            log.warn("파일이 존재하지 않거나 읽을 수 없습니다.");
            throw new FileMissingException(fileName);
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
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
    public ResponseEntity<Object> downloadFile(
            @PathVariable Integer articleSeq,
            @PathVariable String fileName,
            @RequestParam BoardOption optionStr
    ) throws Exception {

        String boardCode = optionStr.getBoardCode();

        Path basePath = Paths.get(globalsProperties.getFileStorePath(), boardCode, articleSeq.toString()).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(fileName).normalize();

        if (!filePath.startsWith(basePath)) {
            log.warn("경로 접근 위반: {}", filePath);
            throw new FileAccessViolationException(filePath.toString());
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            log.warn("파일이 존재하지 않거나 읽을 수 없습니다.");
            throw new FileMissingException(fileName);
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

}
