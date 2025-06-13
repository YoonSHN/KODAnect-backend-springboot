package kodanect.domain.donation.service.impl;

import kodanect.common.response.CursorCommentPaginationResponse;
import kodanect.common.response.CursorPaginationResponse;
import kodanect.common.util.CursorFormatter;
import kodanect.common.util.MessageResolver;
import kodanect.domain.donation.dto.request.DonationStoryCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyStoryPasscodeDto;
import kodanect.domain.donation.dto.response.*;
import kodanect.domain.donation.entity.DonationStory;
import kodanect.domain.donation.exception.*;
import kodanect.domain.donation.repository.DonationCommentRepository;
import kodanect.domain.donation.repository.DonationRepository;
import kodanect.domain.donation.service.DonationCommentService;
import kodanect.domain.donation.service.DonationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationServiceImpl implements DonationService {

    /** Cursor 기반 기본 Size */
    private static final int DEFAULT_SIZE = 3;
    private static final String DONATION_ERROR_NOTFOUND = "donation.error.notfound";

    private final DonationRepository donationRepository;
    private final DonationCommentRepository commentRepository;
    private final MessageResolver messageResolver;
    private final DonationCommentService commentService;

    @Override
    public CursorPaginationResponse<DonationStoryListDto, Long> findStoriesWithCursor(Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);
        List<DonationStoryListDto> results = donationRepository.findByCursor(cursor, pageable);

        long totalCount = donationRepository.countAll();
        return CursorFormatter.cursorFormat(results, size, totalCount); // 이 한 줄이면 충분
    }

    @Override
    public CursorPaginationResponse<DonationStoryListDto, Long> findSearchStoriesWithCursor(String type, String keyword, Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1); // size+1개 조회해서 hasNext 판단

        List<DonationStoryListDto> results;
        long totalCount = 0;

        if ("title".equalsIgnoreCase(type)) {
            results = donationRepository.findByTitleCursor(keyword, cursor, pageable);
            totalCount = donationRepository.countByTitle(keyword);
        } else if ("contents".equalsIgnoreCase(type)) {
            results = donationRepository.findByContentsCursor(keyword, cursor, pageable);
            totalCount = donationRepository.countByContents(keyword);
        } else {
            results = donationRepository.findByTitleOrContentsCursor(keyword, cursor, pageable);
            totalCount = donationRepository.countByTitleAndContents(keyword);
        }

        return CursorFormatter.cursorFormat(results, size, totalCount);
    }

    /** 스토리 작성 폼 데이터 반환 */
    public DonationStoryWriteFormDto loadDonationStoryFormData() {
        List<AreaCode> areas = List.of(AreaCode.AREA100, AreaCode.AREA200, AreaCode.AREA300);
        if (areas.isEmpty()) {
            throw new NotFoundAreaCode(messageResolver.get("donation.error.area.unavailable"));
        }
        return DonationStoryWriteFormDto.builder().areaOptions(areas).build();
    }

    /** 스토리 등록 처리 */
    public void createDonationStory(DonationStoryCreateRequestDto requestDto) {
        validateStoryRequest(requestDto.getAreaCode(), requestDto.getStoryTitle(), requestDto.getStoryPasscode());

        // 이미지가 여러개 저장될 수 도 있음.

        String [] imgNames = imgParsing(requestDto.getStoryContents());
        DonationStory story = DonationStory.builder()
                .areaCode(requestDto.getAreaCode())
                .storyTitle(requestDto.getStoryTitle())
                .storyPasscode(requestDto.getStoryPasscode())
                .storyWriter(requestDto.getStoryWriter())
                .readCount(0)
                .storyContents(requestDto.getStoryContents())
                .orgFileName(imgNames[0])
                .fileName(imgNames[1])
                .build();

        donationRepository.save(story);
    }

    private String[] imgParsing(String storyContents) {
        List<String> orgFileNames = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();

        log.info("==== storyContents ====");
        log.info(storyContents);

        Document doc = Jsoup.parse(storyContents);
        Elements imgTags = doc.select("img");

        log.info("총 이미지 수: " + imgTags.size());

        for (Element img : imgTags) {
            String src = img.attr("src");  // 또는 "data-cke-saved-src"


            if (src == null || !src.contains("/")) {
                continue;
            }

            // 원본 파일명 추출
            String orgFileName = src.substring(src.lastIndexOf("/") + 1);

            // 파일명 저장
            orgFileNames.add(orgFileName);
            fileNames.add(makeStoredFileName()); // UUID 같은 방식
        }

        return new String[]{
                String.join(",", orgFileNames),
                String.join(",", fileNames)
        };
    }

    @Override
    public DonationStoryDetailDto findDonationStoryWithStoryId(Long storySeq) {
        // 1) 스토리 로드 + 조회수 증가
        DonationStory story = donationRepository.findStoryOnlyById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(DONATION_ERROR_NOTFOUND));
        story.increaseReadCount();

        // 2) 최신 댓글 3개 조회
        var pageable = PageRequest.of(0, DEFAULT_SIZE + 1);  // +1로 hasNext 체크
        List<DonationStoryCommentDto> latest = commentRepository.findLatestComments(storySeq, pageable);

        // 3) 커서 포맷팅
        long total = commentRepository.countAllByStorySeq(storySeq);
        CursorCommentPaginationResponse<DonationStoryCommentDto, Long> commentsPage =
                CursorFormatter.cursorCommentCountFormat(latest, DEFAULT_SIZE, total);

        // 4) DTO 조립
        DonationStoryDetailDto dto = DonationStoryDetailDto.fromEntity(story);
        dto.setComments(commentsPage);

        return dto;
    }


    /** 비밀번호 검증 */
    public void verifyPasswordWithPassword(Long storySeq, VerifyStoryPasscodeDto verifyPassword) {
        DonationStory story = donationRepository.findById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(messageResolver.get(DONATION_ERROR_NOTFOUND)));

        if (!validatePassword(verifyPassword.getStoryPasscode())) {
            throw new BadRequestException(messageResolver.get("donation.error.invalid.passcode.format"));
        }
        if (!verifyPassword.getStoryPasscode().equals(story.getStoryPasscode())) {
            throw new BadRequestException(messageResolver.get("donation.error.delete.password_mismatch"));
        }
    }
    /** 스토리 수정 */
    public void     updateDonationStory(Long storySeq, DonationStoryModifyRequestDto requestDto) {
        log.info("===== updateDonationStory 호출됨 =====");

        DonationStory story = donationRepository.findStoryOnlyById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(messageResolver.get(DONATION_ERROR_NOTFOUND)));

        String [] imgNames = imgParsing(requestDto.getStoryContents());

        story.modifyDonationStory(requestDto, imgNames[1], imgNames[0]);
    }

    /** 스토리 삭제 */
    public void deleteDonationStory(Long storySeq, VerifyStoryPasscodeDto storyPasscodeDto) {
        DonationStory story = donationRepository.findStoryOnlyById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(messageResolver.get(DONATION_ERROR_NOTFOUND)));

        if (!storyPasscodeDto.getStoryPasscode().equals(story.getStoryPasscode())) {
            throw new PasscodeMismatchException(messageResolver.get("donation.error.delete.password_mismatch"));
        }
        donationRepository.delete(story);
    }

    /** 비밀번호 유효성 검증 */
    public boolean validatePassword(String password) {
        return password != null && password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,16}$");
    }

    /** 요청 필드 유효성 검증 */
    private void validateStoryRequest(AreaCode areaCode, String title, String password) {
        if (areaCode == null) {
            throw new BadRequestException(messageResolver.get("donation.error.required.area"));
        }
        if (title == null || title.isBlank()) {
            throw new BadRequestException(messageResolver.get("donation.error.required.title"));
        }
        if (password == null || password.isBlank()) {
            throw new BadRequestException(messageResolver.get("donation.error.required.passcode"));
        }
        if (!validatePassword(password)) {
            throw new BadRequestException(messageResolver.get("donation.error.invalid.passcode.format"));
        }
    }

    /** fileName (db 에 저장하는 형식으로 바꾸기)
     *  -(하이픈) 제거
     */
    public String makeStoredFileName(){
        return UUID.randomUUID().toString().replace("-","").toUpperCase();
    }



}
