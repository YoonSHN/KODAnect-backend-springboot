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
     * 공통: 게시글 목록 조회 처리 메서드
     *
     * @param boardCodes 대상 게시판 코드 리스트
     * @param searchField 검색 대상 필드 (제목/내용 등)
     * @param search 검색어
     * @param pageable 페이징 및 정렬 정보
     * @return ApiResponse
     */
    private ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesCommon(List<String> boardCodes, String searchField, String search, Pageable pageable) {
        Page<ArticleDTO> result = service.getArticles(boardCodes, searchField, search, pageable);
        String message = messageSourceAccessor.getMessage(ARTICLE_LIST_SUCCESS);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, result));
    }

    /**
     * 공통: 게시글 상세 조회 처리 메서드
     *
     * @param boardCode 게시판 코드
     * @param articleSeq 게시글 ID
     * @return ApiResponse
     * @throws ArticleNotFoundException 게시글이 없을 경우 예외 발생
     */
    private ResponseEntity<ApiResponse<ArticleDetailDto>> getArticleCommon(String boardCode, Integer articleSeq) {
        ArticleDetailDto article = service.getArticle(boardCode, articleSeq);
        if (article == null) {
            log.warn("게시글 없음: boardCode={}, articleSeq={}", boardCode, articleSeq);
            throw new ArticleNotFoundException(articleSeq);
        }
        String message = messageSourceAccessor.getMessage(ARTICLE_DETAIL_SUCCESS);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, article));
    }

    /**
     * 공통: 첨부파일 다운로드 처리 메서드
     *
     * @param boardCode 게시판 코드
     * @param articleSeq 게시글 ID
     * @param fileName 다운로드할 파일 이름
     * @return ResponseEntity
     * @throws FileAccessViolationException 파일 경로 조작 시 예외 발생
     * @throws FileMissingException 파일이 없거나 읽을 수 없는 경우 예외 발생
     * @throws Exception 기타 IO 예외
     */
    private ResponseEntity<Object> downloadFileCommon(String boardCode, Integer articleSeq, String fileName) throws Exception {
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

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    /**
     * 공지사항, 채용공고 등 게시글 목록 조회 API
     *
     * @param optionStr 게시판 옵션 (1=공지사항, 2=채용공고, all=전체)
     * @param searchField 검색 대상 필드 (제목/내용)
     * @param search 검색어
     * @param pageable 페이징 및 정렬 정보
     * @return ApiResponse
     */
    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticles(
            @RequestParam(defaultValue = "all") BoardOption optionStr,
            @RequestParam(required = false, defaultValue = "all") String searchField,
            @RequestParam(required = false) String search,
            @PageableDefault(size = DEFAULT_ARTICLE_PAGE_SIZE, sort = "writeTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        List<String> boardCodes = optionStr.resolveBoardCodes();
        return getArticlesCommon(boardCodes, searchField, search, pageable);
    }

    /**
     * 기타 게시판(공지/채용 외) 게시글 목록 조회 API
     *
     * @param boardCode URI 경로 변수 게시판 코드명
     * @param searchField 검색 대상 필드 (제목/내용)
     * @param search 검색어
     * @param pageable 페이징 및 정렬 정보
     * @return ApiResponse
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

        return getArticlesCommon(boardCodes, searchField, search, pageable);
    }

    /**
     * 기타 게시판(공지/채용 외) 게시글 상세 조회 API
     *
     * @param boardCode 게시판 코드
     * @param articleSeq 게시글 ID
     * @return ApiResponse
     * @throws InvalidBoardCodeException 게시판 코드가 유효하지 않은 경우 예외 발생
     * @throws ArticleNotFoundException 게시글이 없을 경우 예외 발생
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
        return getArticleCommon(dbBoardCode, articleSeq);
    }

    /**
     * 일반 게시판 게시글 첨부파일 다운로드 API
     *
     * @param boardCode 게시판 코드
     * @param articleSeq 게시글 ID
     * @param fileName 다운로드할 파일명
     * @return ResponseEntity
     * @throws InvalidBoardCodeException 게시판 코드 유효성 검증 실패 시 예외
     * @throws FileAccessViolationException 경로 조작 시 예외
     * @throws FileMissingException 파일 없거나 읽기 불가 시 예외
     * @throws Exception 기타 IO 예외
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
        return downloadFileCommon(dbBoardCode, articleSeq, fileName);
    }

    /**
     * 공지사항, 채용공고 등 게시글 상세 조회 API
     *
     * @param articleSeq 게시글 ID
     * @param optionStr 게시판 옵션
     * @return ApiResponse
     */
    @GetMapping("/notices/{articleSeq}")
    public ResponseEntity<ApiResponse<ArticleDetailDto>> getArticle(
            @PathVariable Integer articleSeq,
            @RequestParam BoardOption optionStr
    ) {
        String boardCode = optionStr.getBoardCode();
        return getArticleCommon(boardCode, articleSeq);
    }

    /**
     * 공지사항, 채용공고 등 게시글 첨부파일 다운로드 API
     *
     * @param articleSeq 게시글 ID
     * @param fileName 다운로드할 파일명
     * @param optionStr 게시판 옵션
     * @return ResponseEntity
     * @throws Exception IO 예외
     */
    @GetMapping("/notices/{articleSeq}/files/{fileName}")
    public ResponseEntity<Object> downloadFile(
            @PathVariable Integer articleSeq,
            @PathVariable String fileName,
            @RequestParam BoardOption optionStr
    ) throws Exception {
        String boardCode = optionStr.getBoardCode();
        return downloadFileCommon(boardCode, articleSeq, fileName);
    }
}
