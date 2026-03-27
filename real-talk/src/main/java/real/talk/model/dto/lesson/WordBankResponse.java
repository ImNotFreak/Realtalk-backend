package real.talk.model.dto.lesson;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordBankResponse {
    private List<WordBankResponse.GlossaryItem> items;
    private String quizlet_export;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GlossaryItem {
        private String expression;
        private String meaning;
        private String quote;
        @JsonAlias("timeCode")
        private String timestamp;
    }
}
