package kodanect.domain.remembrance.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemorialNextCursor {
    private Integer cursor;
    private String date;

    public void setDate(String date) {
        this.date = (date == null || date.isBlank()) ? null : date;
    }
}
