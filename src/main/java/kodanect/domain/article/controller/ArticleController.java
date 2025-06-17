package kodanect.domain.article.controller;

import kodanect.common.exception.config.SecureLogger;
import kodanect.domain.article.exception.ArticleNotFoundException;
import kodanect.common.response.ApiResponse;
import kodanect.domain.article.dto.ArticleDTO;
import kodanect.domain.article.dto.ArticleDetailDto;
import kodanect.domain.article.dto.DownloadFile;
import kodanect.domain.article.dto.SearchCondition;
import kodanect.domain.article.repository.BoardCategoryCache;
import kodanect.domain.article.service.ArticleService;
import kodanect.domain.article.service.FileDownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static kodanect.common.exception.config.MessageKeys.ARTICLE_DETAIL_SUCCESS;
import static kodanect.common.exception.config.MessageKeys.ARTICLE_LIST_SUCCESS;

/**
 * 게시판 관련 API를 제공하는 REST 컨트롤러입니다.
 *
 * <p>공지사항, 채용공고, 일반 게시판 등 다양한 유형의 게시글 조회 및 파일 다운로드 기능을 포함합니다.
 * <ul>
 *     <li>/notices - 공지사항/채용공고 전용</li>
 *     <li>/{boardCode} - 일반 게시판</li>
 * </ul>
 * 내부적으로 {@link BoardCategoryCache}를 통해 게시판 코드 URL 매핑을 처리합니다.
 *
 * @see ArticleService
 * @see FileDownloadService
 * @see BoardCategoryCache
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class ArticleController {

    public static final int DEFAULT_ARTICLE_PAGE_SIZE = 20;

    private static final SecureLogger log = SecureLogger.getLogger(ArticleController.class);

    private final ArticleService service;
    private final MessageSourceAccessor messageSourceAccessor;
    private final BoardCategoryCache boardCategoryCache;
    private final FileDownloadService fileDownloadService;

    /**
     * 게시글 목록을 공통으로 조회하는 내부 메서드
     *
     * @param boardCodes 조회할 게시판 코드 목록
     * @param condition 검색 조건 (검색 필드 및 검색어)
     * @param pageable 페이징 및 정렬 정보
     * @return ApiResponse
     */
    private ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesCommon(
            List<String> boardCodes, SearchCondition condition, Pageable pageable) {

        Page<ArticleDTO> articles = service.getArticles(boardCodes, condition.getType(), condition.getKeyWord(), pageable);
        String message = messageSourceAccessor.getMessage(ARTICLE_LIST_SUCCESS);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, articles));
    }

    /**
     * 게시글 상세 정보를 공통으로 조회하는 내부 메서드
     *
     * @param boardCode 게시판 코드
     * @param articleSeq 게시글 ID (PK)
     * @return ApiResponse
     * @throws ArticleNotFoundException 게시글이 존재하지 않는 경우
     */
    private ResponseEntity<ApiResponse<ArticleDetailDto>> getArticleCommon(String boardCode, Integer articleSeq, HttpServletRequest request) {
        String clientIp = request != null ? request.getRemoteAddr() : "UNKNOWN";
        ArticleDetailDto article = service.getArticle(boardCode, articleSeq, clientIp);
        if (article == null) {
            log.warn("게시글 없음: boardCode={}, articleSeq={}", boardCode, articleSeq);
            throw new ArticleNotFoundException(articleSeq);
        }
        String message = messageSourceAccessor.getMessage(ARTICLE_DETAIL_SUCCESS);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, message, article));
    }

    /**
     * 공지사항 및 채용공고 게시글 목록 조회
     *
     * @param optionStr 게시판 구분 문자열
     * @param condition 검색 조건 (검색 필드 및 검색어)
     * @param pageable 페이징 정보
     * @return ApiResponse
     */
    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticles(
            @RequestParam(defaultValue = "all") String optionStr,
            @Validated @ModelAttribute SearchCondition condition,
            @PageableDefault(size = DEFAULT_ARTICLE_PAGE_SIZE) Pageable pageable
    ) {
        Pageable sortedPageable = applyDefaultSort(pageable);

        List<String> boardCodes;
        if ("all".equalsIgnoreCase(optionStr)) {
            boardCodes = boardCategoryCache.getAllBoardCodesForOptions();
        } else {
            String boardCode = boardCategoryCache.getBoardCodeByUrlParam(optionStr);
            boardCodes = boardCode != null ? List.of(boardCode) : List.of();
        }
        return getArticlesCommon(boardCodes, condition, sortedPageable);
    }

    /**
     * 공지사항/채용공고 게시글 상세 조회
     *
     * @param articleSeq 게시글 ID
     * @param optionStr 게시판 구분 문자열
     * @return ApiResponse
     */
    @GetMapping("/notices/{articleSeq}")
    public ResponseEntity<ApiResponse<ArticleDetailDto>> getArticle(
            @PathVariable Integer articleSeq,
            @RequestParam String optionStr,
            HttpServletRequest request
    ) {
        String boardCode = boardCategoryCache.getBoardCodeByUrlParam(optionStr);
        return getArticleCommon(boardCode, articleSeq, request);
    }

    /**
     * 공지사항/채용공고 게시글 첨부파일 다운로드
     *
     * @param articleSeq 게시글 ID
     * @param fileName 다운로드할 파일 이름
     * @param optionStr 게시판 구분 문자열
     * @return ResponseEntity
     */
    @GetMapping("/notices/{articleSeq}/files/{fileName}")
    public ResponseEntity<Object> downloadFile(
            @PathVariable Integer articleSeq,
            @PathVariable String fileName,
            @RequestParam String optionStr
    ) {
        String boardCode = boardCategoryCache.getBoardCodeByUrlParam(optionStr);
        DownloadFile file = fileDownloadService.loadDownloadFile(boardCode, articleSeq, fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + file.getEncodedFileName())
                .body(file.getResource());
    }



    /**
     * 일반 게시판 목록 조회 (공지/채용 외)
     *
     * @param boardCode 게시판 URL 코드
     * @param condition 검색 조건 (검색 필드 및 검색어)
     * @param pageable 페이징 정보
     * @return ApiResponse
     */
    @GetMapping("/{boardCode}")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getOtherBoardArticles(
            @PathVariable String boardCode,
            @Validated @ModelAttribute SearchCondition condition,
            @PageableDefault(size = DEFAULT_ARTICLE_PAGE_SIZE) Pageable pageable
    ) {
        Pageable sortedPageable = applyDefaultSort(pageable);
        String dbBoardCode = boardCategoryCache.getBoardCodeByUrlParam(boardCode);
        return getArticlesCommon(List.of(dbBoardCode), condition, sortedPageable);
    }

    /**
     * 일반 게시판 상세 조회 (공지/채용 외)
     *
     * @param boardCode 게시판 URL 코드
     * @param articleSeq 게시글 ID
     * @return ApiResponse
     */
    @GetMapping("/{boardCode}/{articleSeq}")
    public ResponseEntity<ApiResponse<ArticleDetailDto>> getOtherBoardArticle(
            @PathVariable String boardCode,
            @PathVariable Integer articleSeq,
            HttpServletRequest request
    ) {
        String dbBoardCode = boardCategoryCache.getBoardCodeByUrlParam(boardCode);
        return getArticleCommon(dbBoardCode, articleSeq, request);
    }

    /**
     * 일반 게시판 첨부파일 다운로드
     *
     * @param boardCode 게시판 URL 코드
     * @param articleSeq 게시글 ID
     * @param fileName 다운로드할 파일 이름
     * @return ResponseEntity
     */
    @GetMapping("/{boardCode}/{articleSeq}/files/{fileName}")
    public ResponseEntity<Object> downloadOtherBoardFile(
            @PathVariable String boardCode,
            @PathVariable Integer articleSeq,
            @PathVariable String fileName
    ) {
        String dbBoardCode = boardCategoryCache.getBoardCodeByUrlParam(boardCode);
        DownloadFile file = fileDownloadService.loadDownloadFile(dbBoardCode, articleSeq, fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + file.getEncodedFileName())
                .body(file.getResource());
    }

    private Pageable applyDefaultSort(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Order.desc("fixFlag"),
                        Sort.Order.desc("writeTime"),
                        Sort.Order.desc("articleSeq")
                )
        );
    }

}
