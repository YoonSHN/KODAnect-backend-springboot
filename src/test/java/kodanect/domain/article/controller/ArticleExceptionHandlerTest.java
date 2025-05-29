package kodanect.domain.article.controller;

import kodanect.common.config.GlobalsProperties;
import kodanect.common.exception.config.ArticleExceptionHandler;
import kodanect.common.exception.custom.*;
import kodanect.domain.article.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static kodanect.common.exception.config.MessageKeys.FILE_NOT_FOUND;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(controllers = ArticleController.class)
@Import(ArticleExceptionHandler.class)
class ArticleExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private MessageSourceAccessor messageSourceAccessor;

    @MockBean
    private GlobalsProperties globalsProperties;

    @Test
    @DisplayName("게시글 없음 예외 - 404")
    void handleArticleNotFoundException() throws Exception {
        int articleSeq = 999;
        String optionStr = "1";

        when(articleService.getArticle(eq("7"), eq(articleSeq)))
                .thenThrow(new ArticleNotFoundException(articleSeq));
        when(messageSourceAccessor.getMessage(anyString(), any(Object[].class), anyString()))
                .thenReturn("게시글을 찾을 수 없습니다.");

        mockMvc.perform(get("/newKoda/notices/{articleSeq}", articleSeq)
                        .param("optionStr", optionStr)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("잘못된 게시판 옵션 - 400")
    void handleInvalidBoardOptionException() throws Exception {
        String invalidOption = "999";

        when(articleService.getArticles(null, null, null, null))
                .thenThrow(new InvalidBoardOptionException(invalidOption));
        when(messageSourceAccessor.getMessage(anyString(), any(Object[].class), anyString()))
                .thenReturn("유효하지 않은 게시판 옵션입니다.");

        mockMvc.perform(get("/newKoda/notices")
                        .param("optionStr", invalidOption)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("유효하지 않은 게시판 옵션입니다."));
    }

    @Test
    @DisplayName("잘못된 게시판 코드 - 400")
    void handleInvalidBoardCodeException() throws Exception {
        String boardCode = "INVALID";

        when(articleService.getArticle(eq("NOTICE"), eq(1)))
                .thenThrow(new InvalidBoardCodeException(boardCode));
        when(messageSourceAccessor.getMessage(anyString(), any(Object[].class), anyString()))
                .thenReturn("잘못된 게시판 코드입니다.");

        mockMvc.perform(get("/newKoda/{boardCode}/{articleSeq}", boardCode, 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("잘못된 게시판 코드입니다."));
    }
    @Test
    @DisplayName("파일 없음 예외 - 404")
    void handleFileMissingException() throws Exception {
        String optionStr = "1";
        int articleSeq = 1;
        String fileName = "notfound.pdf";


        when(globalsProperties.getFileStorePath()).thenReturn("/files");

        when(articleService.getArticle(anyString(), anyInt()))
                .thenReturn(null);

        String message = messageSourceAccessor.getMessage(FILE_NOT_FOUND);
        log.debug("file.notFound message = {}", message);

        when(messageSourceAccessor.getMessage(eq(FILE_NOT_FOUND), any(Object[].class), anyString()))
                .thenReturn("파일을 찾을 수 없습니다.");

        mockMvc.perform(get("/newKoda/notices/{articleSeq}/files/{fileName}", articleSeq, fileName)
                        .param("optionStr", optionStr)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("파일을 찾을 수 없습니다."));
    }
}