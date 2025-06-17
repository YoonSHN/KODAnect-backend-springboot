package kodanect.domain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.KodanectBootApplication;
import kodanect.common.exception.config.GlobalExcepHndlr;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.domain.donation.controller.DonationController;
import kodanect.domain.donation.dto.request.*;
import kodanect.domain.donation.dto.response.*;
import kodanect.domain.donation.exception.DonationNotFoundException;
import kodanect.domain.donation.exception.PasscodeMismatchException;
import kodanect.domain.donation.service.DonationCommentService;
import kodanect.domain.donation.service.DonationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DonationController.class)
@Import(GlobalExcepHndlr.class)
class DonationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DonationService donationService;

    @MockBean
    private DonationCommentService donationCommentService;

    @MockBean
    private MessageSourceAccessor messageSourceAccessor;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUpLocale() {
        Locale.setDefault(Locale.KOREA); // 또는 new Locale("ko", "KR")
    }

    // 1) GET /donationLetters – 전체 목록
    @Test
    @DisplayName("GET /donationLetters - 성공")
    void getAllDonationList_success() throws Exception {
        DonationStoryListDto dto = new DonationStoryListDto(
                1L, "제목1", "글쓴이1", 0, LocalDate.now()
        );
        CursorPaginationResponse<DonationStoryListDto, Long> pageResp =
                CursorPaginationResponse.<DonationStoryListDto, Long>builder()
                        .content(List.of(dto))
                        .nextCursor(null)
                        .hasNext(false)
                        .build();

        given(donationService.findStoriesWithCursor(any(Long.class), anyInt()))
                .willReturn(pageResp);
        given(messageSourceAccessor.getMessage("board.list.get.success"))
                .willReturn("게시글 목록 조회를 성공했습니다.");

        mockMvc.perform(get("/donationLetters")
                        .param("cursor", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].storySeq").value(1));
    }

    @Test
    @DisplayName("GET /donationLetters - 실패 (서비스 예외→500)")
    void getAllDonationList_failure() throws Exception {
        given(donationService.findStoriesWithCursor(any(Long.class), anyInt()))
                .willThrow(new RuntimeException("DB 오류"));
        given(messageSourceAccessor.getMessage("error.internal"))
                .willReturn("서버 내부 오류");

        mockMvc.perform(get("/donationLetters")
                        .param("cursor", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    // 2) GET /donationLetters/search – 검색
    @Test
    @DisplayName("GET /donationLetters/search - 성공")
    void searchDonationStories_success() throws Exception {
        DonationStoryListDto dto = new DonationStoryListDto(
                2L, "검색제목", "검색작성자", 5, LocalDate.now()
        );
        CursorPaginationResponse<DonationStoryListDto, Long> pageResp =
                CursorPaginationResponse.<DonationStoryListDto, Long>builder()
                        .content(List.of(dto))
                        .nextCursor(null)
                        .hasNext(false)
                        .build();

        given(donationService.findSearchStoriesWithCursor(eq("제목"), eq("키워드"), any(Long.class), anyInt()))
                .willReturn(pageResp);
        given(messageSourceAccessor.getMessage("article.detailSuccess"))
                .willReturn("검색 성공");

        mockMvc.perform(get("/donationLetters/search")
                        .param("type", "제목")
                        .param("keyword", "키워드")
                        .param("cursor", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].storySeq").value(2));
    }

    @Test
    @DisplayName("GET /donationLetters/search - 빈 결과")
    void searchDonationStories_empty() throws Exception {
        CursorPaginationResponse<DonationStoryListDto, Long> pageResp =
                CursorPaginationResponse.<DonationStoryListDto, Long>builder()
                        .content(List.of())
                        .nextCursor(null)
                        .hasNext(false)
                        .build();

        given(donationService.findSearchStoriesWithCursor(eq("잘못된"), eq("키워드"), any(Long.class), anyInt()))
                .willReturn(pageResp);
        given(messageSourceAccessor.getMessage("article.detailSuccess"))
                .willReturn("검색 성공");

        mockMvc.perform(get("/donationLetters/search")
                        .param("type", "잘못된")
                        .param("keyword", "키워드")
                        .param("cursor", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    // 3) GET /donationLetters/new – 작성 폼 데이터
    @Test
    @DisplayName("GET /donationLetters/new - 성공")
    void getDonationWriteForm_success() throws Exception {
        DonationStoryWriteFormDto formDto = DonationStoryWriteFormDto.builder()
                .areaOptions(List.of(AreaCode.AREA100, AreaCode.AREA200))
                .build();
        given(donationService.loadDonationStoryFormData()).willReturn(formDto);

        mockMvc.perform(get("/donationLetters/new")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.areaOptions.length()").value(2));
    }

    // 4) POST /donationLetters – 등록
    @Test
    @DisplayName("POST /donationLetters - 성공 (201)")
    void createStory_success() throws Exception {
        given(messageSourceAccessor.getMessage("donation.create.success"))
                .willReturn("등록 성공");
        doNothing().when(donationService).createDonationStory(any(DonationStoryCreateRequestDto.class));

        mockMvc.perform(post("/donationLetters")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("areaCode", "AREA100")
                        .param("storyTitle", "제목")
                        .param("storyPasscode", "abcd1234")
                        .param("storyWriter", "작가")
                        .param("storyContents", "")
                        .param("captchaToken", "token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("등록 성공"));
    }

    // 5) GET /donationLetters/{storySeq} – 상세 조회
    @Test
    @DisplayName("GET /donationLetters/{storySeq} - 성공")
    void getDonationStoryDetail_success() throws Exception {
        DonationStoryDetailDto detailDto = DonationStoryDetailDto.builder()
                .storySeq(1L)
                .title("제목")
                .storyWriter("작가")
                .uploadDate("2025-06-12")
                .areaCode(AreaCode.AREA100)
                .readCount(0)
                .storyContent("본문")
                .fileName(null)
                .orgFileName(null)
                .build();
        given(donationService.findDonationStoryWithStoryId(1L)).willReturn(detailDto);
        given(messageSourceAccessor.getMessage("board.read.success"))
                .willReturn("조회 성공");

        mockMvc.perform(get("/donationLetters/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storySeq").value(1))
                .andExpect(jsonPath("$.message").value("조회 성공"));
    }

    @Test
    @DisplayName("GET /donationLetters/{storySeq} - 실패 (404)")
    void getDonationStoryDetail_notFound() throws Exception {
        doThrow(new DonationNotFoundException("donation.error.notfound"))
                .when(donationService).findDonationStoryWithStoryId(999L);
        given(messageSourceAccessor.getMessage("donation.error.notfound"))
                .willReturn("없음");

        mockMvc.perform(get("/donationLetters/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("없음"));
    }

    // 6) POST /{storySeq}/verifyPwd – 비밀번호 검증
    @Test
    @DisplayName("POST /donationLetters/{storySeq}/verifyPwd - 성공")
    void verifyStoryPassword_success() throws Exception {
        VerifyStoryPasscodeDto req = new VerifyStoryPasscodeDto("abcd1234");
        DonationStoryDetailDto detailDto = DonationStoryDetailDto.builder()
                .storySeq(1L).title("제목").storyWriter("글쓴이").storyContent("내용1").build();

        given(messageSourceAccessor.getMessage("donation.password.match"))
                .willReturn("비밀번호 일치");
        given(donationService.findDonationStoryWithStoryId(1L)).willReturn(detailDto);


        doNothing().when(donationService).verifyPasswordWithPassword(eq(1L), any(VerifyStoryPasscodeDto.class));

        mockMvc.perform(post("/donationLetters/1/verifyPwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("POST /donationLetters/{storySeq}/verifyPwd - 실패 (400)")
    void verifyStoryPassword_mismatch() throws Exception {
        VerifyStoryPasscodeDto req = new VerifyStoryPasscodeDto("wrongpass");
        given(messageSourceAccessor.getMessage("donation.comment.verify.passcode.blank"))
                .willReturn("패스코드 없음");
        doThrow(new PasscodeMismatchException("donation.comment.verify.passcode.blank"))
                .when(donationService).verifyPasswordWithPassword(eq(1L), any(VerifyStoryPasscodeDto.class));

        mockMvc.perform(post("/donationLetters/1/verifyPwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("패스코드 없음"));
    }

    // 7) PATCH /{storySeq} – 수정
    @Test
    @DisplayName("PATCH /donationLetters/{storySeq} - 성공")
    void modifyStory_success() throws Exception {
        DonationStoryModifyRequestDto reqDto = DonationStoryModifyRequestDto.builder()
                .areaCode(AreaCode.AREA100)
                .storyTitle("수정제목")
                .storyWriter("수정작가")
                .storyContents("수정본문")
                .build();
        given(messageSourceAccessor.getMessage("donation.update.success"))
                .willReturn("수정 성공");
        doNothing().when(donationService).updateDonationStory(eq(1L), any(DonationStoryModifyRequestDto.class));

        mockMvc.perform(patch("/donationLetters/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("areaCode", "AREA100")
                        .param("storyTitle", "수정제목")
                        .param("storyWriter", "수정작가")
                        .param("storyContents", "수정본문"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("수정 성공"));
    }

    // 8) DELETE /{storySeq} – 삭제
    @Test
    @DisplayName("DELETE /donationLetters/{storySeq} - 성공")
    void deleteStory_success() throws Exception {
        VerifyStoryPasscodeDto req = new VerifyStoryPasscodeDto("abcd1234");
        given(messageSourceAccessor.getMessage("donation.delete.success"))
                .willReturn("삭제 성공");
        doNothing().when(donationService).deleteDonationStory(eq(1L), any(VerifyStoryPasscodeDto.class));

        mockMvc.perform(delete("/donationLetters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("삭제 성공"));
    }

    @Test
    @DisplayName("DELETE /donationLetters/{storySeq} - 실패 (500)")
    void deleteStory_internalError() throws Exception {
        VerifyStoryPasscodeDto req = new VerifyStoryPasscodeDto("abcd1234");
        given(messageSourceAccessor.getMessage("error.internal"))
                .willReturn("서버 오류");
        doThrow(new RuntimeException("DB 오류"))
                .when(donationService).deleteDonationStory(eq(1L), any(VerifyStoryPasscodeDto.class));

        mockMvc.perform(delete("/donationLetters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("서버 오류"));
    }

    // 9) GET /{storySeq}/comments – 댓글 목록 조회
    @Test
    @DisplayName("GET /donationLetters/{storySeq}/comments - 성공")
    void getAllDonationCommentList_success() throws Exception {
        DonationStoryCommentDto comment = DonationStoryCommentDto.builder()
                .commentSeq(5L)
                .commentWriter("댓글작성자")
                .comments("댓글본문")
                .commentWriteTime(LocalDateTime.now())
                .build();
        CursorPaginationResponse<DonationStoryCommentDto, Long> pageResp =
                CursorPaginationResponse.<DonationStoryCommentDto, Long>builder()
                        .content(List.of(comment))
                        .nextCursor(null)
                        .hasNext(false)
                        .build();

        given(donationCommentService.findCommentsWithCursor(1L, null, 3))
                .willReturn(pageResp);
        given(messageSourceAccessor.getMessage("donation.commentSuccess"))
                .willReturn("댓글 조회 성공");

        mockMvc.perform(get("/donationLetters/1/comments")
                        .param("size", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].commentSeq").value(5))
                .andExpect(jsonPath("$.message").value("댓글 조회 성공"));
    }

    @Test
    @DisplayName("GET /donationLetters/{storySeq}/comments - 실패 (예: 스토리 없음)")
    void getAllDonationCommentList_notFound() throws Exception {
        given(donationCommentService.findCommentsWithCursor(999L, null, 3))
                .willThrow(new DonationNotFoundException("donation.error.notfound"));
        given(messageSourceAccessor.getMessage("donation.error.notfound"))
                .willReturn("댓글 스토리를 찾을 수 없음");

        mockMvc.perform(get("/donationLetters/999/comments")
                        .param("size", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("댓글 스토리를 찾을 수 없음"));
    }

    // 10) POST /{storySeq}/comments – 댓글 등록
    @Test
    @DisplayName("POST /donationLetters/{storySeq}/comments - 성공")
    void createComment_success() throws Exception {
        DonationCommentCreateRequestDto req = DonationCommentCreateRequestDto.builder()
                .commentWriter("C")
                .commentPasscode("pwd")
                .contents("T")
                .build();
        given(messageSourceAccessor.getMessage("donation.comment.create.success"))
                .willReturn("댓글 등록 성공");
        doNothing().when(donationCommentService)
                .createDonationStoryComment(eq(1L), any(DonationCommentCreateRequestDto.class));

        mockMvc.perform(post("/donationLetters/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 등록 성공"));
    }

    @Test
    @DisplayName("POST /donationLetters/{storySeq}/comments - 실패 (500)")
    void createComment_internalError() throws Exception {
        DonationCommentCreateRequestDto req = DonationCommentCreateRequestDto.builder()
                .commentWriter("C")
                .commentPasscode("pwd")
                .contents("T")
                .build();
        given(messageSourceAccessor.getMessage("error.internal"))
                .willReturn("서버 오류");
        doThrow(new RuntimeException("Err"))
                .when(donationCommentService)
                .createDonationStoryComment(eq(1L), any(DonationCommentCreateRequestDto.class));

        mockMvc.perform(post("/donationLetters/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("서버 오류"));
    }

    // 11) PATCH /{storySeq}/comments/{commentSeq} – 댓글 수정
    @Test
    @DisplayName("PATCH /donationLetters/{storySeq}/comments/{commentSeq} - 성공")
    void modifyComment_success() throws Exception {
        DonationStoryCommentModifyRequestDto req = DonationStoryCommentModifyRequestDto.builder()
                .commentWriter("X")
                .contents("수정댓글")
                .build();
        given(messageSourceAccessor.getMessage("donation.comment.update.success"))
                .willReturn("댓글 수정 성공");
        doNothing().when(donationCommentService)
                .updateDonationComment(eq(1L), eq(2L), any(DonationStoryCommentModifyRequestDto.class));

        mockMvc.perform(patch("/donationLetters/1/comments/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 수정 성공"));
    }

    // 12) DELETE /{storySeq}/comments/{commentSeq} – 댓글 삭제
    @Test
    @DisplayName("DELETE /donationLetters/{storySeq}/comments/{commentSeq} - 성공")
    void deleteComment_success() throws Exception {
        VerifyCommentPasscodeDto req = VerifyCommentPasscodeDto.builder()
                .commentPasscode("pwd")
                .build();
        given(messageSourceAccessor.getMessage("donation.comment.delete.success"))
                .willReturn("댓글 삭제 성공");
        doNothing().when(donationCommentService)
                .deleteDonationComment(eq(1L), eq(2L), any(VerifyCommentPasscodeDto.class));

        mockMvc.perform(delete("/donationLetters/1/comments/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("댓글 삭제 성공"));
    }

    @Test
        @DisplayName("DELETE /{storySeq}/comments/{commentSeq} - 실패 (500)")
        void deleteComment_internalError() throws Exception {
            VerifyCommentPasscodeDto req = VerifyCommentPasscodeDto.builder()
                .commentPasscode("pwd")
                .build();
        given(messageSourceAccessor.getMessage("error.internal"))
                .willReturn("서버 오류");
        doThrow(new RuntimeException("DB 오류"))
                .when(donationCommentService)
                .deleteDonationComment(eq(1L), eq(2L), any(VerifyCommentPasscodeDto.class));

        mockMvc.perform(delete("/donationLetters/1/comments/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("서버 오류"));
    }

    //수정 인증
    @Test
    @DisplayName("POST /donationLetters/{storySeq}/comments/{commentSeq}/verifyPwd - 성공")
    void verifyCommentPassword_success() throws Exception {
        VerifyCommentPasscodeDto req = new VerifyCommentPasscodeDto("pwd");
        given(messageSourceAccessor.getMessage("donation.password.match"))
                .willReturn("비밀번호 일치");

        doNothing().when(donationCommentService)
                .verifyPasswordWithPassword(eq(1L), eq(2L), any(VerifyCommentPasscodeDto.class));

        mockMvc.perform(post("/donationLetters/1/comments/2/verifyPwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호 일치"));
    }

    @Test
    @DisplayName("POST /donationLetters/{storySeq}/comments/{commentSeq}/verifyPwd - 실패 (400)")
    void verifyCommentPassword_fail() throws Exception {
        VerifyCommentPasscodeDto req = new VerifyCommentPasscodeDto("wrong");
        given(messageSourceAccessor.getMessage("donation.comment.verify.passcode.blank"))
                .willReturn("비밀번호 불일치");

        doThrow(new PasscodeMismatchException("donation.comment.verify.passcode.blank"))
                .when(donationCommentService)
                .verifyPasswordWithPassword(eq(1L), eq(2L), any(VerifyCommentPasscodeDto.class));

        mockMvc.perform(post("/donationLetters/1/comments/2/verifyPwd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호 불일치"));
    }
}