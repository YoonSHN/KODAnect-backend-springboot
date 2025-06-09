package kodanect.common.util;

import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.exception.MemorialNotFoundException;
import kodanect.domain.remembrance.repository.MemorialRepository;
import org.springframework.stereotype.Component;

/**
 *
 * 게시글 존재 유무 확인 및 Memorial 반환 클래스
 *
 * <p>사용법: MemorialFinder.findByIdOrThrow(Integer donateSeq)</p>
 *
 **/
@Component
public class MemorialFinder {

    private final MemorialRepository memorialRepository;

    public MemorialFinder(MemorialRepository memorialRepository){
        this.memorialRepository = memorialRepository;
    }

    /**
     *
     * 게시글 존재 유무 확인 및 Memorial 반환 메서드
     *
     * @param donateSeq 상세 게시글 번호
     * @return 상세 게시글 번호에 매칭되는 게시글(Memorial)
     * */
    public Memorial findByIdOrThrow(Integer donateSeq) throws MemorialNotFoundException {

        return memorialRepository.findById(donateSeq).orElseThrow(() -> new MemorialNotFoundException(donateSeq));
    }
}