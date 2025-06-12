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
    }

    @Test
    public void testGetArticles() throws Exception {
        ArticleDTO dto = ArticleDTO.builder().title("공지사항 제목").build();

        when(boardCategoryCache.getBoardCodeByUrlParam("1")).thenReturn("7");

        when(articleService.getArticles(
                eq(List.of("7")),
                eq("all"),
                isNull(),
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
                isNull(),
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
                .andExpect(jsonPath("$.message").value("검색 필드는 title, content, all 중 하나여야 합니다."));
    }

    @Test
    public void testSearchKeywordTooLong() throws Exception {
        when(boardCategoryCache.getBoardCodeByUrlParam("1")).thenReturn("7");

        String longKeyword = "a".repeat(101);

        mockMvc.perform(get("/notices")
                        .param("optionStr", "1")
                        .param("type", "title")
                        .param("search", longKeyword)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("검색어는 최대 100자까지 입력할 수 있습니다."));
    }


}
