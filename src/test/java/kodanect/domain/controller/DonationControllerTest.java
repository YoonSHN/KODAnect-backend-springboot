package kodanect.domain.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import kodanect.domain.donation.controller.DonationController;
import kodanect.domain.donation.dto.request.*;
import kodanect.domain.donation.dto.response.AreaCode;
import kodanect.domain.donation.dto.response.DonationStoryDetailDto;
import kodanect.domain.donation.dto.response.DonationStoryListDto;
import kodanect.domain.donation.dto.response.DonationStoryWriteFormDto;
import kodanect.domain.donation.service.impl.DonationCommentService;
import kodanect.domain.donation.service.impl.DonationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DonationController.class)
@ContextConfiguration(classes = KodaApplication.class)
public class DonationControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean
    DonationService donationService;
    @MockBean
    DonationCommentService donationCommentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("스토리 전체 조회 - 성공")
    public void getAllDonationList_success() throws Exception { //donationLetters 에 접속시 200 오류 500이 잘 나오는지 + service호출시 DonationStoryListDto가 잘 반환되는지
        //given
        DonationStoryListDto dto = new DonationStoryListDto(
                1L, "제목1", "글쓴이1", 0, LocalDateTime.now()
        );
        List<DonationStoryListDto> content = List.of(dto);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "storySeq"));
        Page<DonationStoryListDto> page = new PageImpl<>(content, pageable, 1);

        given(donationService.findAllDonationStories(any(Pageable.class))).willReturn(page);

        //when & then
        mockMvc.perform(get("/donationLetters").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("기증 후 스토리 목록 가져오기 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].storySeq").value(1))
                .andExpect(jsonPath("$.data[0].storyTitle").value("제목1"))
                .andExpect(jsonPath("$.data[0].storyWriter").value("글쓴이1"))
                .andExpect(jsonPath("$.data[0].readCount").value(0))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andExpect(jsonPath("$.pageInfo.currentPage").value(0))
                .andExpect(jsonPath("$.pageInfo.isFirst").value(true))
                .andExpect(jsonPath("$.pageInfo.hasNext").value(false));
    }
    @Test
    @DisplayName("스토리 전체 조회 - 실패")
    public void getAllDonationList_failure() throws Exception{
        DonationStoryListDto dto1 = new DonationStoryListDto(1L, "제목1", "글쓴이1", 0, LocalDateTime.now());
        List<DonationStoryListDto> listDto = List.of(dto1);
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "storySeq"));

        given(donationService.findAllDonationStories(pageable)).willThrow(new RuntimeException("기증 스토리 조회 실패"));

        mockMvc.perform(get("/donationLetters")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("기증 후 스토리 목록 가져오기 실패"));

        System.out.println("donationSerivce = " + donationService);
    }

    @Test
    @DisplayName("스토리 작성 폼 출력 - 성공")
    public void getDonationWriteForm() throws Exception {
        DonationStoryWriteFormDto writeFormDto = new DonationStoryWriteFormDto(List.of(AreaCode.AREA100, AreaCode.AREA200));

        given(donationService.loadDonationStoryFormData()).willReturn(writeFormDto);

        mockMvc.perform(get("/donationLetters/new")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("스토리 등록 - 성공")
    public void createStory_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "hello world".getBytes()
        );
        DonationStoryCreateRequestDto requestDto = new DonationStoryCreateRequestDto(
                AreaCode.AREA100,"제목1","pdddceqceqd","작가1",
                "안녕하세요","ㅇwqkdoqwkdowqkdq",file);

        doNothing().when(donationService).createDonationStory(any(DonationStoryCreateRequestDto.class));
        mockMvc.perform(multipart("/donationLetters")
                .file(file)
                .param("areaCode", requestDto.getAreaCode().name())
                .param("storyTitle", requestDto.getStoryTitle())
                        .param("storyPasscode", requestDto.getStoryPasscode())
                        .param("storyContents", requestDto.getStoryContents())
                        .param("storyWriter", requestDto.getStoryWriter())
                        .param("captchaToken", requestDto.getCaptchaToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("스토리가 성공적으로 등록되었습니다."));
    }
    @Test
    @DisplayName("스토리 등록 - 실패")
    public void createStory_failure() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "hello world".getBytes()
        );
        DonationStoryCreateRequestDto requestDto = new DonationStoryCreateRequestDto(
                AreaCode.AREA100,"dqwdwqkodqdq","ddddqwdqdd","작가1",
                "안녕하세요","ㅇwqkdoqwkdowqkdq",file);

        doThrow(new IllegalArgumentException("예외 발생")).when(donationService).createDonationStory(any(DonationStoryCreateRequestDto.class));
        mockMvc.perform(multipart("/donationLetters")
                        .file(file)
                        .param("areaCode", "AREA100")
                        .param("storyTitle", "제목1")
                        .param("storyPasscode", requestDto.getStoryPasscode())
                        .param("storyWriter", requestDto.getStoryWriter())
                        .param("storyContents", requestDto.getStoryContents())
                        .param("captchaToken", requestDto.getCaptchaToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("필수 입력값이 누락되었습니다."))
                        .andExpect(jsonPath("$.success").value(false));
    }
    @Test
    @DisplayName("상세 페이지 조회 - 성공(스토리 있음)")
    public void getDonationStoryDetail() throws Exception {
        // given
        Long storySeq = 1L;
        DonationStoryDetailDto listDto = DonationStoryDetailDto.builder()
                .storySeq(storySeq)
                .title("제목1")
                .storyWriter("작성자1")
                .readCount(0)
                .areaCode(AreaCode.AREA100)
                .storyContent("cdscdockdcpa")
                .fileName("343029490423890")
                .orgFileName("dice1.jpg")
                .build();

        // service가 해당 값을 반환하도록 mock 설정
        given(donationService.findDonationStory(storySeq)).willReturn(listDto);

        // when & then
        mockMvc.perform(get("/donationLetters/{storySeq}", storySeq)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("detail 페이지 출력"))
                .andExpect(jsonPath("$.data.storySeq").value(1))
                .andExpect(jsonPath("$.data.title").value("제목1"))
                .andExpect(jsonPath("$.data.storyWriter").value("작성자1"))
                .andExpect(jsonPath("$.data.readCount").value(0))
                .andExpect(jsonPath("$.data.areaCode").value("AREA100"))
                .andExpect(jsonPath("$.data.storyContent").value("cdscdockdcpa"))
                .andExpect(jsonPath("$.data.fileName").value("343029490423890"))
                .andExpect(jsonPath("$.data.orgFileName").value("dice1.jpg"));
    }
    @Test
    @DisplayName("상세 페이지 조회 - 실패(스토리 없음 - 400 에러)")
    public void getDonationStoryDetail_fail_notFound() throws Exception {
        // given
        Long storySeq = 999L;

        // service가 예외 던지도록 설정
        given(donationService.findDonationStory(storySeq))
                .willThrow(new IllegalArgumentException("NOT_FOUND"));

        // when & then
        mockMvc.perform(get("/donationLetters/{storySeq}", storySeq)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // 404 응답 검증
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("해당 스토리를 찾을 수 없습니다."));
    }
    @Test
    @DisplayName("상세 페이지 조회 - 실패 (서버 내부 오류 - 500 에러)")
    public void getDonationStoryDetail_fail_internalServerError() throws Exception {
        // given
        Long storySeq = 999L;

        // 서비스에서 예기치 않은 예외 발생
        given(donationService.findDonationStory(storySeq))
                .willThrow(new RuntimeException("DB 연결 실패"));

        // when & then
        mockMvc.perform(get("/donationLetters/{storySeq}", storySeq)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.error").value("DB 연결 실패"));
    }
    @Test
    @DisplayName("스토리 비밀번호 검증 - 성공")
    public void verifyStoryPassword_success() throws Exception {
        Long storySeq = 1L;
        VerifyStoryPasscodeDto dto = new VerifyStoryPasscodeDto("correctPwd");

        doNothing().when(donationService).verifyPasswordWithPassword(eq(storySeq), any(VerifyStoryPasscodeDto.class));

        mockMvc.perform(post("/donationLetters/{storySeq}/verifyPwd", storySeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value(1))
            .andExpect(jsonPath("$.message").value("비밀번호가 일치합니다."));
    }

    @Test
    @DisplayName("스토리 비밀번호 검증 - 실패 (NOT_FOUND)")
    public void verifyStoryPassword_fail_notFound() throws Exception {
        Long storySeq = 999L;
        VerifyStoryPasscodeDto dto = new VerifyStoryPasscodeDto("anyPwd");

        doThrow(new IllegalArgumentException("NOT_FOUND"))
            .when(donationService).verifyPasswordWithPassword(eq(storySeq), any(VerifyStoryPasscodeDto.class));

        mockMvc.perform(post("/donationLetters/{storySeq}/verifyPwd", storySeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.result").value(0))
            .andExpect(jsonPath("$.message").value("해당 게시글이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("스토리 비밀번호 검증 - 실패 (MISMATCH_PWD)")
    public void verifyStoryPassword_fail_mismatch() throws Exception {
        Long storySeq = 1L;
        VerifyStoryPasscodeDto dto = new VerifyStoryPasscodeDto("wrongPwd");

        doThrow(new IllegalArgumentException("MISMATCH_PWD"))
            .when(donationService).verifyPasswordWithPassword(eq(storySeq), any(VerifyStoryPasscodeDto.class));

        mockMvc.perform(post("/donationLetters/{storySeq}/verifyPwd", storySeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.result").value(0))
            .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("스토리 수정 - 성공")
    public void modifyStory_success() throws Exception {
        Long storySeq = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "edit.png", "image/png", "bytes".getBytes());
        DonationStoryModifyRequestDto req = DonationStoryModifyRequestDto.builder()
                .areaCode(AreaCode.AREA100)
                .storyTitle("제목1")
                .storyWriter("작성자1")
                .storyContents("ㅇㅂ재ㅏㅇ베재")
                .file(file)
                .captchaToken("qwdkqopdq").build();

        doNothing().when(donationService).modifyDonationStory(eq(storySeq), any(DonationStoryModifyRequestDto.class));

        mockMvc.perform(multipart("/donationLetters/{storySeq}", storySeq)
                .file(file)
                .with(request -> { request.setMethod("PATCH"); return request; })
                .param("areaCode", req.getAreaCode().name())
                .param("storyTitle", req.getStoryTitle())
                .param("captchaToken", req.getCaptchaToken())
                .param("storyWriter", req.getStoryWriter())
                .param("storyContents", req.getStoryContents())
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(201))
            .andExpect(jsonPath("$.message").value("스토리가 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("스토리 수정 - 실패")
    public void modifyStory_failure() throws Exception {
        Long storySeq = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "edit.png", "image/png", "bytes".getBytes());
        DonationStoryModifyRequestDto req = DonationStoryModifyRequestDto.builder()
                        .areaCode(AreaCode.AREA100)
                        .storyTitle("제목1")
                        .storyWriter("작성자1")
                        .storyContents("ㅇㅂ재ㅏㅇ베재")
                        .file(file)
                        .captchaToken("qwdkqopdq").build();

        doThrow(new IllegalArgumentException("error"))
            .when(donationService).modifyDonationStory(eq(storySeq), any(DonationStoryModifyRequestDto.class));

        mockMvc.perform(multipart("/donationLetters/{storySeq}", storySeq)
                .file(file)
                .with(request -> { request.setMethod("PATCH"); return request; })
                .param("areaCode", req.getAreaCode().name())
                .param("storyTitle", req.getStoryTitle())
                .param("captchaToken", req.getCaptchaToken())
                        .param("storyWriter",req.getStoryWriter())
                        .param("storyContents", req.getStoryContents())
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("필수 입력값이 누락되었습니다."));
    }

    @Test
    @DisplayName("스토리 삭제 - 성공")
    public void deleteStory_success() throws Exception {
        Long storySeq = 1L;
        VerifyStoryPasscodeDto dto = new VerifyStoryPasscodeDto("pwd");

        doNothing().when(donationService).deleteDonationStory(eq(storySeq), any(VerifyStoryPasscodeDto.class));

        mockMvc.perform(delete("/donationLetters/{storySeq}", storySeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value(1))
            .andExpect(jsonPath("$.message").value("스토리가 정상적으로 삭제 되었습니다."));
    }

    @Test
    @DisplayName("스토리 삭제 - 실패")
    public void deleteStory_failure() throws Exception {
        Long storySeq = 1L;
        VerifyStoryPasscodeDto dto = new VerifyStoryPasscodeDto("wrong");

        doThrow(new RuntimeException("pwd mismatch"))
            .when(donationService).deleteDonationStory(eq(storySeq), any(VerifyStoryPasscodeDto.class));

        mockMvc.perform(delete("/donationLetters/{storySeq}", storySeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.result").value(0))
            .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("댓글 등록 - 성공")
    public void createComment_success() throws Exception {
        Long storySeq = 1L;
        DonationCommentCreateRequestDto dto = new DonationCommentCreateRequestDto("writer","pwd","contents","token");

        doNothing().when(donationCommentService).createDonationStoryComment(eq(storySeq), any(DonationCommentCreateRequestDto.class));

        mockMvc.perform(post("/donationLetters/{storySeq}/comments", storySeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("편지 댓글이 성공적으로 등록되었습니다."));
    }

    @Test
    @DisplayName("댓글 등록 - 실패")
    public void createComment_failure() throws Exception {
        Long storySeq = 1L;
        DonationCommentCreateRequestDto dto = new DonationCommentCreateRequestDto("writer","pwd","contents","token");

        doThrow(new IllegalArgumentException("error"))
            .when(donationCommentService).createDonationStoryComment(eq(storySeq), any(DonationCommentCreateRequestDto.class));

        mockMvc.perform(post("/donationLetters/{storySeq}/comments", storySeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("필수 입력값이 누락되었습니다."));
    }

    @Test
    @DisplayName("댓글 수정 - 성공")
    public void modifyComment_success() throws Exception {
        Long storySeq = 1L, commentSeq = 2L;
        DonationStoryCommentModifyRequestDto dto = new DonationStoryCommentModifyRequestDto("writer","newContents","pwd","token");

        doNothing().when(donationCommentService).modifyDonationComment(eq(commentSeq), any(DonationStoryCommentModifyRequestDto.class));

        mockMvc.perform(patch("/donationLetters/{storySeq}/comments/{commentSeq}", storySeq, commentSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(201))
            .andExpect(jsonPath("$.message").value("스토리 댓글이 성공적으로 수정되었습니다."));
    }

    @Test
    @DisplayName("댓글 수정 - 실패")
    public void modifyComment_failure() throws Exception {
        Long storySeq = 1L, commentSeq = 2L;
        DonationStoryCommentModifyRequestDto dto = new DonationStoryCommentModifyRequestDto("writer","newContents","pwd","token");

        doThrow(new IllegalArgumentException("error"))
            .when(donationCommentService).modifyDonationComment(eq(commentSeq), any(DonationStoryCommentModifyRequestDto.class));

        mockMvc.perform(patch("/donationLetters/{storySeq}/comments/{commentSeq}", storySeq, commentSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("필수 입력값이 누락되었습니다."));
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    public void deleteComment_success() throws Exception {
        Long storySeq = 1L, commentSeq = 2L;
        VerifyCommentPasscodeDto dto = new VerifyCommentPasscodeDto("pwd");

        doNothing().when(donationCommentService).deleteDonationComment(eq(commentSeq), any(VerifyCommentPasscodeDto.class));

        mockMvc.perform(delete("/donationLetters/{storySeq}/comments/{commentSeq}", storySeq, commentSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(201))
            .andExpect(jsonPath("$.message").value("스토리 댓글이 성공적으로 삭제되었습니다."));
    }

    @Test
    @DisplayName("댓글 삭제 - 실패")
    public void deleteComment_failure() throws Exception {
        Long storySeq = 1L, commentSeq = 2L;
        VerifyCommentPasscodeDto dto = new VerifyCommentPasscodeDto("pwd");

        doThrow(new IllegalArgumentException("error"))
            .when(donationCommentService).deleteDonationComment(eq(commentSeq), any(VerifyCommentPasscodeDto.class));

        mockMvc.perform(delete("/donationLetters/{storySeq}/comments/{commentSeq}", storySeq, commentSeq)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("error"));
    }
}