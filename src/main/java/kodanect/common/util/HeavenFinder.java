package kodanect.common.util;

import kodanect.domain.heaven.dto.HeavenDto;
import kodanect.domain.heaven.entity.Heaven;
import kodanect.domain.heaven.exception.HeavenNotFoundException;
import kodanect.domain.heaven.repository.HeavenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HeavenFinder {

    private final HeavenRepository heavenRepository;

    /* 편지 존재 여부 확인 */
    public Heaven findByIdOrThrow(Integer letterSeq) {
        return heavenRepository.findByIdAndDelFlag(letterSeq)
                .orElseThrow(() -> new HeavenNotFoundException(letterSeq));
    }

    public HeavenDto findAnonymizedByIdOrThrow(Integer letterSeq) {
        HeavenDto heavenDto = heavenRepository.findAnonymizedById(letterSeq);

        if (heavenDto == null) {
            throw new HeavenNotFoundException(letterSeq);
        }

        return heavenDto;
    }
}
