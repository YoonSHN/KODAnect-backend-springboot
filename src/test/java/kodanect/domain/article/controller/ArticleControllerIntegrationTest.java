//package kodanect.domain.article.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//public class ArticleControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void getArticles_success() throws Exception {
//        mockMvc.perform(get("/newKoda/notices")
//                        .param("optionStr", "all")
//                        .param("search", "")
//                        .param("page", "0")
//                        .param("size", "10")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.data.content").isArray());
//    }
//
//    @Test
//    void getArticle_detail_success() throws Exception {
//        mockMvc.perform(get("/newKoda/notices/{articleSeq}", 1)
//                        .param("optionStr", "1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.code").value(200))
//                .andExpect(jsonPath("$.data.articleSeq").value(1));
//    }
//
//    @Test
//    void downloadFile_suceess() throws Exception {
//        mockMvc.perform(get("/newKoda/notices/{articleSeq}/files/{fileName}", 1, "testfile.txt")
//                        .param("optionStr", "1"))
//                .andExpect(status().isOk())
//                .andExpect(header().exists("Content-Disposition"));
//    }
//}
