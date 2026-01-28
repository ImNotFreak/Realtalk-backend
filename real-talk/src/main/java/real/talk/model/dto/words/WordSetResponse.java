package real.talk.model.dto.words;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordSetResponse {
    private UUID id;
    private String name;
    private Integer wordCount;
}
