package kodanect.common.util;

import kodanect.domain.remembrance.exception.InvalidEmotionTypeException;
import kodanect.domain.remembrance.repository.MemorialRepository;

public enum EmotionType {
    FLOWER{
        @Override
        public void apply(MemorialRepository repository, Integer donateSeq) {
            /* 헌화 카운트 수 업데이트 */
            repository.incrementFlower(donateSeq);
        }
    },
    LOVE{
        @Override
        public void apply(MemorialRepository repository, Integer donateSeq) {
            /* 사랑해요 카운트 수 업데이트 */
            repository.incrementLove(donateSeq);
        }
    },
    SEE{
        @Override
        public void apply(MemorialRepository repository, Integer donateSeq) {
            /* 보고싶어요 카운트 수 업데이트 */
            repository.incrementSee(donateSeq);
        }
    },
    MISS{
        @Override
        public void apply(MemorialRepository repository, Integer donateSeq) {
            /* 그리워요 카운트 수 업데이트 */
            repository.incrementMiss(donateSeq);
        }
    },
    PROUD{
        @Override
        public void apply(MemorialRepository repository, Integer donateSeq) {
            /* 자랑스러워요 카운트 수 업데이트 */
            repository.incrementProud(donateSeq);
        }
    },
    HARD{
        @Override
        public void apply(MemorialRepository repository, Integer donateSeq) {
            /* 힘들어요 카운트 수 업데이트 */
            repository.incrementHard(donateSeq);
        }
    },
    SAD{
        @Override
        public void apply(MemorialRepository repository, Integer donateSeq) {
            /* 슬퍼요 카운트 수 업데이트 */
            repository.incrementSad(donateSeq);
        }
    };

    public abstract void apply(MemorialRepository repository, Integer donateSeq);

    public static EmotionType from(String emotion) throws InvalidEmotionTypeException {
        /* 문자열 검증 flower, love, see, miss, proud, hard, sad */
        if(emotion == null || emotion.isEmpty()){
            throw new InvalidEmotionTypeException(emotion);
        }

        try {
            return EmotionType.valueOf(emotion.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            throw new InvalidEmotionTypeException("지원하지 않는 이모지 타입입니다.");
        }
    }
}

