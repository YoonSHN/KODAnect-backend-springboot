package kodanect.domain.article.util;

import kodanect.domain.article.dto.BoardOption;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Component
public class StringToBoardOptionConverter implements Converter<String, BoardOption> {

    @Override
    public BoardOption convert(String source) {
        return BoardOption.fromParam(source);
    }
}
