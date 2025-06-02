package kodanect.domain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.KodanectBootApplication;
import kodanect.common.exception.config.GlobalExcepHndlr;
import kodanect.domain.donation.controller.DonationController;
import kodanect.domain.donation.dto.OffsetBasedPageRequest;
import kodanect.domain.donation.dto.request.*;
import kodanect.domain.donation.dto.response.AreaCode;
import kodanect.domain.donation.dto.response.DonationStoryDetailDto;
import kodanect.domain.donation.dto.response.DonationStoryListDto;
import kodanect.domain.donation.dto.response.DonationStoryWriteFormDto;
import kodanect.domain.donation.exception.DonationNotFoundException;
import kodanect.domain.donation.service.DonationCommentService;
import kodanect.domain.donation.service.DonationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DonationController의 핵심 엔드포인트만 “성공/실패(대표 케이스)”를 간단히 검증하는 최소 단위 테스트
 */
@WebMvcTest(DonationController.class)
@ContextConfiguration(classes = KodanectBootApplication.class)
@Import(GlobalExcepHndlr.class)
public class DonationControllerTest {

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


    // -------------------------------------------------------
    // 1) GET /donationLetters – 전체 목록 조회
    // -------------------------------------------------------

    @Test
    @DisplayName("GET /donationLetters - 성공")
    void getAllDonationList_success() throws Exception {
        // given
        DonationStoryListDto dto = new DonationStoryListDto(
                1L, "제목1", "글쓴이1", 0, LocalDateTime.now()
        );
        List<DonationStoryListDto> content = List.of(dto);
        Pageable pageable = new OffsetBasedPageRequest(0, 20, Sort.by("storySeq").descending());
        Slice<DonationStoryListDto> slice = new PageImpl<>(content, pageable, 1);

        given(donationService.findStoriesWithOffset(any(Pageable.class))).willReturn(slice);
        given(messageSourceAccessor.getMessage("board.list.get.success"))
                .willReturn("게시글 목록 조회를 성공했습니다.");

        // when & then
        mockMvc.perform(get("/donationLetters")
                        .param("offset", "0")
                        .param("limit", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())                              // HTTP 200
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))               // body.code == 200
                .andExpect(jsonPath("$.message").value("게시글 목록 조회를 성공했습니다."))
                .andExpect(jsonPath("$.data.content[0].storySeq").value(1L))
                .andExpect(jsonPath("$.data.content[0].storyTitle").value("제목1"))
                .andExpect(jsonPath("$.data.pageable.offset").value(0));
    }

    @Test
    @DisplayName("GET /donationLetters - 실패 (서비스 예외 발생 → 500)")
    void getAllDonationList_failure() throws Exception {
        given(donationService.findStoriesWithOffset(any(Pageable.class)))
                .willThrow(new RuntimeException("DB 오류"));
        given(messageSourceAccessor.getMessage("error.internal"))
                .willReturn("서버 내부 오류가 발생했습니다.");
        given(messageSourceAccessor.getMessage(eq("error.internal"), anyString()))
                .willReturn("서버 내부 오류가 발생했습니다.");
        mockMvc.perform(get("/donationLetters")
                        .param("offset", "0")
                        .param("limit", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError())           // HTTP 500
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500))              // body.code == 500
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }


    // -------------------------------------------------------
    // 2) GET /donationLetters/search – 검색
    // -------------------------------------------------------

    @Test
    @DisplayName("GET /donationLetters/search - 성공")
    void searchDonationStories_success() throws Exception {
        DonationStoryListDto dto = new DonationStoryListDto(
                2L, "검색제목", "검색작성자", 5, LocalDateTime.now()
        );
        List<DonationStoryListDto> content = List.of(dto);
        Pageable pageable = new OffsetBasedPageRequest(0, 5, Sort.by("storySeq").descending());
        Slice<DonationStoryListDto> slice = new PageImpl<>(content, pageable, 1);

        given(donationService.findDonationStorySearchResult(any(Pageable.class), eq("제목"), eq("키워드")))
                .willReturn(slice);
        given(messageSourceAccessor.getMessage("board.list.get.success"))
                .willReturn("검색 성공");

        mockMvc.perform(get("/donationLetters/search")
                        .param("type", "제목")
                        .param("keyword", "키워드")
                        .param("offset", "0")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("검색 성공"))
                .andExpect(jsonPath("$.data.items[0].storySeq").value(2));
    }

    @Test
    @DisplayName("GET /donationLetters/search - 빈 결과")
    void searchDonationStories_empty() throws Exception {
        Pageable pageable = new OffsetBasedPageRequest(0, 5, Sort.by("storySeq").descending());
        Slice<DonationStoryListDto> emptySlice = new PageImpl<>(List.of(), pageable, 0);

        given(donationService.findDonationStorySearchResult(any(Pageable.class), eq("잘못된"), eq("키워드")))
                .willReturn(emptySlice);
        given(messageSourceAccessor.getMessage("board.list.get.success"))
                .willReturn("검색 성공");

        mockMvc.perform(get("/donationLetters/search")
                        .param("type", "잘못된")
                        .param("keyword", "키워드")
                        .param("offset", "0")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isEmpty());
    }


    // -------------------------------------------------------
    // 3) GET /donationLetters/new – 작성 폼 데이터
    // -------------------------------------------------------

    @Test
    @DisplayName("GET /donationLetters/new - 성공")
    void getDonationWriteForm_success() throws Exception {
        DonationStoryWriteFormDto formDto = DonationStoryWriteFormDto.builder()
                .areaOptions(List.of(AreaCode.AREA100, AreaCode.AREA200))
                .build();
        given(donationService.loadDonationStoryFormData()).willReturn(formDto);

        mockMvc.perform(get("/donationLetters/new")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.areaOptions.length()").value(2));
    }


    // -------------------------------------------------------
    // 4) POST /donationLetters – 등록
    // -------------------------------------------------------

    @Test
    @DisplayName("POST /donationLetters - 성공 (HTTP 201 & 메시지 확인)")
    void createStory_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "hello".getBytes());
        doNothing().when(donationService).createDonationStory(any(DonationStoryCreateRequestDto.class));
        given(messageSourceAccessor.getMessage("donation.create.success"))
                .willReturn("스토리가 성공적으로 등록되었습니다.");

        mockMvc.perform(multipart("/donationLetters")
                        .file(file)
                        .param("areaCode", "AREA100")
                        .param("storyTitle", "제목1")
                        .param("storyPasscode", "abcd1234")
                        .param("storyContents", "내용")
                        .param("storyWriter", "작성자1")
                        .param("captchaToken", "dummy")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("스토리가 성공적으로 등록되었습니다."));
    }



    // -------------------------------------------------------
    // 5) GET /donationLetters/{storySeq} – 상세 조회
    // -------------------------------------------------------

    @Test
    @DisplayName("GET /donationLetters/{storySeq} - 성공")
    void getDonationStoryDetail_success() throws Exception {
        Long storySeq = 1L;
        DonationStoryDetailDto detailDto = DonationStoryDetailDto.builder()
                .storySeq(storySeq)
                .title("제목1")
                .storyWriter("작성자1")
                .areaCode(AreaCode.AREA100)
                .readCount(0)
                .storyContent("상세 내용")
                .fileName("file123")
                .orgFileName("origin.jpg")
                .build();
        given(donationService.findDonationStory(storySeq)).willReturn(detailDto);
        given(messageSourceAccessor.getMessage("article.detail.success"))
                .willReturn("게시글 상세를 성공적으로 조회했습니다.");

        mockMvc.perform(get("/donationLetters/{storySeq}", storySeq)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storySeq").value(1))
                .andExpect(jsonPath("$.message").value("게시글 상세를 성공적으로 조회했습니다."));

    }

    @Test
    @DisplayName("GET /donationLetters/{storySeq} - 실패 (NotFound → 404)")
    void getDonationStoryDetail_notFound() throws Exception {
        Long storySeq = 999L;
        doThrow(new DonationNotFoundException("donation.error.notfound"))
                .when(donationService).findDonationStory(storySeq);
        given(messageSourceAccessor.getMessage("donation.error.notfound"))
                .willReturn("해당 스토리를 찾을 수 없습니다.");

        mockMvc.perform(get("/donationLetters/{storySeq}", storySeq)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("해당 스토리를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("GET /donationLetters/{storySeq} - 실패 (서버 예외 → 500)")
    void getDonationStoryDetail_internalError() throws Exception {
        Long storySeq = 999L;
        doThrow(new RuntimeException("DB 오류"))
                .when(donationService).findDonationStory(storySeq);
        given(messageSourceAccessor.getMessage("error.internal"))
                .willReturn("서버 내부 오류가 발생했습니다.");

        mockMvc.perform(get("/donationLetters/{storySeq}", storySeq)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }


    // -------------------------------------------------------
    // 6) POST /donationLetters/{storySeq}/verifyPwd – 비밀번호 검증
    // -------------------------------------------------------

    @Test
    @DisplayName("POST /donationLetters/{storySeq}/verifyPwd - 성공")
    void verifyStoryPassword_success() throws Exception {
        Long storySeq = 1L;
        VerifyStoryPasscodeDto reqDto = new VerifyStoryPasscodeDto("correctPwd");

        doNothing().when(donationService)
                .verifyPasswordWithPassword(eq(storySeq), any(VerifyStoryPasscodeDto.class));
        given(messageSourceAccessor.getMessage("donation.password.match"))
                .willReturn("비밀번호가 일치합니다.");

        mockMvc.perform(post("/donationLetters/{storySeq}/verifyPwd", storySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치합니다."))
                .andExpect(jsonPath("$.data.result").value(1));
    }

    @Test
    @DisplayName("POST /donationLetters/{storySeq}/verifyPwd - 실패 (비밀번호 불일치 → 400)")
    void verifyStoryPassword_mismatch() throws Exception {
        Long storySeq = 1L;
        VerifyStoryPasscodeDto reqDto = new VerifyStoryPasscodeDto("wrongPwd");

        doThrow(new IllegalArgumentException("donation.error.delete.password_mismatch"))
                .when(donationService).verifyPasswordWithPassword(eq(storySeq), any(VerifyStoryPasscodeDto.class));
        given(messageSourceAccessor.getMessage("donation.error.delete.password_mismatch"))
                .willReturn("비밀번호가 일치하지 않습니다.");

        mockMvc.perform(post("/donationLetters/{storySeq}/verifyPwd", storySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."))
        // 실패 시 data.result는 기본적으로 없으므로 검사하지 않음
        ;
    }

    @Test
    @DisplayName("POST /donationLetters/{storySeq}/verifyPwd - 실패 (서버 예외 → 500)")
    void verifyStoryPassword_internalError() throws Exception {
        Long storySeq = 1L;
        VerifyStoryPasscodeDto reqDto = new VerifyStoryPasscodeDto("anyPwd");

        doThrow(new RuntimeException("예상치 못한 오류"))
                .when(donationService).verifyPasswordWithPassword(eq(storySeq), any());
        given(messageSourceAccessor.getMessage("error.internal"))
                .willReturn("서버 내부 오류가 발생했습니다.");

        mockMvc.perform(post("/donationLetters/{storySeq}/verifyPwd", storySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }


    // -------------------------------------------------------
    // 7) PATCH /donationLetters/{storySeq} – 수정
    // -------------------------------------------------------

    @Test
    @DisplayName("PATCH /donationLetters/{storySeq} - 성공")
    void modifyStory_success() throws Exception {
        Long storySeq = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "edit.png", "image/png", "dummy".getBytes());
        DonationStoryModifyRequestDto reqDto = DonationStoryModifyRequestDto.builder()
                .areaCode(AreaCode.AREA100)
                .storyTitle("수정제목")
                .storyWriter("수정작성자")
                .storyContents("수정내용")
                .file(file)
                .captchaToken("token")
                .build();

        doNothing().when(donationService).modifyDonationStory(eq(storySeq), any(DonationStoryModifyRequestDto.class));
        given(messageSourceAccessor.getMessage("donation.update.success"))
                .willReturn("스토리가 성공적으로 수정되었습니다.");

        mockMvc.perform(multipart("/donationLetters/{storySeq}", storySeq)
                        .file(file)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .param("areaCode", reqDto.getAreaCode().name())
                        .param("storyTitle", reqDto.getStoryTitle())
                        .param("storyWriter", reqDto.getStoryWriter())
                        .param("storyContents", reqDto.getStoryContents())
                        .param("captchaToken", reqDto.getCaptchaToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                // 컨트롤러가 ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, ...)) 형태로 반환하므로
                // HTTP Status는 200이지만 body.code 필드는 201이어야 한다.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("스토리가 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("PATCH /donationLetters/{storySeq} - 실패 (필수 입력 누락 → 400, 스프링 검증)")
    void modifyStory_badRequest_validation() throws Exception {
        Long storySeq = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "edit.png", "image/png", "dummy".getBytes());
        // 제목을 빈 문자열로 보내서 스프링 유효성 검증(예: @NotBlank)이 걸리도록 함
        mockMvc.perform(multipart("/donationLetters/{storySeq}", storySeq)
                        .file(file)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .param("areaCode", "AREA100")
                        .param("storyTitle", "ㅇㅈㅂㅇㅂㅈㅇ")  // 빈 문자열 → 검증 실패
                        .param("storyWriter", "")
                        .param("storyContents", "수정내용")
                        .param("captchaToken", "token")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isBadRequest())
                // 전역 핸들러에서 BindException을 잡아 “donation.error.required.title” 메시지로 응답
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("작성자는 필수 입력값입니다."));
    }


    // -------------------------------------------------------
    // 8) DELETE /donationLetters/{storySeq} – 삭제
    // -------------------------------------------------------

    @Test
    @DisplayName("DELETE /donationLetters/{storySeq} - 성공")
    void deleteStory_success() throws Exception {
        Long storySeq = 1L;
        VerifyStoryPasscodeDto reqDto = new VerifyStoryPasscodeDto("correctPwd");

        doNothing().when(donationService).deleteDonationStory(eq(storySeq), any(VerifyStoryPasscodeDto.class));
        given(messageSourceAccessor.getMessage("donation.delete.success"))
                .willReturn("스토리가 정상적으로 삭제 되었습니다.");

        mockMvc.perform(delete("/donationLetters/{storySeq}", storySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("스토리가 정상적으로 삭제 되었습니다."));
    }

    @Test
    @DisplayName("DELETE /donationLetters/{storySeq} - 실패 (비밀번호 불일치 → 400)")
    void deleteStory_passwordMismatch() throws Exception {
        Long storySeq = 1L;
        VerifyStoryPasscodeDto reqDto = new VerifyStoryPasscodeDto("wrongPwd");

        doThrow(new IllegalArgumentException("donation.error.delete.password_mismatch"))
                .when(donationService).deleteDonationStory(eq(storySeq), any(VerifyStoryPasscodeDto.class));
        given(messageSourceAccessor.getMessage("donation.error.delete.password_mismatch"))
                .willReturn("비밀번호가 일치하지 않습니다.");

        mockMvc.perform(delete("/donationLetters/{storySeq}", storySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("DELETE /donationLetters/{storySeq} - 실패 (서버 예외 → 500)")
    void deleteStory_internalError() throws Exception {
        Long storySeq = 1L;
        VerifyStoryPasscodeDto reqDto = new VerifyStoryPasscodeDto("asdnsadiqwd");


        given(messageSourceAccessor.getMessage("donation.delete.success"))
                .willReturn("스토리가 성공적으로 삭제되었습니다.");

        doThrow(new RuntimeException("DB 오류"))
                .when(donationService).deleteDonationStory(eq(storySeq), any());

        System.out.println("exception mocking 설정 완료");

        given(messageSourceAccessor.getMessage("error.internal"))
                .willReturn("서버 내부 오류가 발생했습니다.");

        System.out.println("메시지 mocking 설정 완료");

        mockMvc.perform(delete("/donationLetters/{storySeq}", storySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }


    // -------------------------------------------------------
    // 9) 댓글 등록/수정/삭제 – 대표적인 케이스만 간략하게
    // -------------------------------------------------------

    @Test
    @DisplayName("POST /donationLetters/{storySeq}/comments - 성공")
    void createComment_success() throws Exception {
        Long storySeq = 1L;
        DonationCommentCreateRequestDto reqDto = DonationCommentCreateRequestDto.builder()
                .commentWriter("댓글작성자")
                .commentPasscode("abcd1234")
                .contents("댓글내용")
                .captchaToken("token")
                .build();

        doNothing().when(donationCommentService)
                .createDonationStoryComment(eq(storySeq), any(DonationCommentCreateRequestDto.class));
        given(messageSourceAccessor.getMessage("donation.comment.create.success"))
                .willReturn("편지 댓글이 성공적으로 등록되었습니다.");

        mockMvc.perform(post("/donationLetters/{storySeq}/comments", storySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("편지 댓글이 성공적으로 등록되었습니다."));
    }

    @Test
    @DisplayName("POST /donationLetters/{storySeq}/comments - 실패 (필수 입력 누락 → 400)")
    void createComment_badRequest_validation() throws Exception {
        Long storySeq = 1L;
        DonationCommentCreateRequestDto reqDto = DonationCommentCreateRequestDto.builder()
                .commentWriter("")            // 빈 작성자 → 검증 실패
                .commentPasscode("abcd1234")
                .contents("댓글내용")
                .captchaToken("token")
                .build();
        given(messageSourceAccessor.getMessage(eq("donation.error.required.writer")))
                .willReturn("작성자는 필수 입력값입니다.");

        // 스프링이 자동으로 MethodArgumentNotValidException을 발생시킴
        mockMvc.perform(post("/donationLetters/{storySeq}/comments", storySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("작성자는 필수 입력값입니다."));
    }

    @Test
    @DisplayName("POST /donationLetters/{storySeq}/comments - 실패 (서비스 예외 → 500)")
    void createComment_internalError() throws Exception {
        Long storySeq = 1L;
        DonationCommentCreateRequestDto reqDto = DonationCommentCreateRequestDto.builder()
                .commentWriter("댓글작성자")
                .commentPasscode("abcd1234")
                .contents("댓글내용")
                .captchaToken("token")
                .build();

        doThrow(new RuntimeException("예상치 못한 에러"))
                .when(donationCommentService)
                .createDonationStoryComment(eq(storySeq), any(DonationCommentCreateRequestDto.class));
        given(messageSourceAccessor.getMessage("error.internal"))
                .willReturn("서버 내부 오류가 발생했습니다.");

        mockMvc.perform(post("/donationLetters/{storySeq}/comments", storySeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }


    @Test
    @DisplayName("PATCH /donationLetters/{storySeq}/comments/{commentSeq} - 성공")
    void modifyComment_success() throws Exception {
        Long storySeq = 1L;
        Long commentSeq = 2L;
        DonationStoryCommentModifyRequestDto reqDto = DonationStoryCommentModifyRequestDto.builder()
                .commentWriter("수정작성자")
                .commentPasscode("abcd1234")
                .commentContents("수정댓글")
                .captchaToken("token")
                .build();

        doNothing().when(donationCommentService)
                .modifyDonationComment(eq(commentSeq), any(DonationStoryCommentModifyRequestDto.class));
        given(messageSourceAccessor.getMessage("donation.comment.update.success"))
                .willReturn("스토리 댓글이 성공적으로 수정되었습니다.");

        mockMvc.perform(patch("/donationLetters/{storySeq}/comments/{commentSeq}", storySeq, commentSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("스토리 댓글이 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("PATCH /donationLetters/{storySeq}/comments/{commentSeq} - 실패 (비밀번호 불일치 → 400)")
    void modifyComment_passwordMismatch() throws Exception {
        Long storySeq = 1L;
        Long commentSeq = 2L;
        DonationStoryCommentModifyRequestDto reqDto = DonationStoryCommentModifyRequestDto.builder()
                .commentWriter("수정작성자")
                .commentPasscode("wrongpass")
                .commentContents("수정댓글")
                .captchaToken("token")
                .build();

        doThrow(new IllegalArgumentException("donation.error.passcode.mismatch"))
                .when(donationCommentService).modifyDonationComment(eq(commentSeq), any());
        given(messageSourceAccessor.getMessage("donation.error.passcode.mismatch"))
                .willReturn("비밀번호가 일치하지 않습니다.");

        mockMvc.perform(patch("/donationLetters/{storySeq}/comments/{commentSeq}", storySeq, commentSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }


    @Test
    @DisplayName("DELETE /donationLetters/{storySeq}/comments/{commentSeq} - 성공")
    void deleteComment_success() throws Exception {
        Long storySeq = 1L;
        Long commentSeq = 2L;
        VerifyCommentPasscodeDto reqDto = VerifyCommentPasscodeDto.builder()
                .commentPasscode("abcd1234")
                .build();

        doNothing().when(donationCommentService)
                .deleteDonationComment(eq(commentSeq), any(VerifyCommentPasscodeDto.class));
        given(messageSourceAccessor.getMessage("donation.comment.delete.success"))
                .willReturn("스토리 댓글이 성공적으로 삭제되었습니다.");

        mockMvc.perform(delete("/donationLetters/{storySeq}/comments/{commentSeq}", storySeq, commentSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("스토리 댓글이 성공적으로 삭제되었습니다."));
    }

    @Test
    @DisplayName("DELETE /donationLetters/{storySeq}/comments/{commentSeq} - 실패 (서버 예외 → 500)")
    void deleteComment_internalError() throws Exception {
        Long storySeq = 1L;
        Long commentSeq = 2L;
        VerifyCommentPasscodeDto reqDto = VerifyCommentPasscodeDto.builder()
                .commentPasscode("wrongpass")
                .build();

        doThrow(new RuntimeException("DB 오류"))
                .when(donationCommentService).deleteDonationComment(eq(commentSeq), any(VerifyCommentPasscodeDto.class));
        given(messageSourceAccessor.getMessage("error.internal"))
                .willReturn("서버 내부 오류가 발생했습니다.");
        given(messageSourceAccessor.getMessage(eq("error.internal"), anyString()))
                .willReturn("서버 내부 오류가 발생했습니다.");

        mockMvc.perform(delete("/donationLetters/{storySeq}/comments/{commentSeq}", storySeq, commentSeq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }
}