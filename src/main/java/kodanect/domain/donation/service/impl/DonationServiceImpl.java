package kodanect.domain.donation.service.impl;

import kodanect.common.exception.config.SecureLogger;
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
import kodanect.domain.donation.service.DonationService;
import lombok.RequiredArgsConstructor;
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
public class DonationServiceImpl implements DonationService {

    // 로거 선언
    private static final SecureLogger logger = SecureLogger.getLogger(DonationServiceImpl.class);

    /** Cursor 기반 기본 Size */
    private static final int DEFAULT_SIZE = 3;
    private static final String DONATION_ERROR_NOTFOUND = "donation.error.notfound";

    private final DonationRepository donationRepository;
    private final DonationCommentRepository commentRepository;
    private final MessageResolver messageResolver;

    /* 스토리 목록 조회 */
    @Override
    public CursorPaginationResponse<DonationStoryListDto, Long> findStoriesWithCursor(Long cursor, int size) {
        logger.debug(">>> findStoriesWithCursor() 호출");

        Pageable pageable = PageRequest.of(0, size + 1);
        List<DonationStoryListDto> results = donationRepository.findByCursor(cursor, pageable);
        logger.debug("스토리 목록 조회 결과 수: {}", results.size());

        long totalCount = donationRepository.countAll();
        logger.debug("게시글 총 개수 : {}", totalCount);
        return CursorFormatter.cursorFormat(results, size, totalCount); // 이 한 줄이면 충분
    }

    /* 스토리 검색 */
    @Override
    public CursorPaginationResponse<DonationStoryListDto, Long> findSearchStoriesWithCursor(String type, String keyword, Long cursor, int size) {
        logger.debug(">>> findSearchStoriesWithCursor() 호출");
        Pageable pageable = PageRequest.of(0, size + 1); // size+1개 조회해서 hasNext 판단

        List<DonationStoryListDto> results;
        long totalCount = 0;

        if ("TITLE".equalsIgnoreCase(type)) {
            results = donationRepository.findByTitleCursor(keyword, cursor, pageable);
            totalCount = donationRepository.countByTitle(keyword);
        } else if ("CONTENTS".equalsIgnoreCase(type)) {
            results = donationRepository.findByContentsCursor(keyword, cursor, pageable);
            totalCount = donationRepository.countByContents(keyword);
        } else {
            results = donationRepository.findByTitleOrContentsCursor(keyword, cursor, pageable);
            totalCount = donationRepository.countByTitleAndContents(keyword);
        }
        logger.debug("스토리 목록 조회 결과 수: {}", results.size());

        return CursorFormatter.cursorFormat(results, size, totalCount);
    }



    /** 스토리 등록 처리 */
    public void createDonationStory(DonationStoryCreateRequestDto requestDto) {
        logger.debug(">>> createDonationStory() 호출");

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

        if (storyContents == null || storyContents.isBlank()) { //null 체크
            return new String[]{"", ""};
        }

        Document doc = Jsoup.parse(storyContents);
        Elements imgTags = doc.select("img");


        for (Element img : imgTags) {
            String src = img.attr("src");  // 또는 "data-cke-saved-src"


            if (src == null || !src.contains("/")) {
                continue;
            }

            // 원본 파일명 추출 ( \\\ 제거)
            String orgFileName = src.substring(src.lastIndexOf("/") + 1).replace("\"","");
            String fileExt = src.substring(src.lastIndexOf(".") + 1).replace("\"", "");

            // 파일명 저장
            orgFileNames.add(orgFileName);
            fileNames.add(makeStoredFileName() + "." + fileExt); // UUID + ".jpg(확장자)"
        }

        return new String[]{
                String.join(",", orgFileNames),
                String.join(",", fileNames)
        };
    }

    /* 스토리 상세 조회 */
    @Override
    public DonationStoryDetailDto findDonationStoryWithStoryId(Long storySeq) {
        logger.debug(">>> findDonationStoryWithStoryId() 호출");
        // 1) 스토리 로드 + 조회수 증가
        DonationStory story = donationRepository.findStoryOnlyById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(DONATION_ERROR_NOTFOUND));
        story.increaseReadCount();

        // 2) 최신 댓글 3개 조회
        var pageable = PageRequest.of(0, DEFAULT_SIZE + 1);  // +1로 hasNext 체크
        List<DonationStoryCommentDto> latest = commentRepository.findLatestComments(storySeq, pageable);

        logger.debug("조회된 댓글 수 : {}", latest.size());
        // 3) 커서 포맷팅
        long total = commentRepository.countAllByStorySeq(storySeq);
        CursorCommentPaginationResponse<DonationStoryCommentDto, Long> commentsPage =
                CursorFormatter.cursorCommentCountFormat(latest, DEFAULT_SIZE, total);

        // 4) DTO 조립
        DonationStoryDetailDto dto = DonationStoryDetailDto.fromEntity(story);

        dto.setComments(commentsPage);
        dto.setImageUrl(getWholeImageUrl(story.getStoryContents())); // imageUrl

        return dto;
    }

    private String getWholeImageUrl(String contents){
        List<String> fileUrls = new ArrayList<>();
        if (contents == null || contents.isBlank()) { //null 체크
            return "";
        }
        Document doc = Jsoup.parse(contents);
        Elements imgTags = doc.select("img");

        for(Element img : imgTags){
            String src = img.attr("src");

            if (src == null || !src.contains("/")) {
                continue;
            }

            src = src.replace("\"", "");
            fileUrls.add(src);
        }
        return String.join(",", fileUrls);
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
    public void  updateDonationStory(Long storySeq, DonationStoryModifyRequestDto requestDto) {
        logger.debug(">>> updateDonationStory() 호출");

        DonationStory story = donationRepository.findStoryOnlyById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(messageResolver.get(DONATION_ERROR_NOTFOUND)));

        String [] imgNames = imgParsing(requestDto.getStoryContents());

        story.modifyDonationStory(requestDto, imgNames[1], imgNames[0]);
    }

    /** 스토리 삭제 */
    public void deleteDonationStory(Long storySeq, VerifyStoryPasscodeDto storyPasscodeDto) {
        logger.debug(">>> deleteDonationStory() 호출");

        DonationStory story = donationRepository.findStoryOnlyById(storySeq)
                .orElseThrow(() -> new DonationNotFoundException(messageResolver.get(DONATION_ERROR_NOTFOUND)));

        if (storyPasscodeDto.getStoryPasscode() == null || storyPasscodeDto.getStoryPasscode().isBlank()) {
            throw new PasscodeMismatchException(messageResolver.get("donation.error.required.passcode"));
        }
        if (!storyPasscodeDto.getStoryPasscode().equals(story.getStoryPasscode())) {
            throw new PasscodeMismatchException(messageResolver.get("donation.error.delete.password_mismatch"));
        }

        story.softDeleteStoryAndComments();
        donationRepository.save(story);
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
        if (!(areaCode == AreaCode.AREA100 || areaCode == AreaCode.AREA200 || areaCode == AreaCode.AREA300)) {
            throw new BadRequestException(messageResolver.get("donation.error.invalid.area"));
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
