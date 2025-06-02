package kodanect.domain.donation.service.impl;

import kodanect.common.util.MessageResolver;
import kodanect.domain.donation.dto.OffsetBasedPageRequest;
import kodanect.domain.donation.dto.request.DonationStoryCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyStoryPasscodeDto;
import kodanect.domain.donation.dto.response.AreaCode;
import kodanect.domain.donation.dto.response.DonationStoryDetailDto;
import kodanect.domain.donation.dto.response.DonationStoryListDto;
import kodanect.domain.donation.dto.response.DonationStoryWriteFormDto;
import kodanect.domain.donation.entity.DonationStory;
import kodanect.domain.donation.exception.BadRequestException;
import kodanect.domain.donation.exception.NotFoundException;
import kodanect.domain.donation.repository.DonationRepository;
import kodanect.domain.donation.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final MessageResolver messageResolver;

    private final String uploadDir = "target/test-uploads";

    @Override
    @Transactional(readOnly = true)
    public Slice<DonationStoryListDto> findStoriesWithOffset(Pageable pageable) {
        // +1 해서 hasNext 판단을 위함
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        // 정렬도 유지 (한개 더 가져오기 - hasNext 반별을 위해)
        Pageable plusOnePageable = new OffsetBasedPageRequest(offset, limit + 1, pageable.getSort());
        //다음 데이터 담기
        List<DonationStoryListDto> result = donationRepository.findSliceDonationStoriesWithOffset(plusOnePageable);

        boolean hasNext = result.size() > limit;
        //limit만큼 보내야 하기에 1개 잘라냄
        List<DonationStoryListDto> content = hasNext ? result.subList(0, limit) : result;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    /** 검색 기능 (제목/내용/전체) */
    @Transactional(readOnly = true)
    public Slice<DonationStoryListDto> findDonationStorySearchResult(Pageable pageable, String type, String keyword) {
        return switch (type) {
            case "storyTitle" -> donationRepository.findByTitleContaining(pageable, keyword);
            case "storyContents" -> donationRepository.findByContentsContaining(pageable, keyword);
            case "All" -> donationRepository.findByTitleOrContentsContaining(pageable, keyword);
            default ->  new SliceImpl<>(List.of(), pageable, false);
        };
    }

    /** 스토리 작성 폼 데이터 반환 */
    public DonationStoryWriteFormDto loadDonationStoryFormData() {
        List<AreaCode> areas = List.of(AreaCode.AREA100, AreaCode.AREA200, AreaCode.AREA300);
        if (areas.isEmpty()) {
            throw new RuntimeException(messageResolver.get("donation.error.area.unavailable"));
        }
        return DonationStoryWriteFormDto.builder().areaOptions(areas).build();
    }

    /** 스토리 등록 처리 */
    @Transactional
    public void createDonationStory(DonationStoryCreateRequestDto requestDto) {
        validateStoryRequest(requestDto.getAreaCode(), requestDto.getStoryTitle(), requestDto.getStoryPasscode());

        String storedFileName = null;
        String originalFileName = null;

        if (requestDto.getFile() != null && !requestDto.getFile().isEmpty()) {
            String[] result = saveFileIfExists(requestDto.getFile());
            storedFileName = result[0];
            originalFileName = result[1];
        }

        DonationStory story = DonationStory.builder()
                .areaCode(requestDto.getAreaCode())
                .storyTitle(requestDto.getStoryTitle())
                .storyPasscode(requestDto.getStoryPasscode())
                .storyWriter(requestDto.getStoryWriter())
                .anonymityFlag(null)
                .readCount(0)
                .storyContents(requestDto.getStoryContents())
                .fileName(storedFileName)
                .orgFileName(originalFileName)
                .build();

        donationRepository.save(story);
    }

    /** 스토리 상세 조회 및 조회수 증가 */
    @Transactional
    public DonationStoryDetailDto findDonationStory(Long storySeq) {
        DonationStory story = findStoryOrThrow(storySeq);
        story.increaseReadCount();
        return DonationStoryDetailDto.fromEntity(story);
    }

    /** 비밀번호 검증 */
    public void verifyPasswordWithPassword(Long storySeq, VerifyStoryPasscodeDto verifyPassword) {
        DonationStory story = donationRepository.findById(storySeq)
                .orElseThrow(() -> new NotFoundException(messageResolver.get("donation.error.notfound")));

        if (!validatePassword(verifyPassword.getStoryPasscode())) {
            throw new BadRequestException(messageResolver.get("donation.error.invalid.passcode.format"));
        }
        if (!verifyPassword.getStoryPasscode().equals(story.getStoryPasscode())) {
            throw new BadRequestException(messageResolver.get("donation.error.delete.password_mismatch"));
        }
    }

    /** 스토리 수정 */
    @Transactional
    public void modifyDonationStory(Long storySeq, DonationStoryModifyRequestDto requestDto) {
        DonationStory story = findStoryOrThrow(storySeq);

        String storedFileName = story.getFileName();
        String originalFileName = story.getOrgFileName();

        if (requestDto.getFile() != null && !requestDto.getFile().isEmpty()) {
            String[] result = updateFileIfChanged(requestDto.getFile(), originalFileName, storedFileName);
            storedFileName = result[0];
            originalFileName = result[1];
        }

        story.modifyDonationStory(requestDto, storedFileName, originalFileName);
    }

    /** 스토리 삭제 */
    @Transactional
    public void deleteDonationStory(Long storySeq, VerifyStoryPasscodeDto storyPasscodeDto) {
        DonationStory story = findStoryOrThrow(storySeq);

        if (!storyPasscodeDto.getStoryPasscode().equals(story.getStoryPasscode())) {
            throw new BadRequestException(messageResolver.get("donation.error.delete.password_mismatch"));
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

    /** 파일 업로드 처리 */
    private String[] saveFileIfExists(MultipartFile file) {
        try {
            String storedFileName = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            String originalFileName = file.getOriginalFilename();
            Path savePath = Paths.get(uploadDir, storedFileName);
            Files.copy(file.getInputStream(), savePath);
            return new String[]{storedFileName, originalFileName};
        }
        catch (IOException e) {
            throw new RuntimeException(messageResolver.get("donation.error.file.upload.fail"), e);
        }
    }

    /** 기존 파일 변경 시 업데이트 */
    private String[] updateFileIfChanged(MultipartFile file, String oldOriginal, String oldStored) {
        String newOriginal = file.getOriginalFilename();
        if (oldOriginal != null && oldOriginal.equals(newOriginal)) {
            return new String[]{oldStored, oldOriginal};
        }
        try {
            if (oldStored != null) {
                Files.deleteIfExists(Paths.get(uploadDir, oldStored));
            }
            return saveFileIfExists(file);
        }
        catch (IOException e) {
            throw new RuntimeException(messageResolver.get("donation.error.file.delete.fail"), e);
        }
    }

    /** ID로 스토리 조회 또는 예외 발생 */
    private DonationStory findStoryOrThrow(Long storySeq) {
        return donationRepository.findWithCommentsById(storySeq)
                .orElseThrow(() -> new NotFoundException(messageResolver.get("donation.error.notfound")));
    }
}
