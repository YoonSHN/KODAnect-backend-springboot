package kodanect.domain.article.controller;

import kodanect.common.config.GlobalsProperties;
import kodanect.domain.article.dto.ArticleDTO;
import kodanect.domain.article.dto.ArticleDetailDto;
import kodanect.domain.article.dto.DownloadFile;
import kodanect.domain.article.repository.BoardCategoryCache;
import kodanect.domain.article.service.ArticleService;
import kodanect.domain.article.service.FileDownloadService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ArticleController.class)
public class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private MessageSourceAccessor messageSourceAccessor;

    @MockBean
    private GlobalsProperties globalsProperties;

    @MockBean
    private BoardCategoryCache boardCategoryCache;

    @MockBean
    private FileDownloadService fileDownloadService;

    @Before
    public void setUp() {
        when(messageSourceAccessor.getMessage("article.listSuccess"))
                .thenReturn("게시글 목록 조회에 성공했습니다.");
        when(messageSourceAccessor.getMessage("article.detailSuccess"))
                .thenReturn("게시글 상세 조회에 성공했습니다.");
        when(messageSourceAccessor.getMessage(eq("article.notFound"), any(), anyString()))
                .thenReturn("게시글을 찾을 수 없습니다.");
    }

    @Test
    public void testGetArticles() throws Exception {
        ArticleDTO dto = ArticleDTO.builder().title("공지사항 제목").build();

        when(boardCategoryCache.getBoardCodeByUrlParam("1")).thenReturn("7");

        when(articleService.getArticles(
                eq(List.of("7")),
                eq("all"),
                any(),
                any(PageRequest.class)))
                .thenReturn((Page) new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/notices")
                        .param("optionStr", "1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.content[0].title").value("공지사항 제목"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetOtherBoardArticles() throws Exception {
        ArticleDTO dto = ArticleDTO.builder().title("사전정보공개 제목").build();

        when(boardCategoryCache.getBoardCodeByUrlParam("makePublic")).thenReturn("32");

        when(articleService.getArticles(
                eq(List.of("32")),
                eq("all"),
                any(),
                any(PageRequest.class)))
                .thenReturn((Page) new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/makePublic")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.content[0].title").value("사전정보공개 제목"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetArticleDetail() throws Exception {
        ArticleDetailDto detailDto = ArticleDetailDto.builder()
                .title("상세 제목")
                .contents("상세 내용")
                .build();

        when(boardCategoryCache.getBoardCodeByUrlParam("1")).thenReturn("7");
        when(articleService.getArticle("7", 1)).thenReturn(detailDto);

        mockMvc.perform(get("/notices/1")
                        .param("optionStr", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 상세 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.title").value("상세 제목"))
                .andExpect(jsonPath("$.data.contents").value("상세 내용"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testInvalidSearchField() throws Exception {
        when(boardCategoryCache.getBoardCodeByUrlParam("1")).thenReturn("7");

        mockMvc.perform(get("/notices")
                        .param("optionStr", "1")
                        .param("type", "invalidField")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("검색 필드는 title, contents, all 중 하나여야 합니다."));
    }

    @Test
    public void testSearchKeywordTooLong() throws Exception {
        when(boardCategoryCache.getBoardCodeByUrlParam("1")).thenReturn("7");

        String longKeyword = "a".repeat(101);

        mockMvc.perform(get("/notices")
                        .param("optionStr", "1")
                        .param("type", "title")
                        .param("keyWord", longKeyword)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("검색어는 최대 100자까지 입력할 수 있습니다."));
    }

    @Test
    public void testSearchByTitleKeyword() throws Exception {
        ArticleDTO dto = ArticleDTO.builder()
                .title("검색된 제목")
                .build();

        when(boardCategoryCache.getBoardCodeByUrlParam("1")).thenReturn("7");

        when(articleService.getArticles(
                eq(List.of("7")),
                eq("title"),
                eq("검색어"),
                any(PageRequest.class)
        )).thenReturn((Page) new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/notices")
                        .param("optionStr", "1")
                        .param("type", "title")
                        .param("keyWord", "검색어")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.content[0].title").value("검색된 제목"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testSearchByContentsKeyword() throws Exception {
        ArticleDTO dto = ArticleDTO.builder()
                .title("본문 포함 게시글")
                .build();

        when(boardCategoryCache.getBoardCodeByUrlParam("1")).thenReturn("7");

        when(articleService.getArticles(
                eq(List.of("7")),
                eq("contents"),
                eq("내용"),
                any(PageRequest.class)
        )).thenReturn((Page) new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/notices")
                        .param("optionStr", "1")
                        .param("type", "contents")
                        .param("keyWord", "내용")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("본문 포함 게시글"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testSearchTypeFallbackToAllWhenMissing() throws Exception {
        ArticleDTO dto = ArticleDTO.builder()
                .title("기본 검색 게시글")
                .build();

        when(boardCategoryCache.getBoardCodeByUrlParam("1")).thenReturn("7");

        when(articleService.getArticles(
                eq(List.of("7")),
                eq("all"),
                eq("검색어"),
                any(PageRequest.class)
        )).thenReturn((Page) new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/notices")
                        .param("optionStr", "1")
                        .param("keyWord", "검색어")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시글 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.content[0].title").value("기본 검색 게시글"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testMissingOptionStrShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/notices")
                        .param("type", "title")
                        .param("keyWord", "공지")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testSearchWithoutKeywordShouldReturnAllArticles() throws Exception {
        ArticleDTO dto = ArticleDTO.builder().title("전체 목록 게시글").build();

        when(boardCategoryCache.getBoardCodeByUrlParam("1")).thenReturn("7");
        when(articleService.getArticles(
                eq(List.of("7")),
                eq("all"),
                eq(""),
                any(PageRequest.class)
        )).thenReturn((Page) new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/notices")
                        .param("optionStr", "1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("전체 목록 게시글"));
    }

    @Test
    public void testSearchWithNoResultShouldReturnEmptyList() throws Exception {
        when(boardCategoryCache.getBoardCodeByUrlParam("1")).thenReturn("7");
        when(articleService.getArticles(
                eq(List.of("7")),
                eq("title"),
                eq("없는키워드"),
                any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/notices")
                        .param("optionStr", "1")
                        .param("type", "title")
                        .param("keyWord", "없는키워드")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testNegativePageNumberShouldFail() throws Exception {
        mockMvc.perform(get("/notices")
                        .param("optionStr", "1")
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetOtherBoardArticleDetailNotFound() throws Exception {
        when(boardCategoryCache.getBoardCodeByUrlParam("makePublic")).thenReturn("32");
        when(articleService.getArticle("32", 99)).thenReturn(null);

        mockMvc.perform(get("/makePublic/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void testDownloadFile() throws Exception {
        // given
        String boardCode = "7";
        Integer articleSeq = 1;
        String fileName = "example.pdf";
        String encodedFileName = "example.pdf";
        String contentType = "application/pdf";

        byte[] fileContent = "dummy file content".getBytes();
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        DownloadFile mockFile = DownloadFile.builder()
                .encodedFileName(encodedFileName)
                .contentType(contentType)
                .resource(resource)
                .build();

        // when
        when(boardCategoryCache.getBoardCodeByUrlParam("1")).thenReturn(boardCode);
        when(fileDownloadService.loadDownloadFile(eq(boardCode), eq(articleSeq), eq(fileName)))
                .thenReturn(mockFile);

        // then
        mockMvc.perform(get("/notices/{articleSeq}/files/{fileName}", articleSeq, fileName)
                        .param("optionStr", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", contentType))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename*=UTF-8''" + encodedFileName))
                .andExpect(content().bytes(fileContent));
    }



}