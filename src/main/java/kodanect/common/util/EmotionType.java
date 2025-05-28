package kodanect.common.util;

import kodanect.domain.remembrance.entity.Memorial;
import kodanect.domain.remembrance.exception.InvalidEmotionTypeException;

public enum EmotionType {
    FLOWER{
        @Override
        public void apply(Memorial memorial) {
            /* 헌화 카운트 수 업데이트 */
            memorial.setFlowerCount(memorial.getFlowerCount() + 1);
        }
    },
    LOVE{
        @Override
        public void apply(Memorial memorial) {
            /* 사랑해요 카운트 수 업데이트 */
            memorial.setLoveCount(memorial.getLoveCount() + 1);
        }
    },
    SEE{
        @Override
        public void apply(Memorial memorial) {
            /* 보고싶어요 카운트 수 업데이트 */
            memorial.setSeeCount(memorial.getSeeCount() + 1);
        }
    },
    MISS{
        @Override
        public void apply(Memorial memorial) {
            /* 그리워요 카운트 수 업데이트 */
            memorial.setMissCount(memorial.getMissCount() + 1);
        }
    },
    PROUD{
        @Override
        public void apply(Memorial memorial) {
            /* 자랑스러워요 카운트 수 업데이트 */
            memorial.setProudCount(memorial.getProudCount() + 1);
        }
    },
    HARD{
        @Override
        public void apply(Memorial memorial) {
            /* 힘들어요 카운트 수 업데이트 */
            memorial.setHardCount(memorial.getHardCount() + 1);
        }
    },
    SAD{
        @Override
        public void apply(Memorial memorial) {
            /* 슬퍼요 카운트 수 업데이트 */
            memorial.setSadCount(memorial.getSadCount() + 1);
        }
    };

    public abstract void apply(Memorial memorial);

    public static EmotionType from(String emotion) throws InvalidEmotionTypeException {
        /* 문자열 검증 flower, love, see, miss, proud, hard, sad */
        if(emotion == null || emotion.isEmpty()){
            throw new InvalidEmotionTypeException();
        }

        try {
            return EmotionType.valueOf(emotion.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            throw new InvalidEmotionTypeException("지원하지 않는 이모지 타입입니다.");
        }
    }
}

