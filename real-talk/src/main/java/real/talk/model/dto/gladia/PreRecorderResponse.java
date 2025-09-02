package real.talk.model.dto.gladia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreRecorderResponse {
    private String id;
    private String resultUrl;
}
