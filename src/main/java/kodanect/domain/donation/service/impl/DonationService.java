package kodanect.domain.donation.service.impl;



import kodanect.domain.donation.dto.request.DonationStoryCreateRequestDto;
import kodanect.domain.donation.dto.request.DonationStoryModifyRequestDto;
import kodanect.domain.donation.dto.request.VerifyStoryPasscodeDto;
import kodanect.domain.donation.dto.response.AreaCode;
import kodanect.domain.donation.dto.response.DonationStoryDetailDto;
import kodanect.domain.donation.dto.response.DonationStoryListDto;
import kodanect.domain.donation.dto.response.DonationStoryWriteFormDto;
import kodanect.domain.donation.entity.entity.DonationStory;
import kodanect.domain.donation.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class DonationService {

    private final DonationRepository donationRepository;
    private final CaptchaService captchaService;

    /*
    기증 후 스토리 게시글 출력
     */
    @Transactional(readOnly = true)
    public Page<DonationStoryListDto>   findAllDonationStories(Pageable pageable) {
        return donationRepository.findAllDonationStories(pageable) //엔티티 -> DTO로 변환
                .map(DonationStoryListDto::fromEntity);
    }

    /*
    기증 후 스토리 글쓰기 폼 데이터 출력
     */
    public DonationStoryWriteFormDto loadDonationStoryFormData(){
        List<AreaCode> areas = List.of(AreaCode.AREA100, AreaCode.AREA200, AreaCode.AREA300);
        if(areas.isEmpty()){
            throw new RuntimeException("사용 가능한 지역 코드가 없습니다.");
        }
        return DonationStoryWriteFormDto.builder()
                .areaOptions(areas).build();
    }

    /*
    기증 후 스토리 글쓰기 등록
     */
    @Transactional
    public void createDonationStory(DonationStoryCreateRequestDto requestDto){
        String areaCode = requestDto.getAreaCode().toString();
        if(areaCode== null || areaCode.isBlank())
            throw new IllegalArgumentException("권역 선택은 필수입니다.");

        if(!(areaCode.equals("AREA100") || areaCode.equals("AREA200") || areaCode.equals("AREA300"))){
            throw new IllegalArgumentException("존재하지 않는 권역 코드입니다.");
        }
        if(requestDto.getStoryTitle() == null || requestDto.getStoryTitle().isBlank()){
            throw new IllegalArgumentException("제목은 필수 입력값입니다.");
        }
        if(requestDto.getStoryPasscode() == null || requestDto.getStoryPasscode().isBlank()){
            throw new IllegalArgumentException("비밀번호는 필수 입력값입니다.");
        }
        if(!validatePassword(requestDto.getStoryPasscode())){
            throw new RuntimeException("패스워드가 틀립니다.");
        }
        //!captchaService.verifyCaptcha(requestDto.getCaptchaToken())
        if (false) {
            throw new IllegalArgumentException("캡차 인증 실패");
        }
        //캡차 검증 코드 추가해야 함.
        String storedFileName = null;
        String originalFileName = null;

        MultipartFile file = requestDto.getFile();
        System.out.println("file :" + file.getContentType());

        if(file != null && !file.isEmpty()){
            String contentType= requestDto.getFile().getContentType();
            if(contentType == null || !contentType.startsWith("image")){
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
            }
            if(requestDto.getFile() != null && !requestDto.getFile().isEmpty()){
                try{
                    originalFileName = requestDto.getFile().getOriginalFilename();
                    storedFileName = UUID.randomUUID().toString().replace("-", "").toUpperCase();

                    Path savePath = Paths.get("target/test-uploads",storedFileName); //파일 주소 얻기(일단 임시 주소)
                    Files.copy(requestDto.getFile().getInputStream(), savePath); //파일 저장
                } catch (IOException e) {
                    throw new RuntimeException("파일 업로드 실패", e);
                }
            }
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
    /*
    기증 후 스토리 상세 조회
     */
    @Transactional
    public DonationStoryDetailDto findDonationStory(Long storySeq){
        DonationStory storyDetailStory = donationRepository.findWithCommentsById(storySeq)
                .orElseThrow( () -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        storyDetailStory.increaseReadCount(); //조회수 증가

        return DonationStoryDetailDto.fromEntity(storyDetailStory);

    }
    /*
    패스워드 인증 메서드
     */
    public void verifyPasswordWithPassword(Long storySeq, VerifyStoryPasscodeDto verifyPassword) {
        DonationStory story = donationRepository.findById(storySeq)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));

        if(!validatePassword(verifyPassword.getStoryPasscode())){
            throw new RuntimeException("비밀번호가 형식에 맞지 않습니다.");
        }
        if(!verifyPassword.getStoryPasscode().equals(story.getStoryPasscode())){
            throw new IllegalArgumentException("MISMATCH_PWD");
        }
    }
    @Transactional
    public void modifyDonationStory(Long storySeq, DonationStoryModifyRequestDto requestDto) {
        DonationStory donationStory = donationRepository.findById(storySeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        MultipartFile file = requestDto.getFile();
        String storedFileName = donationStory.getFileName();
        String originalFileName = donationStory.getOrgFileName();

        //!captchaService.verifyCaptcha(requestDto.getCaptchaToken())
        if (false) {
            throw new IllegalArgumentException("캡차 인증 실패");
        }
        // 새 파일이 들어왔고, 기존 파일과 다른 경우만 삭제 + 교체
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image")) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
            }

            boolean isSameFile = originalFileName != null && originalFileName.equals(file.getOriginalFilename());

            if (!isSameFile) {
                // 기존 파일 삭제
                if (storedFileName != null) {
                    Path oldPath = Paths.get("target/test-uploads", storedFileName);
                    try {
                        Files.deleteIfExists(oldPath);
                    } catch (IOException e) {
                        throw new RuntimeException("기존 이미지 삭제 실패", e);
                    }
                }

                // 새 파일 저장
                try {
                    originalFileName = file.getOriginalFilename();
                    storedFileName = UUID.randomUUID().toString().replace("-", "").toUpperCase();
                    Path savePath = Paths.get("target/test-uploads", storedFileName);
                    Files.copy(file.getInputStream(), savePath);
                } catch (IOException e) {
                    throw new RuntimeException("파일 업로드 실패", e);
                }
            }
        }

        // 엔티티 수정
        donationStory.modifyDonationStory(requestDto, storedFileName, originalFileName);
    }

    @Transactional
    public void deleteDonationStory(Long storySeq, VerifyStoryPasscodeDto storyPasscodeDto){

        DonationStory donationStory = donationRepository.findById(storySeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if(!storyPasscodeDto.getStoryPasscode().equals(donationStory.getStoryPasscode())){ //비밀번호 불일치시
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        donationRepository.delete(donationStory);
    }

    //비밀번호 검증 (영 + 숫자 + 8자 ~ 16자 코드) - story, comment 모두 처리
    public boolean validatePassword(String password){
        if(password.matches("^[a-zA-Z0-9]{8,16}$")){
            return true;
        }
        return false;
    }



}
