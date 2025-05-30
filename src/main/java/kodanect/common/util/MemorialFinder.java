package kodanect.common.util;

import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.exception.MemorialNotFoundException;
import kodanect.domain.remembrance.repository.MemorialRepository;
import org.springframework.stereotype.Component;

@Component
public class MemorialFinder {

    private final MemorialRepository memorialRepository;

    public MemorialFinder(MemorialRepository memorialRepository){
        this.memorialRepository = memorialRepository;
    }

    public Memorial findByIdOrThrow(Integer donateSeq) throws MemorialNotFoundException {
        /* 게시글 조회 */
        return memorialRepository.findById(donateSeq).orElseThrow(MemorialNotFoundException::new);
    }
}