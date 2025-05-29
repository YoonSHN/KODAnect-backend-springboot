package kodanect.domain.article.controller;

import kodanect.common.config.GlobalsProperties;
import kodanect.domain.article.entity.*;
import kodanect.domain.article.repository.ArticleFileRepository;
import kodanect.domain.article.repository.ArticleRepository;
import kodanect.domain.article.repository.BoardCategoryRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ArticleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleFileRepository articleFileRepository;

    @Autowired
    private BoardCategoryRepository boardCategoryRepository;

    @Autowired
    private GlobalsProperties globalsProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(globalsProperties, "fileStorePath", tempDir.toString());

        BoardCategory category1 = BoardCategory.builder()
                .boardCode("7")
                .boardName("공지사항")
                .boardTypeCode("NTC")
                .fileCount(0)
                .htmlFlag("N")
                .replyFlag("N")
                .delFlag("N")
                .writeTime(LocalDateTime.now())
                .writerId("admin")
                .modifyTime(LocalDateTime.now())
                .modifierId("admin")
                .build();

        BoardCategory category2 = BoardCategory.builder()
                .boardCode("32")
                .boardName("사전정보공개")
                .boardTypeCode("INFO")
                .fileCount(0)
                .htmlFlag("N")
                .replyFlag("N")
                .delFlag("N")
                .writeTime(LocalDateTime.now())
                .writerId("admin")
                .modifyTime(LocalDateTime.now())
                .modifierId("admin")
                .build();

        boardCategoryRepository.save(category1);
        boardCategoryRepository.save(category2);



        ArticleId articleId = new ArticleId("7", 1);
        Article article = Article.builder()
                .id(articleId)
                .title("트랜잭션 테스트")
                .contents("본문 내용입니다.")
                .writerId("tester")
                .readCount(0)
                .fixFlag("N")
                .writeTime(LocalDateTime.now())
                .delFlag("N")
                .modifierId("admin")
                .modifyTime(LocalDateTime.now())
                .build();

        articleRepository.save(article);

        ArticleId makePublicId = new ArticleId("32", 2);
        Article makePublic = Article.builder()
                .id(makePublicId)
                .title("사전정보공개 제목")
                .contents("본문 내용입니다.")
                .writerId("tester2")
                .readCount(0)
                .fixFlag("N")
                .writeTime(LocalDateTime.now())
                .delFlag("N")
                .modifierId("admin")
                .modifyTime(LocalDateTime.now())
                .build();

        articleRepository.save(makePublic);
    }

    @Test
    @DisplayName("게시글 목록 조회")
    void getArticles() throws Exception {
        mockMvc.perform(get("/newKoda/notices")
                        .param("optionStr", "1")
                        .param("searchField", "title")
                        .param("search", "트랜잭션")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("트랜잭션 테스트"))
                .andDo(print());
    }

    @Test
    @DisplayName("사전정보 게시판 게시글 목록 조회")
    void getOtherBoardArticles() throws Exception {
        mockMvc.perform(get("/newKoda/makePublic")
                        .param("searchField", "title")
                        .param("search", "사전정보공개")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("사전정보공개 제목"))
                .andDo(print());
    }

    @Test
    @DisplayName("사전정보 게시판 상세 조회")
    void getOtherBoardArticleDetail() throws Exception {
        mockMvc.perform(get("/newKoda/makePublic/2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("사전정보공개 제목"))
                .andDo(print());
    }

    @Test
    @DisplayName("사전정보 게시판 첨부파일 다운로드 - 성공")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void downloadOtherBoardFile() throws Exception {
        String rootPath = globalsProperties.getFileStorePath();
        Path uploadPath = Paths.get(rootPath, "32", "2");
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve("sample.txt");
        Files.writeString(filePath, "샘플 파일 내용입니다.", StandardCharsets.UTF_8);

        ArticleFile file = ArticleFile.builder()
                .id(new ArticleFileId("32", 2, 1))
                .fileName("sample.txt")
                .orgFileName("sample.txt")
                .filePathName(Paths.get("32", "2", "sample.txt").toString())
                .writerId("tester2")
                .writeTime(LocalDateTime.now())
                .modifierId("admin")
                .modifyTime(LocalDateTime.now())
                .delFlag("N")
                .build();

        articleFileRepository.save(file);

        mockMvc.perform(get("/newKoda/makePublic/2/files/sample.txt"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("filename*=UTF-8''sample.txt")))
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
                    assertThat(body).isEqualTo("샘플 파일 내용입니다.");
                });
    }

    @Test
    @DisplayName("사전정보 게시판 첨부파일 다운로드 - 파일 없음")
    void downloadOtherBoardFileNotFound() throws Exception {
        mockMvc.perform(get("/newKoda/makePublic/2/files/notfound.txt")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 상세 조회")
    void getArticleDetail() throws Exception {
        mockMvc.perform(get("/newKoda/notices/1")
                        .param("optionStr", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("트랜잭션 테스트"))
                .andDo(print());
    }

    @Test
    @DisplayName("첨부파일 다운로드 - 파일 미존재 시 오류 처리")
    void downloadFileNotFound() throws Exception {
        mockMvc.perform(get("/newKoda/notices/1/files/nonexistent.txt")
                        .param("optionStr", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());
    }
}
