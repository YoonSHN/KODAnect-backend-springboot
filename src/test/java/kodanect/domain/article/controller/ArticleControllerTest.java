package kodanect.domain.article.controller;

import kodanect.common.config.GlobalsProperties;
import kodanect.domain.article.dto.ArticleDTO;
import kodanect.domain.article.dto.ArticleDetailDto;
import kodanect.domain.article.service.ArticleService;
import kodanect.domain.article.util.StringToBoardOptionConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ArticleController.class)
@Import(StringToBoardOptionConverter.class)
public class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private MessageSourceAccessor messageSourceAccessor;

    @MockBean
    private GlobalsProperties globalsProperties;

    @Before
    public void setup() {
        when(messageSourceAccessor.getMessage("article.list.success"))
                .thenReturn("게시글 목록 조회에 성공했습니다.");
        when(messageSourceAccessor.getMessage("article.detail.success"))
                .thenReturn("게시글 상세 조회에 성공했습니다.");
    }

    @Test
    public void testGetArticles() throws Exception {
        when(articleService.getArticles(
                Collections.singletonList("notice"),
                eq("all"),
                null,
                PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(Collections.singletonList(new ArticleDTO())));

        mockMvc.perform(get("/newKoda/notices")
                        .param("optionStr", "1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 목록 조회에 성공했습니다."));
    }

    @Test
    public void testGetOtherBoardArticles() throws Exception {
        String boardCode = "";

        when(articleService.getArticles(
                eq(Collections.singletonList("")),
                eq("all"),
                eq(null),
                eq(PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(Collections.singletonList(new ArticleDTO())));

        mockMvc.perform(get("/newKoda/" + boardCode)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글 목록 조회에 성공했습니다."));
    }


    @Test
    public void testGetArticleDetail() throws Exception {
        ArticleDetailDto dto = ArticleDetailDto.builder()
                .title("테스트 제목")
                .contents("내용입니다")
                .build();

        when(articleService.getArticle(eq("7"), eq(1))).thenReturn(dto);

        mockMvc.perform(get("/newKoda/notices/1")
                        .param("optionStr", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("테스트 제목"))
                .andExpect(jsonPath("$.success").value(true));
    }

}
